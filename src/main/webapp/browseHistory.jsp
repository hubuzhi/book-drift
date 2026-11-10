<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="浏览记录"/>
<c:set var="pageKey" value="history"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="section-head">
    <h2>浏览记录</h2>
    <c:if test="${not empty historyBooks}">
        <form class="inline-form" action="${ctx}/clearHistory" method="post" style="display:inline;">
            <button class="btn btn-ghost btn-sm" type="submit">清除记录</button>
        </form>
    </c:if>
</div>

<c:choose>
    <c:when test="${empty historyBooks}">
        <div class="empty-state">
            <div class="empty-icon" aria-hidden="true">📖</div>
            <p>还没有浏览记录，去图书漂流看看吧。</p>
            <a class="btn btn-primary btn-sm" href="${ctx}/bookList">去逛逛</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="book-grid">
            <c:forEach var="book" items="${historyBooks}">
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
                            <a class="btn btn-primary btn-sm" href="${ctx}/readBook?id=${book.id}">继续阅读</a>
                            <a class="btn btn-ghost btn-sm" href="${ctx}/bookDetail?id=${book.id}">查看详情</a>
                        </div>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
