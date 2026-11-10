package com.book.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类（骨架）
 * 对应功能：F1 注册、F2 登录
 * 字段：用户名 username、密码 password、昵称 nickname、头像 avatar（选做）
 * 【待填写】补充字段与 getter / setter 方法
 */
public class User {

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String signature;
    /** 该用户收藏的图书 ID，持久化到用户文件，避免退出登录后丢失 */
    private List<Long> collectIds = new ArrayList<>();
    /** 浏览记录（图书 ID，最新在前），同样持久化 */
    private List<Long> historyIds = new ArrayList<>();
    public User() {
    }
    public User(String username, String password, String nickname, String avatar) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }
    public List<Long> getCollectIds() {
        return collectIds;
    }
    public void setCollectIds(List<Long> collectIds) {
        this.collectIds = collectIds != null ? collectIds : new ArrayList<Long>();
    }
    public List<Long> getHistoryIds() {
        return historyIds;
    }
    public void setHistoryIds(List<Long> historyIds) {
        this.historyIds = historyIds != null ? historyIds : new ArrayList<Long>();
    }
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
