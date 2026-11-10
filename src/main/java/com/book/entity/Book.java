package com.book.entity;

import com.book.util.TxtBookParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private final String title;
    private final String author;
    private final String category;
    /** 简介会在启动后被豆瓣真实简介替换，因此不能是 final */
    private volatile String description;
    private final boolean available;
    private final String ownerNickname;
    private final List<Chapter> chapters;
    /** 缓存的封面文件名（如 1.jpg），为空表示仍使用 CSS 文字封面 */
    private volatile String coverFile;

    public Book(long id, String title, String author, String category,
                String description, boolean available, String ownerNickname, List<Chapter> chapters) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.description = description;
        this.available = available;
        this.ownerNickname = ownerNickname;
        this.chapters = chapters != null ? chapters : new ArrayList<>();
    }

    public Book(long id, String title, String author, String category,
                String description, boolean available, String ownerNickname) {
        this(id, title, author, category, description, available, ownerNickname, new ArrayList<>());
    }

    /**
     * 传入全文字符串的构造：内部自动按章节标题切分成 chapters
     */
    public Book(long id, String title, String author, String category,
                String description, boolean available, String ownerNickname, String contentText) {
        this(id, title, author, category, description, available, ownerNickname,
                TxtBookParser.parseText(contentText));
    }

    public Book(long id, String title, String author, String category) {
        this(id, title, author, category, "", true, "", new ArrayList<>());
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverFile() {
        return coverFile;
    }

    public void setCoverFile(String coverFile) {
        this.coverFile = coverFile;
    }

    /** 是否已有真实封面图片（对应 JSP 中的 hasCover 属性） */
    public boolean isHasCover() {
        return coverFile != null && !coverFile.isEmpty();
    }

    public boolean isAvailable() {
        return available;
    }

    public String getStatus() {
        return available ? "在架" : "已借出";
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public int getChapterCount() {
        return chapters.size();
    }
}
