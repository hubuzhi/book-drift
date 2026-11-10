package com.book.servlet;

import com.book.entity.User;
import com.book.util.UserStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 头像上传 Servlet（选做 F8）
 * 路径：/uploadAvatar
 */
@WebServlet("/uploadAvatar")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,     // 1 MB 以上才写磁盘临时文件
        maxFileSize = 5 * 1024 * 1024,        // 单文件最大 5 MB
        maxRequestSize = 10 * 1024 * 1024     // 整个请求最大 10 MB
)
public class UploadServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 设置编码
        request.setCharacterEncoding("UTF-8");
        // 获取上传的文件
        Part part = request.getPart("avatarFile");
        // 获取原始文件名
        String originName = part.getSubmittedFileName();
        if (originName == null || originName.isEmpty()) {
            request.setAttribute("msg", "请选择图片！");
            request.getRequestDispatcher("/personal.jsp").forward(request, response);
            return;
        }
        // 取后缀
        String suffix = originName.substring(originName.lastIndexOf("."));
        // 生成唯一文件名，防止重名覆盖
        String newFileName = UUID.randomUUID() + suffix;

        // 保存路径：webapp/static/avatar
        String savePath = getServletContext().getRealPath("/static/avatar");
        File folder = new File(savePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        part.write(savePath + File.separator + newFileName);

        // 把头像路径存到 session 里的 loginUser 对象
        com.book.entity.User loginUser = (com.book.entity.User) request.getSession().getAttribute("loginUser");
        if (loginUser != null) {
            loginUser.setAvatar("static/avatar/" + newFileName);
            // 持久化头像变更到文件
            java.util.List<User> userList = (java.util.List<User>) getServletContext().getAttribute("userList");
            if (userList != null) {
                UserStore.save(getServletContext().getRealPath("/"), userList);
            }
        }

        // 【修复】原重定向路径 "/personal.jsp" 缺少 contextPath
        response.sendRedirect(request.getContextPath() + "/personal.jsp");
    }
}
