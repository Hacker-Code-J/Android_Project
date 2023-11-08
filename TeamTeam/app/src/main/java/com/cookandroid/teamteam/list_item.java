package com.cookandroid.teamteam;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class list_item {
    private String id;
    private String title;
    private String content;
    private String nickname;

    @ServerTimestamp
    private  Date date;

    //생성자

    // constructor
    public list_item(String id, String title, String content, String nickname) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.nickname = nickname;
    }

    // getter and setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    // tostring
    @Override
    public String toString() {
        return "list_item{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", nickname='" + nickname + '\'' +
                ", date=" + date +
                '}';
    }
}
