package com.book.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 图书封面图片 Servlet
 * 路径：/bookCover?id=1
 * 读取 BookEnricher 缓存到 WEB-INF/covers 的封面图片并输出
 */
@WebServlet("/bookCover")
public class BookCoverServlet extends HttpServlet {

    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png", "webp", "gif", "svg"};

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long bookId;
        try {
            bookId = Long.parseLong(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "图书编号不合法");
            return;
        }

        String realPath = getServletContext().getRealPath("/");
        if (realPath == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File coverDir = new File(realPath, "WEB-INF" + File.separator + "covers");

        File cover = null;
        for (String ext : EXTENSIONS) {
            File candidate = new File(coverDir, bookId + "." + ext);
            if (candidate.isFile() && candidate.length() > 0) {
                cover = candidate;
                break;
            }
        }
        if (cover == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(contentType(cover.getName()));
        response.setContentLengthLong(cover.length());
        response.setHeader("Cache-Control", "public, max-age=604800");

        try (FileInputStream in = new FileInputStream(cover);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    private String contentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}
