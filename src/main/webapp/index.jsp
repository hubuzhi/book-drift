<%@ page contentType="text/html;charset=UTF-8" %>
<%-- 欢迎页：访问项目根路径时进入此页 --%>
<%-- 【待填写】自动跳转到图书列表（/bookList） --%>
<%
    // 访问项目根路径，自动跳转到图书列表页
    response.sendRedirect(request.getContextPath() + "/bookList");
%>