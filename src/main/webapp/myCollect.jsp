<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="我的收藏"/>
<c:set var="pageKey" value="collect"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<c:choose>
    <c:when test="${empty sessionScope.loginUser}">
        <div class="empty-state">
            <div class="empty-icon" aria-hidden="true">书</div>
            <p>登录后才能查看自己的收藏书架。</p>
            <a class="btn btn-primary" href="${ctx}/login.jsp">去登录</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="section-head page-head">
            <div>
                <p class="eyebrow">我的书架</p>
                <h1>我的收藏</h1>
            </div>
            <span class="count-badge">共 <c:out value="${fn:length(sessionScope.collectBooks)}"/> 本</span>
        </div>

        <c:choose>
            <c:when test="${empty sessionScope.collectBooks}">
                <div class="empty-state">
                    <div class="empty-icon" aria-hidden="true">书</div>
                    <p>还没有收藏任何图书，去书架上挑一本吧。</p>
                    <a class="btn btn-primary" href="${ctx}/bookList">前往图书列表</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="book-grid">
                    <c:forEach items="${sessionScope.collectBooks}" var="book">
                        <article class="book-card">
                            <div class="book-cover ${book.hasCover ? 'has-img' : ''}" data-letter="${fn:substring(book.title, 0, 1)}">
                                <c:choose>
                                    <c:when test="${book.hasCover}">
                                        <img class="cover-img" src="${ctx}/bookCover?id=${book.id}&amp;v=${book.coverFile}" alt="图书封面" loading="lazy">
                                    </c:when>
                                    <c:otherwise>
                                        <span class="cover-cat"><c:out value="${book.category}"/></span>
                                        <span class="cover-title"><c:out value="${book.title}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="book-body">
                                <div class="book-head">
                                    <h3 class="book-title">
                                        <a href="${ctx}/bookDetail?id=${book.id}"><c:out value="${book.title}"/></a>
                                    </h3>
                                    <c:choose>
                                        <c:when test="${book.available}">
                                            <span class="tag tag-in">在架</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="tag tag-out">已借出</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <p class="book-author"><c:out value="${book.author}"/></p>
                                <div class="book-actions">
                                    <a class="btn btn-ghost btn-sm" href="${ctx}/bookDetail?id=${book.id}">查看详情</a>
                                    <a class="btn btn-cancel btn-sm" href="${ctx}/collect?id=${book.id}">取消收藏</a>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
