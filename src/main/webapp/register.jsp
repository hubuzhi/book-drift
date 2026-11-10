<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="用户注册"/>
<c:set var="pageKey" value="register"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<section class="auth-shell">
    <div class="auth-card">
        <div class="auth-head">
            <span class="brand-mark" aria-hidden="true">书</span>
            <h1>加入图书漂流</h1>
            <p>注册一个账号，把你的书分享给更多读者</p>
        </div>

        <c:if test="${not empty requestScope.msg}">
            <div class="alert alert-error" role="alert"><c:out value="${requestScope.msg}"/></div>
        </c:if>

        <form class="stack-form" action="${ctx}/register" method="post"
              onsubmit="return validateRegisterForm()">
            <div class="field">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username" autocomplete="username"
                       placeholder="用于登录，建议使用英文或数字">
            </div>
            <div class="field">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" autocomplete="new-password"
                       placeholder="至少 6 位">
                <span class="field-hint">密码长度不少于 6 位</span>
            </div>
            <div class="field">
                <label for="nickname">昵称</label>
                <input type="text" id="nickname" name="nickname" placeholder="展示给其他读者的名字">
            </div>
            <button class="btn btn-primary btn-block" type="submit">创建账号</button>
        </form>

        <p class="auth-foot">已有账号？<a href="${ctx}/login.jsp">去登录</a></p>
    </div>
</section>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
