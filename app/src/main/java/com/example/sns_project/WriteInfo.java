package com.example.sns_project;

import java.util.ArrayList;
import java.util.Date;

public class WriteInfo {

    private String title;
    private ArrayList<String> contents;
    private String publisher;
    private Date created;

    public WriteInfo(String title, ArrayList<String> contents, String publisher, Date creatAt) {
        this.title = title;
        this.contents = contents;
        this.publisher = publisher;
        this.created = creatAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getContents() {
        return contents;
    }

    public void setContents(ArrayList<String> contents) {
        this.contents = contents;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
