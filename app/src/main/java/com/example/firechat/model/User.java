package com.example.firechat.model;


public class User {
    String uuid = "";
    String name = "";
    String number = "";
    String image = "";
    int room = 0;

    public User() {
    }

    public User(String uuid, String name, String number, String image) {
        this.uuid = uuid;
        this.name = name;
        this.number = number;
        this.image = image;
    }

    public User(String uuid, String name, String number, String image, int room) {
        this.uuid = uuid;
        this.name = name;
        this.number = number;
        this.image = image;
        this.room = room;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", image='" + image + '\'' +
                ", room=" + room +
                '}';
    }
}

