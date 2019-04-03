package com.bradperkins.confinder.objects;

// Date 1/2/19
// 
// Bradley Perkins

// AID - 1809

// PerkinsBradley_CE
public class ConKey {

    private String title;
    private String pos;

    public ConKey(String title, String pos) {
        this.title = title;
        this.pos = pos;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
}
