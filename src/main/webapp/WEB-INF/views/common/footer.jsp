<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
</main>
<footer class="site-footer">
    <div class="footer-inner">
        <p>让每一本书，在校园里遇见下一位读者。</p>
        <p class="online-status">
            <span class="status-dot" aria-hidden="true"></span>
            当前在线 <strong><c:out value="${applicationScope.onlineCount}"/></strong> 人
        </p>
    </div>
</footer>
<script src="${ctx}/static/js/validate.js" defer></script>
</body>
</html>
