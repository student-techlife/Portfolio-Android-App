package com.iiatimd.portfolioappv2.Entities;

// NOTE: variables kunnen het beste overeenkomen met de database
public class Project {
    private int id,hours;
    private String created_at,desc,photo,name,website,client,completion_date;
    private User user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAantalUur() { return hours; }

    public void setAantalUur(int aantalUur) { this.hours = aantalUur; }

    public String getDate() {
        return created_at;
    }

    public void setDate(String date) {
        this.created_at = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getProjectName() { return name; }

    public void setProjectName(String name) { this.name = name; }

    public String getWebsite() { return website; }

    public void setWebsite(String website) { this.website = website; }

    public String getOpdrachtgever() { return client; }

    public void setOpdrachtgever(String opdrachtgever) { this.client = opdrachtgever; }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDatumOplerving() {
        return completion_date;
    }

    public void setDatumOplevering(String datumOplevering) {
        this.completion_date = datumOplevering;
    }
}
