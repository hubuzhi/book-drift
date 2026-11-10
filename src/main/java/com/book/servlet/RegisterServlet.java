package com.book.servlet;

import com.book.entity.User;
import com.book.util.UserStore;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;




/**
 * 用户注册 Servlet（F1，骨架）
 * 路径：/register
 * 【待填写】doPost 实现：接收用户名/密码/昵称，封装 User，存入 ServletContext 的 userList，注册成功跳转登录页
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: 待填写
        this.doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: 待填写
        //防止中文乱码
        request.setCharacterEncoding("UTF-8");
        //获取表单数据
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        //获取用户列表
        ServletContext application = this.getServletContext();
        List<User> userList = (List<User>) application.getAttribute("userList");
        //如果用户列表为空，就new一个
        if (userList == null) {
            userList = new ArrayList<User>();
        }
        //检验用户名是否存在
        boolean isExist = false;
        for (int i=0; i<userList.size(); i++) {
            User u = userList.get(i);
            if (u.getUsername() != null && u.getUsername().equals(username)) {
                isExist = true;
                break;
            }
        }
        //结果
        if (isExist) {
            //用户名存在，注册失败
            request.setAttribute("msg", "用户名已存在");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        } else {
            //用户名不存在，注册成功
            User newUser = new User(username, password, nickname, null);
            userList.add(newUser);
            application.setAttribute("userList", userList);
            // 持久化到文件，防止重启后丢失
            UserStore.save(application.getRealPath("/"), userList);
            response.sendRedirect(request.getContextPath()+"/login.jsp");
        }
    }
}
