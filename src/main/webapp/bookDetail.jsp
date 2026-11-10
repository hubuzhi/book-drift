<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty book}">
    <c:redirect url="/bookList"/>
</c:if>
<c:set var="pageTitle" value="${book.title}"/>
<c:set var="pageKey" value="list"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<a class="back-link" href="${ctx}/bookList">← 返回图书列表</a>

<section class="detail-card">
    <div class="detail-cover ${book.hasCover ? 'has-img' : ''}">
        <c:choose>
            <c:when test="${book.hasCover}">
                <img class="cover-img" src="${ctx}/bookCover?id=${book.id}&amp;v=${book.coverFile}" alt="图书封面">
            </c:when>
            <c:otherwise>
                <span class="cover-cat"><c:out value="${book.category}"/></span>
                <span class="cover-letter" aria-hidden="true"><c:out value="${fn:substring(book.title, 0, 1)}"/></span>
                <span class="cover-title"><c:out value="${book.title}"/></span>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="detail-main">
        <c:choose>
            <c:when test="${book.available}">
                <span class="tag tag-in">在架可借</span>
            </c:when>
            <c:otherwise>
                <span class="tag tag-out">已借出</span>
            </c:otherwise>
        </c:choose>

        <div>
            <h1 class="detail-title"><c:out value="${book.title}"/></h1>
            <p class="detail-author">作者 · <c:out value="${book.author}"/></p>
        </div>

        <div>
            <h2 class="detail-section-title">内容简介</h2>
            <p class="detail-desc">
                <c:choose>
                    <c:when test="${not empty book.description}"><c:out value="${book.description}"/></c:when>
                    <c:otherwise>暂无简介</c:otherwise>
                </c:choose>
            </p>
        </div>

        <dl class="meta-list">
            <div class="meta-row">
                <dt>图书类别</dt>
                <dd><c:out value="${book.category}"/></dd>
            </div>
            <div class="meta-row">
                <dt>漂流提供者</dt>
                <dd><c:out value="${book.ownerNickname}"/></dd>
            </div>
            <div class="meta-row">
                <dt>当前状态</dt>
                <dd><c:out value="${book.status}"/></dd>
            </div>
        </dl>

        <div class="detail-actions">
            <a href="${ctx}/readBook?id=${book.id}" class="btn btn-primary">在线阅读</a>
            <c:choose>
                <c:when test="${not empty sessionScope.loginUser}">
                    <a href="${ctx}/collect?id=${book.id}"
                       class="btn ${favorited ? 'btn-cancel' : 'btn-primary'}">
                        ${favorited ? '取消收藏' : '收藏这本书'}
                    </a>
                    <c:if test="${favorited}">
                        <a href="${ctx}/myCollect.jsp" class="btn btn-ghost">查看我的收藏</a>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/login.jsp" class="btn btn-primary">登录后收藏</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
