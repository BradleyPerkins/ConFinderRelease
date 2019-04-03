package com.bradperkins.confinder.objects;

// Date 12/4/18
// 
// Bradley Perkins

// AID - 1809

import java.io.Serializable;

// PerkinsBradley_CE
public class ConReports implements Serializable {

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String address;
    private String building;
    private String city;
    private String date;
    private String hours1;
    private String hours2;
    private String hours3;
    private String hours4;
    private double id;
    private String image;
    private double latitude;
    private double longitude;
    private String state;
    private String tickets;
    private String title;
    private String url;
    private double zip;
    private double distance;
    private int likes;
    private int attending;
    private String admin;
    private String report;
    private String conid;


    public ConReports(String address, String building, String city, String date, String hours1,
                      String hours2, String hours3, String hours4, double id, String image,
                      double latitude, double longitude, String state, String tickets, String title,
                      String url, double zip, double distance, int likes, String admin, int attending, String report, String conid) {

        this.address = address;
        this.building = building;
        this.city = city;
        this.date = date;
        this.hours1 = hours1;
        this.hours2 = hours2;
        this.hours3 = hours3;
        this.hours4 = hours4;
        this.id = id;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.tickets = tickets;
        this.title = title;
        this.url = url;
        this.zip = zip;
        this.distance = distance;
        this.likes = likes;
        this.admin = admin;
        this.attending = attending;
        this.report = report;
        this.conid = conid;

    }


    public ConReports() {

    }


    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    public String getBuilding() {
        return building;
    }

    public String getCity() {
        return city;
    }

    public String getHours1() {
        return hours1;
    }

    public String getHours2() {
        return hours2;
    }

    public String getHours3() {
        return hours3;
    }

    public String getHours4() {
        return hours4;
    }

    public double getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getState() {
        return state;
    }

    public String getTickets() {
        return tickets;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public double getZip() {
        return zip;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public int getAttending() {
        return attending;
    }

    public void setAttending(int attending) {
        this.attending = attending;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }



    public String getConid() {
        return conid;
    }

    public void setConid(String conid) {
        this.conid = conid;
    }

}


