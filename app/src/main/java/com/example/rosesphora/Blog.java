package com.example.rosesphora;

public class Blog {

    private String post_date, blog_title, blog_content, author, blog_link;

    public Blog(){

    }

    public Blog(String post_date, String blog_title, String blog_content, String author, String blog_link) {
        this.post_date = post_date;
        this.blog_title = blog_title;
        this.blog_content = blog_content;
        this.author = author;
        this.blog_link = blog_link;
    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public String getBlog_title() {
        return blog_title;
    }

    public void setBlog_title(String blog_title) {
        this.blog_title = blog_title;
    }

    public String getBlog_content() {
        return blog_content;
    }

    public void setBlog_content(String blog_content) {
        this.blog_content = blog_content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBlog_link() {
        return blog_link;
    }

    public void setBlog_link(String blog_link) {
        this.blog_link = blog_link;
    }
}
