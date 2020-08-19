package com.iiatimd.portfolioappv2.Entities;

import com.squareup.moshi.Json;

public class User {

    @Json(name = "id")
    int id;
    @Json(name = "name")
    String name;
    @Json(name = "lastname")
    String lastname;
    @Json(name = "photo")
    String photo;
    @Json(name = "email")
    String email;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
