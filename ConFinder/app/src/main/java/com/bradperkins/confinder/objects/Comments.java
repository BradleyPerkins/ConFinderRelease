package com.bradperkins.confinder.objects;

// Date 1/3/19
// 
// Bradley Perkins

// AID - 1809

import java.io.Serializable;

// PerkinsBradley_CE
public class Comments implements Serializable {

    private String username;
    private String posted;
    private String message;

    public Comments(String username, String posted, String message) {
        this.username = username;
        this.posted = posted;
        this.message = message;
    }

    public Comments() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
