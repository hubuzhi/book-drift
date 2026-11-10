package com.book.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.book.entity.Book;
import com.book.entity.User;
import com.book.util.UserStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 收藏 Servlet（F5）
 * 路径：/collect?id=xxx
 * 功能：登录用户收藏/取消收藏，收藏集合存 Session（key：collectBooks）
 */
@WebServlet("/collect")
public class CollectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 获取 Session 对象
        HttpSession session = request.getSession();

        // 2. 判断用户是否登录，session 中 loginUser 为 null 代表未登录
        // 【修复】原代码 key 为 "user"，与 LoginServlet 不一致，改为 "loginUser"
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            // 重定向跳转到登录页
            // 【修复】原路径 "/jsp/login.jsp" 不存在，改为 "/login.jsp"
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 3. 获取请求参数 id（图书编号）
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            // 没有 id 参数，跳转到我的收藏页面
            response.sendRedirect(request.getContextPath() + "/myCollect.jsp");
            return;
        }
        long bookId = Long.parseLong(idStr);

        // 4. 从 application 域获取全部图书列表
        // 【修复】原代码从 session 取 "bookList"，但 InitDataListener 存入的是 application 域
        List<Book> allBook = (List<Book>) getServletContext().getAttribute("bookList");
        Book targetBook = null;
        if (allBook != null) {
            for (Book b : allBook) {
                if (b.getId() == bookId) {
                    targetBook = b;
                    break;
                }
            }
        }
        // 找不到图书，回到图书列表
        if (targetBook == null) {
            // 【修复】原路径 "/jsp/bookList.jsp" 不存在，改为 Servlet 路径 "/bookList"
            response.sendRedirect(request.getContextPath() + "/bookList");
            return;
        }

        // 5. 取出收藏集合，key 固定为 collectBooks
        List<Book> collectBooks = (List<Book>) session.getAttribute("collectBooks");
        // 如果 session 里没有这个集合，新建 ArrayList
        if (collectBooks == null) {
            collectBooks = new ArrayList<>();
        }

        // 6. 判断：存在则移除（取消收藏），不存在则添加（收藏）
        if (BookListServlet.containsBook(collectBooks, bookId)) {
            // 已收藏，取消收藏
            Book toRemove = null;
            for (Book b : collectBooks) {
                if (b.getId() == bookId) {
                    toRemove = b;
                    break;
                }
            }
            if (toRemove != null) {
                collectBooks.remove(toRemove);
            }
        } else {
            // 未收藏，添加收藏
            collectBooks.add(targetBook);
        }

        // 7. 把收藏集合放回 session 域
        session.setAttribute("collectBooks", collectBooks);

        // 7.1 同步到用户对象并写入用户文件，退出重新登录后收藏仍在
        List<Long> collectIds = new ArrayList<>();
        for (Book b : collectBooks) {
            collectIds.add(b.getId());
        }
        user.setCollectIds(collectIds);
        @SuppressWarnings("unchecked")
        List<User> userList = (List<User>) getServletContext().getAttribute("userList");
        if (userList != null) {
            UserStore.save(getServletContext().getRealPath("/"), userList);
        }

        // 8. 异步请求（ajax=1）：只返回最新收藏状态，不跳转页面，避免页面回到顶部
        if ("1".equals(request.getParameter("ajax"))) {
            boolean collected = BookListServlet.containsBook(collectBooks, bookId);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"collected\":" + collected + "}");
            return;
        }

        // 9. 普通请求：重定向回到图书列表页面
        // 【修复】原路径 "/jsp/bookList.jsp" 不存在，改为 Servlet 路径 "/bookList"
        response.sendRedirect(request.getContextPath() + "/bookList");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // post 请求直接调用 doGet，统一处理
        doGet(request, response);
    }
}
