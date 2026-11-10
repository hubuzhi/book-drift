package com.book.servlet;

import com.book.entity.User;
import com.book.util.UserStore;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * 清除浏览记录 Servlet
 * 路径：/clearHistory
 */
@WebServlet("/clearHistory")
public class ClearHistoryServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("browseHistory");
            // 同步清空账号里持久化的浏览记录
            Object loginUser = session.getAttribute("loginUser");
            if (loginUser instanceof User) {
                User user = (User) loginUser;
                user.setHistoryIds(new java.util.ArrayList<Long>());
                @SuppressWarnings("unchecked")
                List<User> userList = (List<User>) session.getServletContext().getAttribute("userList");
                if (userList != null) {
                    UserStore.save(session.getServletContext().getRealPath("/"), userList);
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/browseHistory");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doPost(request, response);
    }
}
