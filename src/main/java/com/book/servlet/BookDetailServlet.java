package com.book.servlet;

import com.book.entity.Book;
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
 * 图书详情 Servlet
 * 路径：/bookDetail
 * 功能：根据图书 id 查找图书并转发到 bookDetail.jsp 展示
 */
@WebServlet("/bookDetail")
public class BookDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 获取请求参数 id
        long bookId;
        try {
            bookId = Long.parseLong(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "图书编号不合法");
            return;
        }

        // 2. 从 application 域获取图书列表
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

        // 3. 图书不存在则返回 404
        if (targetBook == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "图书不存在");
            return;
        }

        // 4. 记录浏览历史
        HttpSession session = request.getSession(true);
        BrowseHistoryServlet.addHistory(session, bookId);

        // 5. 判断当前用户是否已收藏该书
        List<Book> collectBooks = BookListServlet.castBooks(
                session.getAttribute("collectBooks"));
        boolean favorited = BookListServlet.containsBook(collectBooks, bookId);

        // 6. 将数据传递到 JSP
        request.setAttribute("book", targetBook);
        request.setAttribute("favorited", favorited);
        request.getRequestDispatcher("/bookDetail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
