<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="图书列表"/>
<c:set var="pageKey" value="list"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<%-- ===== 首屏区域：上方标语 + 左侧滚动图 + 右侧图书分类 ===== --%>
<section class="hero">
    <p class="eyebrow">共享借阅 · 让好书流动起来</p>
    <div class="hero-main">
        <div class="hero-left">
            <%-- 滚动轮播 --%>
            <div class="hero-carousel" id="heroCarousel">
                <div class="carousel-stage" id="carouselTrack">
                    <c:forEach var="book" items="${featuredList}" varStatus="status">
                        <div class="carousel-slide${status.first ? ' is-active' : ''}" data-index="${status.index}"
                             data-id="${book.id}" data-href="${ctx}/bookDetail?id=${book.id}"
                             data-title="${fn:escapeXml(book.title)}" data-author="${fn:escapeXml(book.author)}">
                            <div class="carousel-cover ${book.hasCover ? 'has-img' : ''}" data-letter="${fn:substring(book.title, 0, 1)}">
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
                        </div>
                    </c:forEach>
                </div>
                <button class="carousel-nav prev" id="carouselPrev" type="button" aria-label="上一本">
                    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path d="M15 5 8 12l7 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                </button>
                <button class="carousel-nav next" id="carouselNext" type="button" aria-label="下一本">
                    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                </button>
                <div class="carousel-caption">
                    <h3 class="carousel-book-title" id="carouselTitle"></h3>
                    <p class="carousel-book-author" id="carouselAuthor"></p>
                </div>
                <div class="carousel-dots" id="carouselDots">
                    <c:forEach var="book" items="${featuredList}" varStatus="status">
                        <button class="carousel-dot ${status.first ? 'is-active' : ''}" data-index="${status.index}" aria-label="第${status.index + 1}本"></button>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div class="hero-right">
            <%-- 图书分类在右侧，与轮播区域等高 --%>
            <div class="hero-category">
                <h3 class="hero-category-title">图书分类</h3>
                <div class="category-tags">
                    <a href="${ctx}/bookList" class="category-tag ${empty param.category ? 'is-active' : ''}">全部</a>
                    <c:forEach var="cat" items="${categoryList}">
                        <a href="${ctx}/bookList?category=${cat}" class="category-tag ${param.category eq cat ? 'is-active' : ''}"><c:out value="${cat}"/></a>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</section>

<%-- ===== 全部图书卡片 ===== --%>
<div class="section-head">
    <h2>全部图书</h2>
    <span class="count-badge">共 <c:out value="${fn:length(displayList)}"/> 本</span>
</div>

<c:choose>
    <c:when test="${empty displayList}">
        <div class="empty-state">
            <div class="empty-icon" aria-hidden="true">书</div>
            <p>书架还是空的，稍后再来看看吧。</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="book-grid">
            <c:forEach var="book" items="${displayList}">
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
                            <a class="btn btn-outline btn-sm" href="${ctx}/readBook?id=${book.id}">阅读</a>
                            <c:if test="${not empty sessionScope.loginUser}">
                                <c:choose>
                                    <c:when test="${favoritedIds.contains(book.id)}">
                                        <a class="btn btn-cancel btn-sm collect-toggle"
                                           data-id="${book.id}"
                                           href="${ctx}/collect?id=${book.id}">已收藏</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="btn btn-primary btn-sm collect-toggle"
                                           data-id="${book.id}"
                                           href="${ctx}/collect?id=${book.id}">收藏</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </div>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

<%-- ===== 椭圆循环轮播脚本 ===== --%>
<script>
(function() {
    var stage = document.getElementById('carouselTrack');
    var dots = document.querySelectorAll('.carousel-dot');
    var titleEl = document.getElementById('carouselTitle');
    var authorEl = document.getElementById('carouselAuthor');
    if (!stage || dots.length === 0) return;
    var slides = Array.prototype.slice.call(stage.querySelectorAll('.carousel-slide'));
    if (slides.length === 0) return;

    var current = 0;
    var total = slides.length;
    var timer = null;

    // 按椭圆轨道排列：中间实体，两侧依次降低透明度/清晰度
    function layout() {
        var width = stage.clientWidth || 600;
        // 中间 1 本 + 左右各 2 本
        var visible = 2;
        // 相邻两本书的水平间距，避免两侧空隙过大
        var stepX = Math.max(135, Math.min(175, width * 0.19));

        slides.forEach(function(slide, i) {
            var diff = (i - current + total) % total;
            if (diff > total / 2) diff -= total;
            var dist = Math.abs(diff);

            if (dist > visible) {
                // 循环到另一侧之前的书：完全虚化并移出视野
                slide.style.opacity = '0';
                slide.style.filter = 'none';
                slide.style.zIndex = '0';
                slide.style.pointerEvents = 'none';
                slide.style.transform = 'translate3d(' + (diff > 0 ? 1 : -1) * width * 0.6 +
                        'px, 34px, 0) scale(.65)';
                slide.classList.remove('is-active');
                return;
            }

            var t = diff / visible;                                  // -1 .. 1
            var x = diff * stepX;                                    // 水平均布，间距收窄
            var y = (1 - Math.cos(t * Math.PI / 2)) * 40;            // 两侧向下沉降
            var scale = 1.22 - 0.18 * dist;                          // 中间最大
            var opacity = 1 - 0.22 * dist;                           // 越远颜色越淡，但不模糊
            var rotY = -diff * 24;                                   // 侧向翻转朝内

            slide.style.transform = 'translate3d(' + x.toFixed(1) + 'px, ' + y.toFixed(1) +
                    'px, 0) rotateY(' + rotY + 'deg) scale(' + scale.toFixed(2) + ')';
            slide.style.opacity = Math.max(0.35, opacity).toFixed(2);
            slide.style.filter = 'none';
            slide.style.zIndex = String(60 - dist);
            slide.style.pointerEvents = 'auto';
            slide.style.cursor = 'pointer';
            slide.classList.toggle('is-active', dist === 0);
        });

        var active = slides[current];
        if (titleEl) titleEl.textContent = active.getAttribute('data-title') || '';
        if (authorEl) authorEl.textContent = active.getAttribute('data-author') || '';
    }

    function goTo(index) {
        current = (index + total) % total;
        dots.forEach(function(d, i) { d.classList.toggle('is-active', i === current); });
        layout();
    }

    function next() { goTo(current + 1); }
    function startAuto() { timer = setInterval(next, 3500); }
    function stopAuto() { clearInterval(timer); }

    dots.forEach(function(dot) {
        dot.addEventListener('click', function() {
            stopAuto();
            goTo(parseInt(this.dataset.index));
            startAuto();
        });
    });

    stage.addEventListener('mouseenter', stopAuto);
    stage.addEventListener('mouseleave', startAuto);

    // 左右翻页按钮
    var prevBtn = document.getElementById('carouselPrev');
    var nextBtn = document.getElementById('carouselNext');
    if (prevBtn) {
        prevBtn.addEventListener('click', function() {
            stopAuto();
            goTo(current - 1);
            startAuto();
        });
    }
    if (nextBtn) {
        nextBtn.addEventListener('click', function() {
            stopAuto();
            goTo(current + 1);
            startAuto();
        });
    }

    // 点击书籍进入详情页（滑动拖拽时不触发）
    var pressPoint = null;
    stage.addEventListener('pointerdown', function(e) {
        pressPoint = { x: e.clientX, y: e.clientY };
    });
    stage.addEventListener('click', function(e) {
        var slide = e.target.closest ? e.target.closest('.carousel-slide') : null;
        if (!slide) return;
        var moved = pressPoint && (Math.abs(e.clientX - pressPoint.x) > 10 || Math.abs(e.clientY - pressPoint.y) > 10);
        pressPoint = null;
        if (moved) return;
        var href = slide.getAttribute('data-href');
        if (href) window.location.href = href;
    });

    // 触摸滑动：左右滑动切换
    var touchX = null;
    stage.addEventListener('touchstart', function(e) {
        touchX = e.touches[0].clientX;
        stopAuto();
    }, { passive: true });
    stage.addEventListener('touchend', function(e) {
        if (touchX === null) return;
        var delta = e.changedTouches[0].clientX - touchX;
        if (Math.abs(delta) > 40) goTo(current + (delta < 0 ? 1 : -1));
        touchX = null;
        startAuto();
    }, { passive: true });

    var resizeTimer = null;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(layout, 120);
    });

    layout();
    startAuto();
})();
</script>

<%-- ===== 收藏异步切换：不刷新页面，保持滚动位置 ===== --%>
<script>
(function () {
    document.querySelectorAll('.collect-toggle').forEach(function (link) {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            if (link.dataset.busy === '1') return;
            link.dataset.busy = '1';

            fetch(link.getAttribute('href') + '&ajax=1', { credentials: 'same-origin' })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    var collected = !!data.collected;
                    link.textContent = collected ? '已收藏' : '收藏';
                    link.classList.toggle('btn-cancel', collected);
                    link.classList.toggle('btn-primary', !collected);
                })
                .catch(function () {
                    // 网络异常时退回普通跳转
                    window.location.href = link.getAttribute('href');
                })
                .finally(function () { link.dataset.busy = ''; });
        });
    });
})();
</script>
