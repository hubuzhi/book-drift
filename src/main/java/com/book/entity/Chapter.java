package com.book.entity;

import java.io.Serializable;

/**
 * 章节实体
 * 一本书由多个章节组成，阅读页按章节展示、翻页
 */
public class Chapter implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 章节序号，从 0 开始 */
    private final int index;
    /** 章节标题，如 第一回 宴桃园豪杰三结义 */
    private final String title;
    /** 章节正文 */
    private final String content;

    public Chapter(int index, String title, String content) {
        this.index = index;
        this.title = title;
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
