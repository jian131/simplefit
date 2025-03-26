package com.jian.simplefit.model.user;

import java.util.Date;

public class User {
    private String id;
    private String name;
    private String email;
    private double weight;
    private double height;
    private String gender;
    private Date dateOfBirth;
    private String profileImageUrl;
    private Date dateCreated;

    public User() {
        this.dateCreated = new Date();
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.dateCreated = new Date();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Date getDateCreated() { return dateCreated; }
}