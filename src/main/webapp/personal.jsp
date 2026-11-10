<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="个人中心"/>
<c:set var="pageKey" value="profile"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<c:if test="${not empty requestScope.msg}">
    <div class="alert" role="alert"><c:out value="${requestScope.msg}"/></div>
</c:if>
<c:if test="${not empty sessionScope.msg}">
    <div class="alert" role="alert"><c:out value="${sessionScope.msg}"/></div>
    <c:remove var="msg" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.msgError}">
    <div class="alert alert-error" role="alert"><c:out value="${sessionScope.msgError}"/></div>
    <c:remove var="msgError" scope="session"/>
</c:if>

<div class="profile-layout">
<section class="profile-card">
    <div class="profile-aside">
        <c:choose>
            <c:when test="${not empty loginUser.avatar}">
                <img class="profile-avatar" src="${ctx}/${loginUser.avatar}" alt="当前头像">
            </c:when>
            <c:otherwise>
                <div class="avatar-placeholder" aria-hidden="true">
                    <c:choose>
                        <c:when test="${not empty loginUser.nickname}">
                            <c:out value="${fn:substring(loginUser.nickname, 0, 1)}"/>
                        </c:when>
                        <c:otherwise>书</c:otherwise>
                    </c:choose>
                </div>
            </c:otherwise>
        </c:choose>

        <form class="avatar-form" action="${ctx}/uploadAvatar" method="post" enctype="multipart/form-data">
            <label for="avatarFile">更换头像</label>
            <input type="file" id="avatarFile" name="avatarFile" accept="image/*">
            <button type="submit" class="btn btn-outline btn-sm">上传头像</button>
            <span class="field-hint">支持 JPG / PNG，大小不超过 5MB</span>
        </form>
    </div>

    <div class="profile-main">
        <p class="eyebrow">个人中心</p>
        <div>
            <h1 class="profile-name">
                <c:choose>
                    <c:when test="${not empty loginUser.nickname}"><c:out value="${loginUser.nickname}"/></c:when>
                    <c:otherwise>读者</c:otherwise>
                </c:choose>
            </h1>
            <p class="profile-sub">欢迎回来，继续在书架上发现下一本好书。</p>
            <p class="profile-signature">
                <c:choose>
                    <c:when test="${not empty loginUser.signature}">
                        <c:out value="${loginUser.signature}"/>
                    </c:when>
                    <c:otherwise>还没有签名，去下面写一句吧。</c:otherwise>
                </c:choose>
            </p>
        </div>

        <dl class="meta-list">
            <div class="meta-row">
                <dt>用户名</dt>
                <dd><c:out value="${loginUser.username}"/></dd>
            </div>
            <div class="meta-row">
                <dt>昵称</dt>
                <dd><c:out value="${loginUser.nickname}"/></dd>
            </div>
            <div class="meta-row">
                <dt>收藏图书</dt>
                <dd>${fn:length(sessionScope.collectBooks)} 本</dd>
            </div>
        </dl>

        <div class="profile-links">
            <a class="btn btn-primary" href="${ctx}/myCollect.jsp">我的收藏</a>
            <a class="btn btn-ghost" href="${ctx}/bookList">去借书</a>
        </div>
    </div>
</section>

<section class="profile-sections">
    <div class="panel">
        <div class="panel-head">
            <h2>编辑资料</h2>
            <p class="panel-desc">修改昵称与个性签名，保存后立即生效。</p>
        </div>
        <form class="stack-form" action="${ctx}/updateProfile" method="post">
            <input type="hidden" name="action" value="profile">
            <div class="field">
                <label for="nickname">昵称</label>
                <input type="text" id="nickname" name="nickname" maxlength="20"
                       value="<c:out value='${loginUser.nickname}'/>" placeholder="最多 20 个字" required>
            </div>
            <div class="field">
                <label for="signature">个性签名</label>
                <textarea id="signature" name="signature" rows="3" maxlength="80"
                          placeholder="写一句喜欢的话，最多 80 个字"><c:out value="${loginUser.signature}"/></textarea>
            </div>
            <button class="btn btn-primary" type="submit">保存资料</button>
        </form>
    </div>

    <div class="panel">
        <div class="panel-head">
            <h2>修改密码</h2>
            <p class="panel-desc">密码长度为 6~32 位，修改后需重新登录。</p>
        </div>
        <form class="stack-form" action="${ctx}/updateProfile" method="post">
            <input type="hidden" name="action" value="password">
            <div class="field">
                <label for="oldPassword">原密码</label>
                <input type="password" id="oldPassword" name="oldPassword" autocomplete="current-password" required>
            </div>
            <div class="field">
                <label for="newPassword">新密码</label>
                <input type="password" id="newPassword" name="newPassword" minlength="6" maxlength="32"
                       autocomplete="new-password" placeholder="6~32 位" required>
            </div>
            <div class="field">
                <label for="confirmPassword">确认新密码</label>
                <input type="password" id="confirmPassword" name="confirmPassword" minlength="6" maxlength="32"
                       autocomplete="new-password" required>
            </div>
            <button class="btn btn-primary" type="submit">修改密码</button>
        </form>
    </div>

    <div class="panel panel-wide">
        <div class="panel-head">
            <h2>阅读偏好</h2>
            <p class="panel-desc">护眼模式会降低屏幕蓝光，设置保存在当前浏览器。</p>
        </div>
        <div class="pref-row">
            <div class="pref-text">
                <strong>护眼模式</strong>
                <span>开启后整个站点变为暖色调，适合夜间阅读</span>
            </div>
            <div class="pref-control">
                <span class="switch-label" id="eyeCareLabel">关闭</span>
                <button class="switch" type="button" id="eyeCareToggle"
                        role="switch" aria-checked="false" aria-label="切换护眼模式">
                    <span class="switch-track" aria-hidden="true"></span>
                </button>
            </div>
        </div>
    </div>
</section>
</div>

<script>
(function () {
    var KEY = 'eyeCareMode';
    var btn = document.getElementById('eyeCareToggle');
    var label = document.getElementById('eyeCareLabel');
    if (!btn) return;

    function isOn() {
        try { return localStorage.getItem(KEY) === '1'; } catch (e) { return false; }
    }
    function render() {
        var on = isOn();
        btn.setAttribute('aria-checked', on ? 'true' : 'false');
        if (label) label.textContent = on ? '开启' : '关闭';
    }
    btn.addEventListener('click', function () {
        try { localStorage.setItem(KEY, isOn() ? '0' : '1'); } catch (e) {}
        document.documentElement.classList.toggle('eye-care', isOn());
        render();
    });
    render();
})();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
