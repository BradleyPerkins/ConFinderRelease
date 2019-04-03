package com.bradperkins.confinder.objects;

// Date 1/12/19
// 
// Bradley Perkins

// AID - 1809

// PerkinsBradley_CE
public class NotificationCons {

    private String title;
    private int pos;
    private String image;

    public NotificationCons(String title, int pos, String image) {
        this.title = title;
        this.pos = pos;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
