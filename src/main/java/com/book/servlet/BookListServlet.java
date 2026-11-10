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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 图书列表 Servlet
 * 路径：/bookList
 * 功能：从 application 域获取图书列表，支持分类筛选和搜索，传递到 bookList.jsp 展示
 */
@WebServlet("/bookList")
public class BookListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 从 application 域获取图书列表（InitDataListener 初始化写入）
        List<Book> bookList = (List<Book>) getServletContext().getAttribute("bookList");
        if (bookList == null) {
            bookList = new ArrayList<>();
        }

        // 获取分类和搜索参数
        String category = request.getParameter("category");
        String query = request.getParameter("q");

        // 构建显示列表（原始列表的副本）
        List<Book> displayList = new ArrayList<>(bookList);

        // 按分类筛选
        if (category != null && !category.trim().isEmpty()) {
            List<Book> filtered = new ArrayList<>();
            for (Book book : displayList) {
                if (category.equals(book.getCategory())) {
                    filtered.add(book);
                }
            }
            displayList = filtered;
        }

        // 按关键词搜索（书名或作者）
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            List<Book> searched = new ArrayList<>();
            for (Book book : displayList) {
                if ((book.getTitle() != null && book.getTitle().toLowerCase().contains(q))
                        || (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(q))) {
                    searched.add(book);
                }
            }
            displayList = searched;
        }

        // bookList 用于轮播（显示全部图书）
        request.setAttribute("bookList", bookList);
        // 首页轮播精选 10 本（按下面的书名顺序排列）
        String[] featuredTitles = {"平凡的世界", "活着", "百年孤独", "红楼梦", "三国演义",
                "三体", "流浪地球", "万历十五年", "苏菲的世界", "思考，快与慢"};
        List<Book> featuredList = new ArrayList<>();
        for (String title : featuredTitles) {
            for (Book book : bookList) {
                if (title.equals(book.getTitle())) {
                    featuredList.add(book);
                    break;
                }
            }
        }
        request.setAttribute("featuredList", featuredList);
        // displayList 用于卡片列表（经过筛选/搜索后的结果）
        request.setAttribute("displayList", displayList);

        // 提取所有分类列表，供分类标签使用
        Set<String> categorySet = new LinkedHashSet<>();
        for (Book book : bookList) {
            if (book.getCategory() != null && !book.getCategory().trim().isEmpty()) {
                categorySet.add(book.getCategory());
            }
        }
        List<String> categories = new ArrayList<>(categorySet);
        // “计算机”固定排在最后
        if (categories.contains("计算机")) {
            categories.remove("计算机");
            categories.add("计算机");
        }
        request.setAttribute("categoryList", categories);

        // 获取当前用户的收藏列表（从 session 域）
        HttpSession session = request.getSession(false);
        List<Book> collectBooks = castBooks(session == null ? null : session.getAttribute("collectBooks"));
        request.setAttribute("collectBooks", collectBooks);

        // 将收藏图书的 ID 集合传给 JSP，方便判断是否已收藏
        Set<Long> favoritedIds = new HashSet<>();
        for (Book book : collectBooks) {
            favoritedIds.add(book.getId());
        }
        request.setAttribute("favoritedIds", favoritedIds);

        request.getRequestDispatcher("/bookList.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * 安全地将 Object 转换为 List<Book>
     */
    @SuppressWarnings("unchecked")
    static List<Book> castBooks(Object value) {
        return value instanceof List<?> ? (List<Book>) value : new ArrayList<>();
    }

    /**
     * 判断收藏列表中是否包含指定 id 的图书
     */
    static boolean containsBook(List<Book> books, long bookId) {
        if (books == null) return false;
        for (Book book : books) {
            if (book.getId() == bookId) {
                return true;
            }
        }
        return false;
    }
}
