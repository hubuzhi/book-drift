﻿<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="书香校园图书漂流站">
    <title><c:out value="${empty pageTitle ? '图书漂流' : pageTitle}"/> - 书香校园</title>
    <link rel="stylesheet" href="${ctx}/static/css/style.css?v=20261108m">
    <script>
        // 护眼模式：在页面渲染前应用，避免闪烁
        try {
            if (localStorage.getItem('eyeCareMode') === '1') {
                document.documentElement.classList.add('eye-care');
            }
        } catch (e) { /* 忽略隐私模式等异常 */ }
    </script>
</head>
<body>
<header class="site-header">
    <div class="header-inner">
        <a class="brand" href="${ctx}/bookList" aria-label="返回图书列表">
            <span class="brand-mark" aria-hidden="true">书</span>
            <span class="brand-text">
                <strong>书香校园</strong>
                <small>图书漂流站</small>
            </span>
        </a>
        <form class="header-search" action="${ctx}/bookList" method="get" role="search">
            <input type="text" name="q" class="search-input" placeholder="搜索书名、作者..." value="<c:out value='${param.q}'/>">
            <button type="submit" class="search-btn" aria-label="搜索">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            </button>
        </form>
        <nav class="main-nav" aria-label="主导航">
            <a class="nav-link ${pageKey eq 'history' ? 'is-active' : ''}" href="${ctx}/browseHistory">浏览记录</a>
            <a class="nav-link ${pageKey eq 'list' ? 'is-active' : ''}" href="${ctx}/bookList">图书漂流</a>
            <c:choose>
                <c:when test="${not empty sessionScope.loginUser}">
                    <a class="nav-link ${pageKey eq 'collect' ? 'is-active' : ''}"
                       href="${ctx}/myCollect.jsp">我的收藏</a>
                    <a class="nav-link ${pageKey eq 'profile' ? 'is-active' : ''}"
                       href="${ctx}/personal.jsp">个人中心</a>
                    <a class="profile-link" href="${ctx}/personal.jsp">
                        <c:choose>
                            <c:when test="${not empty sessionScope.loginUser.avatar}">
                                <img class="nav-avatar" src="${ctx}/${sessionScope.loginUser.avatar}" alt="">
                            </c:when>
                            <c:otherwise>
                                <span class="nav-avatar nav-avatar-placeholder" aria-hidden="true">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.loginUser.nickname}">
                                            <c:out value="${fn:substring(sessionScope.loginUser.nickname, 0, 1)}"/>
                                        </c:when>
                                        <c:otherwise>书</c:otherwise>
                                    </c:choose>
                                </span>
                            </c:otherwise>
                        </c:choose>
                        <c:out value="${sessionScope.loginUser.nickname}"/>
                    </a>
                    <form class="inline-form" action="${ctx}/logout" method="post">
                        <button class="nav-button" type="submit">退出</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a class="nav-link ${pageKey eq 'login' ? 'is-active' : ''}" href="${ctx}/login.jsp">登录</a>
                    <a class="nav-register" href="${ctx}/register.jsp">注册</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>

<main class="page-shell">






