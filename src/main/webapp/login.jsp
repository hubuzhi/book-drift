<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="用户登录"/>
<c:set var="pageKey" value="login"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<section class="auth-shell">
    <div class="auth-card">
        <div class="auth-head">
            <span class="brand-mark" aria-hidden="true">书</span>
            <h1>欢迎回来</h1>
            <p>登录后即可收藏图书，管理自己的漂流书架</p>
        </div>

        <%-- 用 Cookie 展示「您上次登录时间」 --%>
        <%
            String lastTime = "";
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("lastLoginTime".equals(c.getName())) {
                        lastTime = java.net.URLDecoder.decode(c.getValue(), "UTF-8");
                    }
                }
            }
            if (!lastTime.isEmpty()) {
                out.print("<p class='last-login'>上次登录时间：" + lastTime + "</p>");
            }
        %>

        <c:set var="msgText" value="${not empty requestScope.msg ? requestScope.msg : sessionScope.msg}"/>
        <c:if test="${not empty msgText}">
            <div class="alert" role="alert"><c:out value="${msgText}"/></div>
            <c:remove var="msg" scope="session"/>
        </c:if>

        <form class="stack-form" action="${ctx}/login" method="post">
            <div class="field">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username" autocomplete="username"
                       placeholder="请输入用户名" required>
            </div>
            <div class="field">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" autocomplete="current-password"
                       placeholder="请输入密码" required>
            </div>
            <button class="btn btn-primary btn-block" type="submit">登录</button>
        </form>

        <p class="auth-foot">还没有账号？<a href="${ctx}/register.jsp">立即注册</a></p>
    </div>
</section>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
