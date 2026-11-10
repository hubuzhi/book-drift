package com.book.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * 在线人数统计监听器
 * 只统计已登录用户：loginUser 属性加入 Session 时 +1，Session 销毁或 loginUser 移除时 -1
 */
public class OnlineCountListener implements HttpSessionListener, HttpSessionAttributeListener {

    private static final String LOGIN_USER_KEY = "loginUser";

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        // 登录成功：loginUser 被放入 Session
        if (LOGIN_USER_KEY.equals(event.getName())) {
            changeCount(event.getSession().getServletContext(), 1);
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        // 手动移除 loginUser（一般不会走到，退出是 invalidate）
        if (LOGIN_USER_KEY.equals(event.getName())) {
            changeCount(event.getSession().getServletContext(), -1);
        }
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        // 同一 Session 重复登录（替换 loginUser），不改变计数
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // 未登录的 Session 创建不计数
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // Session 销毁时，如果里面有登录用户，则计数 -1
        HttpSession session = se.getSession();
        try {
            if (session.getAttribute(LOGIN_USER_KEY) != null) {
                changeCount(session.getServletContext(), -1);
            }
        } catch (IllegalStateException e) {
            // Session 已失效则忽略
        }
    }

    private void changeCount(ServletContext context, int delta) {
        synchronized (context) {
            Integer count = (Integer) context.getAttribute("onlineCount");
            if (count == null) {
                count = 0;
            }
            int next = count + delta;
            if (next < 0) {
                next = 0;
            }
            context.setAttribute("onlineCount", next);
        }
    }
}
