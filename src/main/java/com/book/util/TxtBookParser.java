package com.book.util;

import com.book.entity.Chapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 全本小说 txt 解析器
 * 功能：读取 txt 文本，按章节标题（第X章/第X回/第X节/第X卷/第X篇）自动切分成章节列表
 */
public class TxtBookParser {

    /** 匹配章节标题行：第 + 中文/阿拉伯数字 + 章/回/节/卷/篇 */
    private static final Pattern CHAPTER_PATTERN =
            Pattern.compile("^第\\s*[一二三四五六七八九十百千零两\\d]+\\s*[章回节卷篇].*$");

    /** 章节标题最大长度，超过则不认为是标题（避免把正文误判为标题） */
    private static final int MAX_TITLE_LENGTH = 40;

    /**
     * 解析文件为章节列表
     */
    public static List<Chapter> parse(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
        }
        return parseText(text.toString());
    }

    /**
     * 解析文本为章节列表
     */
    public static List<Chapter> parseText(String text) {
        List<Chapter> chapters = new ArrayList<>();
        String[] lines = text.split("\n");

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                // 空行作为段落分隔
                if (currentContent.length() > 0) {
                    currentContent.append("\n");
                }
                continue;
            }
            if (isChapterTitle(line)) {
                // 遇到新章节标题，先保存上一章
                if (currentTitle != null) {
                    chapters.add(new Chapter(chapters.size(), currentTitle, currentContent.toString().trim()));
                }
                currentTitle = line;
                currentContent = new StringBuilder();
            } else {
                currentContent.append(line).append("\n");
            }
        }
        // 保存最后一章
        if (currentTitle != null) {
            chapters.add(new Chapter(chapters.size(), currentTitle, currentContent.toString().trim()));
        }
        // 全文没匹配到任何章节标题时，把整篇作为单章
        if (chapters.isEmpty() && !text.trim().isEmpty()) {
            chapters.add(new Chapter(0, "全文", text.trim()));
        }
        return chapters;
    }

    private static boolean isChapterTitle(String line) {
        return line.length() <= MAX_TITLE_LENGTH && CHAPTER_PATTERN.matcher(line).matches();
    }
}
