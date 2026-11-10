package com.book.util;

import com.book.entity.Book;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图书封面 / 简介补全器
 *
 * 启动后在后台线程逐本处理：
 * 1. 封面优先使用豆瓣图书接口返回的真实出版封面；找不到时调用 AI 文生图接口生成；
 * 2. 简介使用豆瓣图书详情页的「内容简介」，替换初始化时的简短描述；
 * 3. 结果缓存到 WEB-INF/covers 目录，避免每次启动重复请求网络。
 */
public final class BookEnricher {

    /** book.douban.com 的 suggest 接口已被反爬拦截（403），改用仍可访问的豆瓣 v2 图书接口 */
    private static final String SEARCH_URL = "https://api.douban.com/v2/book/search?q=";
    private static final String API_KEY = "0ac44ae016490db2204ce0a042db2916";
    private static final String AI_COVER_URL =
            "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=";

    private static final String COVER_DIR = "WEB-INF" + File.separator + "covers";
    private static final String[] COVER_EXTENSIONS = {"jpg", "jpeg", "png", "webp", "gif", "svg"};

    private static final int CONNECT_TIMEOUT = 6000;
    private static final int READ_TIMEOUT = 12000;

    /** AI 文生图接口在不可用时会返回同一张“图片生成中”占位图，不能再把它当成封面缓存 */
    private static final long PLACEHOLDER_SIZE = 176626L;
    private static final String PLACEHOLDER_MD5 = "19a0b822edb11957055e4588c2159058";

    /** 数据源连续失败 5 次后停用，避免几十本书重复等待超时 */
    private static volatile boolean doubanDisabled = false;
    private static volatile boolean aiCoverDisabled = false;
    private static int doubanFailures = 0;
    private static final Object THROTTLE_LOCK = new Object();
    private static long lastRequestAt = 0;

    private BookEnricher() {
    }

    /**
     * 启动一个后台守护线程，依次补全所有图书，避免拖慢应用启动。
     */
    public static void enrichAll(final List<Book> books, final ServletContext context) {
        if (books == null || books.isEmpty()) {
            return;
        }
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                // 并发补全，避免几十本书逐本排队导致后面的书长时间显示占位封面
                ExecutorService pool = Executors.newFixedThreadPool(4);
                for (final Book book : books) {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                enrich(book, context);
                            } catch (Exception e) {
                                System.err.println("[BookEnricher] 处理《" + book.getTitle() + "》失败: " + e.getMessage());
                            }
                        }
                    });
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(10, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[BookEnricher] 图书封面与简介补全完成");
            }
        }, "book-enricher");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * 补全单本图书：已有缓存直接使用，缺失部分才请求网络。
     */
    public static void enrich(Book book, ServletContext context) {
        String realPath = context.getRealPath("/");
        if (realPath == null) {
            return;
        }
        File coverDir = new File(realPath, COVER_DIR);

        String cachedCover = findCachedCover(coverDir, book.getId());
        String cachedIntro = readText(new File(coverDir, book.getId() + ".txt"));

        if (cachedCover != null) {
            book.setCoverFile(cachedCover);
        }
        if (cachedIntro != null && !cachedIntro.trim().isEmpty()) {
            book.setDescription(cachedIntro.trim());
        }
        if (cachedCover != null && cachedIntro != null) {
            return;
        }

        DoubanBook douban = doubanDisabled ? null : findOnDouban(book.getTitle());

        if (cachedIntro == null && douban != null && douban.summary != null) {
            String intro = douban.summary.trim();
            if (!intro.isEmpty()) {
                book.setDescription(intro);
                writeText(new File(coverDir, book.getId() + ".txt"), intro);
                System.out.println("[BookEnricher] 已更新简介《" + book.getTitle() + "》");
            }
        }

        if (cachedCover == null) {
            byte[] image = null;
            if (douban != null) {
                image = downloadImage(douban.pic, douban.url);
            }
            if (image == null && !aiCoverDisabled) {
                image = generateAiCover(book);
            }
            if (isPlaceholder(image)) {
                image = null;
            }
            if (image != null) {
                String fileName = saveCover(coverDir, book.getId(), image);
                if (fileName != null) {
                    book.setCoverFile(fileName);
                    System.out.println("[BookEnricher] 已更新封面《" + book.getTitle() + "》");
                }
            } else {
                // 在线封面源都不可用时，生成一张本地 SVG 文字封面，保证每本书都有独立封面
                String fileName = saveSvgCover(coverDir, book);
                if (fileName != null) {
                    book.setCoverFile(fileName);
                    System.out.println("[BookEnricher] 已生成本地封面《" + book.getTitle() + "》");
                }
            }
        }
    }

    // ==================== 豆瓣 ====================

    private static final class DoubanBook {
        final String title;
        final String url;
        final String pic;
        final String summary;

        DoubanBook(String title, String url, String pic, String summary) {
            this.title = title;
            this.url = url;
            this.pic = pic;
            this.summary = summary;
        }
    }

    private static DoubanBook findOnDouban(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        try {
            throttle();
            String url = SEARCH_URL + URLEncoder.encode(title.trim(), "UTF-8")
                    + "&count=5&apikey=" + API_KEY;
            String json = HttpFetcher.get(url, doubanHeaders("https://book.douban.com/"),
                    CONNECT_TIMEOUT, READ_TIMEOUT).getText();
            List<DoubanBook> candidates = new ArrayList<>();
            for (String object : arrayObjects(json, "books")) {
                String name = jsonString(object, "title");
                String subjectUrl = jsonString(object, "url");
                String pic = jsonString(object, "image");
                String summary = jsonString(object, "summary");
                if (name == null || pic == null) {
                    continue;
                }
                candidates.add(new DoubanBook(name, subjectUrl, largeCover(pic), summary));
            }
            if (candidates.isEmpty()) {
                return null;
            }
            doubanFailures = 0;
            String target = normalize(title);
            for (DoubanBook candidate : candidates) {
                if (normalize(candidate.title).equals(target)) {
                    return candidate;
                }
            }
            for (DoubanBook candidate : candidates) {
                String candidateTitle = normalize(candidate.title);
                if (candidateTitle.contains(target) || target.contains(candidateTitle)) {
                    return candidate;
                }
            }
            return candidates.get(0);
        } catch (Exception e) {
            if (++doubanFailures >= 5) {
                doubanDisabled = true;
            }
            System.err.println("[BookEnricher] 豆瓣搜索失败《" + title + "》: " + e.getMessage());
            return null;
        }
    }

    /** 逐个请求之间留出间隔，避免触发豆瓣接口的频率限制 */
    private static void throttle() {
        synchronized (THROTTLE_LOCK) {
            long wait = 350 - (System.currentTimeMillis() - lastRequestAt);
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestAt = System.currentTimeMillis();
        }
    }

    /**
     * 从 JSON 里取出某个数组中的第一层对象。
     * 豆瓣返回的 book 对象内部还有 rating/tags/images 等嵌套对象，
     * 不能简单用正则匹配大括号，这里按括号层级和字符串转义来切分。
     */
    private static List<String> arrayObjects(String json, String key) {
        List<String> objects = new ArrayList<>();
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            return objects;
        }
        int start = json.indexOf('[', keyIndex);
        if (start < 0) {
            return objects;
        }
        int depth = 0;
        int objectStart = -1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) {
                    objectStart = i;
                }
                depth++;
            } else if (c == '}') {
                if (depth > 0) {
                    depth--;
                }
                if (depth == 0 && objectStart >= 0) {
                    objects.add(json.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            } else if (c == ']' && depth == 0) {
                break;
            }
        }
        return objects;
    }

    private static byte[] downloadImage(String imageUrl, String referer) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        byte[] body = tryDownload(imageUrl, referer);
        if (body == null && imageUrl.contains("/l/public/")) {
            // 大图取不到时退回中图
            body = tryDownload(imageUrl.replace("/l/public/", "/m/public/"), referer);
        }
        return body;
    }

    private static byte[] tryDownload(String imageUrl, String referer) {
        try {
            byte[] body = HttpFetcher.get(imageUrl, doubanHeaders(referer),
                    CONNECT_TIMEOUT, READ_TIMEOUT).getBody();
            if (body != null && body.length > 1024 && !isPlaceholder(body)) {
                return body;
            }
        } catch (Exception e) {
            System.err.println("[BookEnricher] 下载封面失败 " + imageUrl + ": " + e.getMessage());
        }
        return null;
    }

    private static Map<String, String> doubanHeaders(String referer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", referer != null ? referer : "https://book.douban.com/");
        headers.put("Accept", "text/html,application/json,image/*,*/*;q=0.8");
        return headers;
    }

    private static String largeCover(String pic) {
        // 搜索结果给的是小图（s/public），换成大图（l/public）
        return pic.replace("/s/public/", "/l/public/")
                .replace("/m/public/", "/l/public/");
    }

    private static String jsonString(String object, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(object);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }

    private static String unescapeJson(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) {
                sb.append(c);
                continue;
            }
            char next = value.charAt(++i);
            switch (next) {
                case 'n':
                    sb.append('\n');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'b':
                    sb.append('\b');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'u':
                    if (i + 4 < value.length()) {
                        try {
                            sb.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            sb.append(next);
                        }
                    } else {
                        sb.append(next);
                    }
                    break;
                default:
                    sb.append(next);
            }
        }
        return sb.toString();
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\s\\u3000·\\-—:：,，.。()（）\\[\\]【】]", "").toLowerCase();
    }

    // ==================== AI 封面 ====================

    private static byte[] generateAiCover(Book book) {
        if (aiCoverDisabled) {
            return null;
        }
        try {
            String description = book.getDescription();
            if (description != null && description.length() > 120) {
                description = description.substring(0, 120);
            }
            String prompt = "A beautiful professional book cover illustration for the book \""
                    + book.getTitle() + "\" by " + (book.getAuthor() == null ? "" : book.getAuthor())
                    + ". Genre: " + (book.getCategory() == null ? "" : book.getCategory())
                    + ". Theme: " + (description == null ? "" : description)
                    + ". Elegant publishing design, cinematic lighting, rich details, "
                    + "high quality artwork, no text, no letters, no words.";
            String url = AI_COVER_URL + URLEncoder.encode(prompt, "UTF-8") + "&image_size=portrait_4_3";
            byte[] body = HttpFetcher.get(url, null, CONNECT_TIMEOUT, READ_TIMEOUT).getBody();
            if (isPlaceholder(body)) {
                // 接口只会返回“图片生成中”占位图，继续调用只会重复拿到同一张图
                aiCoverDisabled = true;
                System.err.println("[BookEnricher] AI 封面接口返回占位图，已停用 AI 封面");
                return null;
            }
            if (body != null && body.length > 4096) {
                return body;
            }
        } catch (Exception e) {
            aiCoverDisabled = true;
            System.err.println("[BookEnricher] AI 生成封面失败《" + book.getTitle() + "》: " + e.getMessage());
        }
        return null;
    }

    // ==================== 文件缓存 ====================

    private static String findCachedCover(File coverDir, long bookId) {
        for (String ext : COVER_EXTENSIONS) {
            File file = new File(coverDir, bookId + "." + ext);
            if (!file.isFile() || file.length() <= 1024) {
                continue;
            }
            if (isPlaceholderFile(file)) {
                // 旧版本把 AI 接口的“图片生成中”占位图缓存成了封面，这里自动清理
                if (file.delete()) {
                    System.out.println("[BookEnricher] 已清理占位封面 " + file.getName());
                }
                continue;
            }
            return file.getName();
        }
        return null;
    }

    private static String saveCover(File coverDir, long bookId, byte[] image) {
        if (!coverDir.exists() && !coverDir.mkdirs()) {
            return null;
        }
        String ext = detectExtension(image);
        String fileName = bookId + "." + ext;
        try (OutputStream out = new FileOutputStream(new File(coverDir, fileName))) {
            out.write(image);
        } catch (IOException e) {
            System.err.println("[BookEnricher] 保存封面失败: " + e.getMessage());
            return null;
        }
        return fileName;
    }

    private static String detectExtension(byte[] image) {
        if (image.length >= 8
                && (image[0] & 0xFF) == 0x89 && image[1] == 'P' && image[2] == 'N' && image[3] == 'G') {
            return "png";
        }
        if (image.length >= 12 && image[0] == 'R' && image[1] == 'I' && image[2] == 'F' && image[3] == 'F'
                && image[8] == 'W' && image[9] == 'E' && image[10] == 'B' && image[11] == 'P') {
            return "webp";
        }
        return "jpg";
    }

    // ==================== 本地 SVG 文字封面 ====================

    /** 按分类配色的文字封面，在线封面源不可用时保证每本书仍有独立封面 */
    private static final String[][] CATEGORY_COLORS = {
            {"文学", "#3b7d6b", "#1f4a3f"},
            {"古典", "#a9783c", "#59391a"},
            {"科幻", "#3f5f92", "#1b2c4d"},
            {"计算机", "#2f6f8f", "#153f52"},
            {"经济", "#8a5a2b", "#4d2c11"},
            {"历史", "#7a5b3a", "#3f2b18"},
            {"哲学", "#6b5a8f", "#372a52"},
            {"心理", "#5a7a8f", "#2a3f4f"},
            {"艺术", "#b45a43", "#6f3025"},
            {"科普", "#4f7a8a", "#24404a"},
            {"传记", "#7b5275", "#452b41"},
            {"小说", "#8f5a5a", "#4d2424"}
    };
    private static final String[] DEFAULT_COLORS = {"#55699b", "#2b3760"};

    private static String saveSvgCover(File coverDir, Book book) {
        if (!coverDir.exists() && !coverDir.mkdirs()) {
            return null;
        }
        String fileName = book.getId() + ".svg";
        try (OutputStream out = new FileOutputStream(new File(coverDir, fileName))) {
            out.write(buildSvgCover(book).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[BookEnricher] 保存本地封面失败: " + e.getMessage());
            return null;
        }
        return fileName;
    }

    private static String buildSvgCover(Book book) {
        String[] colors = colorsFor(book.getCategory());
        String title = book.getTitle() == null ? "" : book.getTitle().trim();
        String author = book.getAuthor() == null ? "" : book.getAuthor().trim();
        String category = book.getCategory() == null ? "" : book.getCategory().trim();
        String initial = title.isEmpty() ? "书" : title.substring(0, 1);

        int fontSize;
        int charsPerLine;
        if (title.length() <= 4) {
            fontSize = 58;
            charsPerLine = 4;
        } else if (title.length() <= 8) {
            fontSize = 48;
            charsPerLine = 4;
        } else if (title.length() <= 12) {
            fontSize = 42;
            charsPerLine = 4;
        } else {
            fontSize = 34;
            charsPerLine = 5;
        }

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < title.length(); i += charsPerLine) {
            lines.add(title.substring(i, Math.min(title.length(), i + charsPerLine)));
        }

        int lineHeight = (int) Math.round(fontSize * 1.32);
        int blockHeight = lines.size() * lineHeight;
        int firstBaseline = 292 - blockHeight / 2 + fontSize;

        StringBuilder titleText = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            titleText.append("<text x=\"200\" y=\"").append(firstBaseline + i * lineHeight)
                    .append("\" font-size=\"").append(fontSize)
                    .append("\" text-anchor=\"middle\" fill=\"#f8f3e6\" letter-spacing=\"3\">")
                    .append(escapeXml(lines.get(i))).append("</text>");
        }

        int catWidth = Math.max(66, category.length() * 20 + 26);
        StringBuilder authorText = new StringBuilder();
        if (!author.isEmpty()) {
            authorText.append("<line x1=\"168\" y1=\"430\" x2=\"232\" y2=\"430\" ")
                    .append("stroke=\"#ffffff\" stroke-opacity=\"0.45\" stroke-width=\"1\"/>")
                    .append("<text x=\"200\" y=\"466\" font-size=\"18\" text-anchor=\"middle\" ")
                    .append("fill=\"#f0e9da\" fill-opacity=\"0.9\" letter-spacing=\"2\">")
                    .append(escapeXml(author)).append("</text>");
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"400\" height=\"560\" "
                + "viewBox=\"0 0 400 560\" font-family=\"Songti SC, STSong, Noto Serif CJK SC, SimSun, serif\">\n"
                + "  <defs>\n"
                + "    <linearGradient id=\"bg\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">\n"
                + "      <stop offset=\"0\" stop-color=\"" + colors[0] + "\"/>\n"
                + "      <stop offset=\"1\" stop-color=\"" + colors[1] + "\"/>\n"
                + "    </linearGradient>\n"
                + "  </defs>\n"
                + "  <rect width=\"400\" height=\"560\" fill=\"url(#bg)\"/>\n"
                + "  <circle cx=\"338\" cy=\"92\" r=\"118\" fill=\"#ffffff\" fill-opacity=\"0.055\"/>\n"
                + "  <circle cx=\"52\" cy=\"498\" r=\"146\" fill=\"#ffffff\" fill-opacity=\"0.045\"/>\n"
                + "  <rect x=\"20\" y=\"0\" width=\"3\" height=\"560\" fill=\"#ffffff\" fill-opacity=\"0.16\"/>\n"
                + "  <rect x=\"32\" y=\"32\" width=\"336\" height=\"496\" fill=\"none\" "
                + "stroke=\"#ffffff\" stroke-opacity=\"0.22\" stroke-width=\"1\"/>\n"
                + "  <text x=\"352\" y=\"524\" font-size=\"132\" text-anchor=\"end\" "
                + "fill=\"#ffffff\" fill-opacity=\"0.10\">" + escapeXml(initial) + "</text>\n"
                + "  <rect x=\"" + (200 - catWidth / 2) + "\" y=\"64\" width=\"" + catWidth + "\" height=\"30\" "
                + "rx=\"15\" fill=\"#ffffff\" fill-opacity=\"0.14\" stroke=\"#ffffff\" stroke-opacity=\"0.28\"/>\n"
                + "  <text x=\"200\" y=\"84\" font-size=\"15\" text-anchor=\"middle\" "
                + "fill=\"#f6f1e4\" letter-spacing=\"2\">" + escapeXml(category) + "</text>\n"
                + "  " + titleText + "\n"
                + "  " + authorText + "\n"
                + "  <text x=\"200\" y=\"532\" font-size=\"11\" text-anchor=\"middle\" "
                + "fill=\"#ffffff\" fill-opacity=\"0.42\" letter-spacing=\"3\">图书漂流</text>\n"
                + "</svg>\n";
    }

    private static String[] colorsFor(String category) {
        if (category != null) {
            for (String[] item : CATEGORY_COLORS) {
                if (item[0].equals(category)) {
                    return new String[]{item[1], item[2]};
                }
            }
        }
        return DEFAULT_COLORS;
    }

    private static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** 占位图文件：尺寸与 MD5 都与 AI 接口的“生成中”默认图一致 */
    private static boolean isPlaceholderFile(File file) {
        if (file.length() != PLACEHOLDER_SIZE) {
            return false;
        }
        try {
            return isPlaceholder(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isPlaceholder(byte[] image) {
        if (image == null || image.length != PLACEHOLDER_SIZE) {
            return false;
        }
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(image);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return PLACEHOLDER_MD5.equals(hex.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static String readText(File file) {
        if (!file.isFile() || file.length() == 0) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            int offset = 0;
            int len;
            while (offset < buffer.length && (len = in.read(buffer, offset, buffer.length - offset)) != -1) {
                offset += len;
            }
            return new String(buffer, 0, offset, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static void writeText(File file, String text) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return;
        }
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[BookEnricher] 保存简介失败: " + e.getMessage());
        }
    }
}
