package com.book.util;

import com.book.entity.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户数据文件持久化工具
 * 将 userList 保存到文件，Tomcat 重启后不丢失注册数据
 * 文件格式：每行一条记录，字段用 ||| 分隔  username|||password|||nickname|||avatar
 *
 * 数据存放在 Tomcat 的 CATALINA_BASE/data/book-floating 下，
 * 不放在 webapps 部署目录里，避免重新部署应用时被一起删除。
 */
public class UserStore {

    private static final String SEPARATOR = "|||";
    private static final String FILE_NAME = "users.txt";
    private static final String WEB_DATA_DIR = "WEB-INF" + File.separator + "data";
    private static final String EXTERNAL_DATA_DIR = "data" + File.separator + "book-floating";

    /**
     * 解析数据目录：优先使用 Tomcat 根目录下的 data/book-floating，
     * 拿不到 catalina.base 时退回 WEB-INF/data。
     */
    private static File dataDir(String realPath) {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
            return new File(catalinaBase, EXTERNAL_DATA_DIR);
        }
        return new File(realPath, WEB_DATA_DIR);
    }

    /**
     * 从文件加载用户列表
     * @param realPath ServletContext.getRealPath("/") 返回的 webapp 根目录绝对路径
     */
    public static List<User> load(String realPath) {
        List<User> userList = new ArrayList<>();
        File file = new File(dataDir(realPath), FILE_NAME);
        if (!file.exists()) {
            return userList;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(Pattern.quote(SEPARATOR), -1);
                if (parts.length >= 3) {
                    String username = parts[0];
                    String password = parts[1];
                    String nickname = parts[2];
                    String avatar = parts.length >= 4 && !parts[3].isEmpty() ? parts[3] : null;
                    User user = new User(username, password, nickname, avatar);
                    if (parts.length >= 5 && !parts[4].isEmpty()) {
                        user.setSignature(parts[4]);
                    }
                    if (parts.length >= 6 && !parts[5].isEmpty()) {
                        user.setCollectIds(parseIds(parts[5]));
                    }
                    if (parts.length >= 7 && !parts[6].isEmpty()) {
                        user.setHistoryIds(parseIds(parts[6]));
                    }
                    userList.add(user);
                }
            }
        } catch (IOException e) {
            System.err.println("[UserStore] 加载用户文件失败: " + e.getMessage());
        }
        return userList;
    }

    /**
     * 将用户列表保存到文件
     * @param realPath ServletContext.getRealPath("/") 返回的 webapp 根目录绝对路径
     * @param userList 要保存的用户列表
     */
    public static void save(String realPath, List<User> userList) {
        File dir = dataDir(realPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, FILE_NAME);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (User u : userList) {
                String line = u.getUsername() + SEPARATOR
                        + u.getPassword() + SEPARATOR
                        + (u.getNickname() != null ? u.getNickname() : "") + SEPARATOR
                        + (u.getAvatar() != null ? u.getAvatar() : "") + SEPARATOR
                        + (u.getSignature() != null ? u.getSignature() : "") + SEPARATOR
                        + joinIds(u.getCollectIds()) + SEPARATOR
                        + joinIds(u.getHistoryIds());
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("[UserStore] 保存用户文件失败: " + e.getMessage());
        }
    }

    /** 解析逗号分隔的图书 ID 列表 */
    private static List<Long> parseIds(String value) {
        List<Long> ids = new ArrayList<>();
        for (String s : value.split(",")) {
            s = s.trim();
            if (s.isEmpty()) continue;
            try {
                ids.add(Long.parseLong(s));
            } catch (NumberFormatException ignore) {
                // 跳过损坏的 ID，不影响用户登录
            }
        }
        return ids;
    }

    /** 把图书 ID 列表拼成逗号分隔字符串 */
    private static String joinIds(List<Long> ids) {
        StringBuilder sb = new StringBuilder();
        if (ids != null) {
            for (Long id : ids) {
                if (sb.length() > 0) sb.append(',');
                sb.append(id);
            }
        }
        return sb.toString();
    }
}
