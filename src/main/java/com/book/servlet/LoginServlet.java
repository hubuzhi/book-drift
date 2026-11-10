package com.book.servlet;

import com.book.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



/**
 * 用户登录 Servlet（F2，骨架）
 * 路径：/login
 * 【待填写】doPost 实现：校验账号密码，成功把用户对象存入 Session（key：loginUser），
 *         并用 Cookie 记录上次登录时间，登录页展示
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

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
        //获取表单数据（账号密码）
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //获取全局的用户列表
        List<User> userList = (List<User>) getServletContext().getAttribute("userList");
        //遍历用户列表，检验账号密码
        User loginUser = null;
        if (userList != null){
            for (User u : userList){
                if (u.getUsername().equals(username) && u.getPassword().equals(password)){
                    loginUser = u;
                    break;
                }
            }
        }
        //判断登录结果
        if (loginUser != null){
            //登录成功
            //把用户对象存入 Session
            HttpSession session=request.getSession();
            session.setAttribute("loginUser", loginUser);
            // 登录时把账号里持久化的收藏恢复进 Session，退出重登后收藏不会丢
            List<com.book.entity.Book> allBooks =
                    (List<com.book.entity.Book>) getServletContext().getAttribute("bookList");
            List<com.book.entity.Book> collectBooks = new java.util.ArrayList<>();
            if (allBooks != null && loginUser.getCollectIds() != null) {
                for (Long collectId : loginUser.getCollectIds()) {
                    for (com.book.entity.Book book : allBooks) {
                        if (book.getId() == collectId) {
                            collectBooks.add(book);
                            break;
                        }
                    }
                }
            }
            session.setAttribute("collectBooks", collectBooks);
            // 恢复持久化的浏览记录
            session.setAttribute("browseHistory",
                    new java.util.ArrayList<Long>(loginUser.getHistoryIds()));
            //成功记录登录时间
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime=sdf.format(new Date());
            String encodedTime= URLEncoder.encode(currentTime, "UTF-8");
            Cookie cookie=new Cookie("lastLoginTime", encodedTime);
            cookie.setMaxAge(60*60*24*7);
            cookie.setPath("/");
            response.addCookie(cookie);
            response.sendRedirect(request.getContextPath() + "/bookList");
        }else {
            //登录失败
            request.setAttribute("msg", "用户名或密码错误！");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}

