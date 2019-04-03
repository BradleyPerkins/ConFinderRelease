package com.bradperkins.confinder.objects;

// Date 12/10/18
// 
// Bradley Perkins

// AID - 1809

// PerkinsBradley_CE
public class User {

    private String displayName;
    private String password;
    private String email;

    public User(String username, String password, String email) {
        this.displayName = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return displayName;
    }

    public void setUsername(String username) {
        this.displayName = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
