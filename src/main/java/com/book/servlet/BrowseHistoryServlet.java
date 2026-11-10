package com.book.servlet;

import com.book.entity.Book;
import com.book.entity.User;
import com.book.util.UserStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 浏览记录 Servlet
 * 路径：/browseHistory
 * 功能：记录用户浏览过的图书，展示浏览历史
 */
@WebServlet("/browseHistory")
public class BrowseHistoryServlet extends HttpServlet {

    private static final String HISTORY_KEY = "browseHistory";
    private static final int MAX_HISTORY = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        List<Long> historyIds = getHistoryIds(session);

        @SuppressWarnings("unchecked")
        List<Book> bookList = (List<Book>) getServletContext().getAttribute("bookList");
        if (bookList == null) {
            bookList = new ArrayList<>();
        }

        // 根据浏览记录 ID 列表获取对应的图书对象
        List<Book> historyBooks = new ArrayList<>();
        for (Long id : historyIds) {
            for (Book book : bookList) {
                if (book.getId() == id) {
                    historyBooks.add(book);
                    break;
                }
            }
        }

        request.setAttribute("historyBooks", historyBooks);
        request.getRequestDispatcher("/browseHistory.jsp").forward(request, response);
    }

    /**
     * 添加浏览记录（供 BookDetailServlet 调用）
     */
    public static void addHistory(HttpSession session, long bookId) {
        if (session == null) return;
        @SuppressWarnings("unchecked")
        List<Long> history = (List<Long>) session.getAttribute(HISTORY_KEY);
        if (history == null) {
            history = new ArrayList<>();
        }
        // 去重：先移除已有的，再添加到最前面
        history.remove(Long.valueOf(bookId));
        history.add(0, bookId);
        // 限制最大记录数
        if (history.size() > MAX_HISTORY) {
            history = new ArrayList<>(history.subList(0, MAX_HISTORY));
        }
        session.setAttribute(HISTORY_KEY, history);

        // 同步到登录用户并落盘，退出重新登录后浏览记录仍在
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof User) {
            User user = (User) loginUser;
            user.setHistoryIds(new ArrayList<>(history));
            @SuppressWarnings("unchecked")
            List<User> userList = (List<User>) session.getServletContext().getAttribute("userList");
            if (userList != null) {
                UserStore.save(session.getServletContext().getRealPath("/"), userList);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Long> getHistoryIds(HttpSession session) {
        if (session == null) return new ArrayList<>();
        List<Long> history = (List<Long>) session.getAttribute(HISTORY_KEY);
        return history != null ? history : new ArrayList<>();
    }
}
