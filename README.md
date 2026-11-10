# 图书漂流 · 共享借阅平台

一个基于 **Servlet + JSP** 的 Java Web 项目，实现图书浏览、检索、在线阅读、收藏与个人中心等功能，
内置 50 本经典书籍文本，支持头像上传与浏览历史记录。

> 本项目为**团队课程项目（5 人协作）**，本人在其中负责**过滤器层**的实现与联调。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Jakarta Servlet 6.0、JSP、JSTL |
| 容器 | Apache Tomcat 10.1 |
| 构建 | Maven（打包 WAR） |
| 前端 | JSP + JSTL + 原生 CSS/JS（服务端渲染） |
| 数据 | 内存 + 文件存储（`WEB-INF/books` 内置书籍文本） |

## 功能模块

- **图书列表**：分页展示、按分类/关键词筛选
- **图书详情**：封面、简介、章节列表
- **在线阅读**：章节内容展示与翻页
- **我的收藏**：收藏 / 取消收藏
- **个人中心**：资料查看与修改、头像上传
- **浏览历史**：记录与清空
- **在线人数统计**：基于 `HttpSessionListener`

## 本人负责部分：过滤器层

项目采用「一个请求多道过滤」的设计，本人实现并调试了两个全局过滤器：

### 1. 统一编码过滤器 `EncodingFilter`

- 在 `init()` 中读取 `web.xml` 的 `init-param` 编码配置，未配置时默认 UTF-8；
- 在参数解析**之前**调用 `request.setCharacterEncoding()`，避免 POST 中文参数被按 ISO-8859-1 解码导致乱码；
- 同时设置 `response.setCharacterEncoding()`，保证 JSP/Servlet 输出中文正常；
- 刻意**不覆盖 Content-Type**，防止 `static/css`、`static/js` 等静态资源被错误地当成 `text/html` 返回。

### 2. 登录拦截过滤器 `LoginFilter`

- 采用**路径黑名单**匹配：`/myCollect.jsp`、`/personal.jsp`、`/collect`、`/upload`；
- 用 `getSession(false)` 取会话（不新建），检查 Session 中是否存在 `loginUser`；
- 未登录时重定向到登录页，并通过 URL 参数与 Session 双重携带提示信息；
- 已登录用户直接放行，不影响公开页面（首页、列表、详情、静态资源）。

## 目录结构

```
src/main/
├── java/com/book/
│   ├── entity/       实体类（Book、Chapter、User）
│   ├── filter/       过滤器（EncodingFilter、LoginFilter）  ← 本人负责
│   ├── listener/     监听器（初始化数据、在线人数）
│   ├── servlet/      业务 Servlet（列表/详情/阅读/收藏/登录/注册…）
│   └── util/         工具类（书籍解析、抓取、用户存储）
└── webapp/
    ├── *.jsp         页面
    ├── static/       样式与脚本
    └── WEB-INF/      配置、内置书籍文本
```

## 本地运行

```bash
# 1. 打包
mvn clean package

# 2. 部署
# 将 target/book-floating.war 放入 Tomcat 10.1 的 webapps 目录

# 3. 访问
http://localhost:8080/book-floating/
```

> 需要 **JDK 11+** 与 **Tomcat 10.1**（Jakarta 命名空间，非 javax）。
