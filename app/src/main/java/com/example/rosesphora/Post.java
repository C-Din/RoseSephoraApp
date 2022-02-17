package com.example.rosesphora;

public class Post {

    private String post_date, text_content, pub_image, user_id;


    public Post(){

    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public String getText_content() {
        return text_content;
    }

    public void setText_content(String text_content) {
        this.text_content = text_content;
    }

    public String getPub_image() {
        return pub_image;
    }

    public void setPub_image(String pub_image) {
        this.pub_image = pub_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
