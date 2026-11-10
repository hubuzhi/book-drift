package com.book.servlet;

import com.book.entity.User;
import com.book.util.UserStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 个人中心资料操作
 * 路径：/updateProfile
 * 支持三类操作（通过 action 参数区分）：
 *   action=profile  → 修改昵称、签名
 *   action=password → 修改密码
 */
@WebServlet("/updateProfile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String msg;
        boolean success = false;

        if ("password".equals(action)) {
            msg = changePassword(request, loginUser);
            success = msg == null;
            if (success) msg = "密码修改成功，下次登录请使用新密码";
        } else {
            msg = updateProfile(request, loginUser);
            success = msg == null;
            if (success) msg = "个人资料已更新";
        }

        if (!success) {
            session.setAttribute("msgError", msg);
        } else {
            session.setAttribute("msg", msg);
        }
        // 头像、昵称等可能变化，重新写回用户文件
        persist(request);

        response.sendRedirect(request.getContextPath() + "/personal.jsp");
    }

    /** 校验并更新昵称、签名，返回 null 表示成功，否则返回错误提示 */
    private String updateProfile(HttpServletRequest request, User user) {
        String nickname = trim(request.getParameter("nickname"));
        String signature = trim(request.getParameter("signature"));

        if (nickname.isEmpty()) {
            return "昵称不能为空";
        }
        if (nickname.length() > 20) {
            return "昵称最多 20 个字";
        }
        if (signature.length() > 80) {
            return "签名最多 80 个字";
        }
        // 用户数据按行、按 ||| 分隔存储，避免破坏文件结构
        if (nickname.contains("|||") || signature.contains("|||")) {
            return "内容不能包含连续的竖线";
        }
        nickname = nickname.replaceAll("[\\r\\n]+", " ");
        signature = signature.replaceAll("[\\r\\n]+", " ");

        user.setNickname(nickname);
        user.setSignature(signature);
        return null;
    }

    /** 校验并修改密码，返回 null 表示成功 */
    private String changePassword(HttpServletRequest request, User user) {
        String oldPassword = trim(request.getParameter("oldPassword"));
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmPassword = trim(request.getParameter("confirmPassword"));

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            return "原密码和新密码都不能为空";
        }
        if (!oldPassword.equals(user.getPassword())) {
            return "原密码不正确";
        }
        if (newPassword.length() < 6 || newPassword.length() > 32) {
            return "新密码长度需为 6~32 位";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "两次输入的新密码不一致";
        }
        user.setPassword(newPassword);
        return null;
    }

    /** 把内存中的用户列表写入文件 */
    @SuppressWarnings("unchecked")
    private void persist(HttpServletRequest request) {
        List<User> userList = (List<User>) getServletContext().getAttribute("userList");
        if (userList != null) {
            UserStore.save(getServletContext().getRealPath("/"), userList);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
