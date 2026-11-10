package com.book.servlet;

import com.book.entity.Book;
import com.book.entity.Chapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线阅读 Servlet
 * 路径：/readBook
 * 功能：根据图书 id 获取阅读内容，支持 ?chapter=N 指定章节；
 *       记录每个用户的阅读进度（Session），下次打开自动续读
 */
@WebServlet("/readBook")
public class ReadServlet extends HttpServlet {

    /** Session 中保存阅读进度的 key：Map<bookId, chapterIndex> */
    private static final String PROGRESS_KEY = "readingProgress";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long bookId;
        try {
            bookId = Long.parseLong(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/bookList");
            return;
        }

        @SuppressWarnings("unchecked")
        List<Book> bookList = (List<Book>) getServletContext().getAttribute("bookList");
        Book targetBook = null;
        if (bookList != null) {
            for (Book book : bookList) {
                if (book.getId() == bookId) {
                    targetBook = book;
                    break;
                }
            }
        }

        if (targetBook == null) {
            response.sendRedirect(request.getContextPath() + "/bookList");
            return;
        }

        List<Chapter> chapters = targetBook.getChapters();
        int chapterCount = chapters.size();

        // 读取/初始化阅读进度
        HttpSession session = request.getSession(true);
        // 在线阅读同样计入浏览记录，避免只点“阅读”不点详情时历史为空
        BrowseHistoryServlet.addHistory(session, bookId);
        @SuppressWarnings("unchecked")
        Map<Long, Integer> progress = (Map<Long, Integer>) session.getAttribute(PROGRESS_KEY);
        if (progress == null) {
            progress = new HashMap<>();
            session.setAttribute(PROGRESS_KEY, progress);
        }

        int current = 0;
        String chapterParam = request.getParameter("chapter");
        if (chapterParam != null && !chapterParam.trim().isEmpty()) {
            // 显式指定章节（点目录或翻页），同时更新进度
            try {
                current = Integer.parseInt(chapterParam.trim());
            } catch (NumberFormatException e) {
                current = 0;
            }
            progress.put(bookId, current);
        } else {
            // 未指定章节：续读上次进度
            Integer saved = progress.get(bookId);
            if (saved != null) {
                current = saved;
            }
        }

        // 章节范围校验
        if (chapterCount > 0) {
            if (current < 0) current = 0;
            if (current > chapterCount - 1) current = chapterCount - 1;
        } else {
            current = 0;
        }

        request.setAttribute("book", targetBook);
        request.setAttribute("chapters", chapters);
        request.setAttribute("chapterCount", chapterCount);
        request.setAttribute("currentChapter", current);
        request.getRequestDispatcher("/readBook.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
