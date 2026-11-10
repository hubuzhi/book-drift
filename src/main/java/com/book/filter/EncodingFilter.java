package com.book.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * 统一编码过滤器（F7）
 * 功能：全站 UTF-8 编码，解决中文乱码
 *
 * 实现要点：
 * 1. init：从 web.xml 的 init-param 读取编码，未配置时默认 UTF-8；
 * 2. doFilter：先设置请求与响应的字符集，再放行，保证请求参数在被任何
 *    Servlet 读取之前就已经按 UTF-8 解码（顺序不能颠倒）；
 * 3. web.xml 中本过滤器映射在 loginFilter 之前，是全站第一个过滤器。
 */
public class EncodingFilter implements Filter {

    /** 字符集，默认 UTF-8，可被 web.xml 的 init-param 覆盖 */
    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String param = filterConfig.getInitParameter("encoding");
        if (param != null && !param.trim().isEmpty()) {
            this.encoding = param.trim();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 1. 设置请求体编码：POST 中文参数靠它解码（必须在 getParameter 之前执行）
        request.setCharacterEncoding(encoding);
        // 2. 设置响应字符集：JSP/Servlet 输出中文不乱码
        response.setCharacterEncoding(encoding);
        // 说明：这里不设置 Content-Type，交给目标资源自己决定，
        //       否则 static 下的 css/js 会被错误地当成 text/html 返回。
        // 3. 放行，交给后续过滤器 / Servlet
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 本过滤器不占用资源，无需释放
    }
}

