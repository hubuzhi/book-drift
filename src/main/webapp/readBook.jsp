<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty book}">
    <c:redirect url="/bookList"/>
</c:if>
<c:set var="pageTitle" value="阅读 - ${book.title}"/>
<c:set var="pageKey" value="list"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="reader-wrapper">
    <div class="reader-toolbar">
        <a class="back-link" href="${ctx}/bookDetail?id=${book.id}">← 返回详情</a>
        <div class="reader-controls">
            <button class="reader-btn" onclick="toggleSidebar()" title="章节目录">☰</button>
            <button class="reader-btn" onclick="changeFontSize(-1)" title="缩小字体">A-</button>
            <button class="reader-btn" onclick="changeFontSize(1)" title="放大字体">A+</button>
            <button class="reader-btn" onclick="toggleTheme()" title="切换主题">◐</button>
        </div>
    </div>

    <div class="reader-main">
        <%-- 章节目录侧栏 --%>
        <aside class="reader-sidebar" id="readerSidebar">
            <h3 class="sidebar-title">目录（共 ${chapterCount} 章）</h3>
            <ul class="chapter-list">
                <c:forEach var="ch" items="${chapters}">
                    <li>
                        <a class="chapter-item ${ch.index eq currentChapter ? 'is-active' : ''}"
                           href="${ctx}/readBook?id=${book.id}&chapter=${ch.index}">
                            <c:out value="${ch.title}"/>
                        </a>
                    </li>
                </c:forEach>
            </ul>
        </aside>

        <%-- 正文区域 --%>
        <article class="reader-content" id="readerContent">
            <header class="reader-header">
                <h1 class="reader-title"><c:out value="${book.title}"/></h1>
                <p class="reader-meta"><c:out value="${book.author}"/> · <c:out value="${book.category}"/></p>
            </header>

            <c:choose>
                <c:when test="${chapterCount > 0}">
                    <h2 class="chapter-title"><c:out value="${chapters[currentChapter].title}"/></h2>
                    <div class="reader-body">
                        <div class="reader-text"><c:out value="${chapters[currentChapter].content}"/></div>
                    </div>
                    <nav class="reader-nav">
                        <c:choose>
                            <c:when test="${currentChapter > 0}">
                                <a class="btn btn-ghost" href="${ctx}/readBook?id=${book.id}&chapter=${currentChapter - 1}">← 上一章</a>
                            </c:when>
                            <c:otherwise><span class="nav-disabled">已是第一章</span></c:otherwise>
                        </c:choose>
                        <span class="nav-progress">${currentChapter + 1} / ${chapterCount}</span>
                        <c:choose>
                            <c:when test="${currentChapter < chapterCount - 1}">
                                <a class="btn btn-primary" href="${ctx}/readBook?id=${book.id}&chapter=${currentChapter + 1}">下一章 →</a>
                            </c:when>
                            <c:otherwise><span class="nav-disabled">已是最后一章</span></c:otherwise>
                        </c:choose>
                    </nav>
                </c:when>
                <c:otherwise>
                    <p class="reader-paragraph reader-empty">该书暂无阅读内容。</p>
                </c:otherwise>
            </c:choose>
        </article>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

<script>
(function() {
    var fontSize = 18;
    var isDark = false;

    window.changeFontSize = function(delta) {
        fontSize = Math.max(14, Math.min(28, fontSize + delta));
        document.getElementById('readerContent').style.fontSize = fontSize + 'px';
    };

    window.toggleTheme = function() {
        isDark = !isDark;
        var wrapper = document.querySelector('.reader-wrapper');
        if (isDark) {
            wrapper.classList.add('reader-dark');
        } else {
            wrapper.classList.remove('reader-dark');
        }
    };

    window.toggleSidebar = function() {
        document.getElementById('readerSidebar').classList.toggle('is-collapsed');
    };
})();
</script>
