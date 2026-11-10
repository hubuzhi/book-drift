package com.book.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * 登录拦截过滤器（F6）
 * 功能：拦截「我的收藏」「个人中心」等路径，未登录跳转登录页并提示
 *
 * 实现要点：
 * 1. 采用「黑名单」方式：只拦截需要登录的路径，其余资源（首页、登录/注册、
 *    图书列表、图书详情、static 静态资源等）一律放行，避免误伤其他成员的页面；
 * 2. 登录标识统一使用 Session 中的 loginUser（由 LoginServlet 登录成功后写入，
 *    与组内约定一致）；
 * 3. 未登录时重定向到 login.jsp，并带 msg 参数提示原因。
 */
public class LoginFilter implements Filter {

    /** 需要登录才能访问的路径（精确匹配，或以「路径/」开头的子路径） */
    private static final List<String> PROTECTED_PATHS = Arrays.asList(
            "/myCollect.jsp",   // 我的收藏（页面）
            "/personal.jsp",    // 个人中心
            "/collect",         // 收藏/取消收藏（CollectServlet）
            "/uploadAvatar",    // 头像上传（UploadServlet，选做 F8）
            "/updateProfile"    // 个人资料 / 密码修改（ProfileServlet）
    );

    /** 未登录时提示的文案 */
    private static final String TIP = "请先登录后再访问";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 无需初始化参数
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 1. 取出去掉上下文路径后的请求路径
        //    例如 /book-floating/myCollect.jsp  →  /myCollect.jsp
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // 2. 不需要登录的资源，直接放行
        if (!needLogin(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 3. 已登录（Session 中有 loginUser）则放行
        //    用 getSession(false)：没有会话时不新建，避免给匿名用户白建 Session
        HttpSession session = req.getSession(false);
        Object loginUser = (session == null) ? null : session.getAttribute("loginUser");
        if (loginUser != null) {
            chain.doFilter(request, response);
            return;
        }

        // 4. 未登录：写入提示信息后跳转登录页
        //    ① 放进 Session，login.jsp 可用 ${msg} 取到（取完建议移除）
        HttpSession newSession = req.getSession(true);
        newSession.setAttribute("msg", TIP);
        //    ② 同时带在 URL 上，login.jsp 也可用 ${param.msg} 取到
        String encoded = URLEncoder.encode(TIP, "UTF-8");
        resp.sendRedirect(contextPath + "/login.jsp?msg=" + encoded);
    }

    /**
     * 判断某路径是否需要登录。
     *
     * @param path 去掉上下文路径后的请求路径
     */
    private boolean needLogin(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        for (String protectedPath : PROTECTED_PATHS) {
            // 精确匹配：/collect、/myCollect.jsp
            if (path.equals(protectedPath)) {
                return true;
            }
            // 子路径匹配：/collect/xxx
            if (path.startsWith(protectedPath + "/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // 本过滤器不占用资源，无需释放
    }
}
