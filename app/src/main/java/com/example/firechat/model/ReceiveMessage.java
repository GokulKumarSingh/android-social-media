package com.example.firechat.model;

public class ReceiveMessage {
    String data = "";
    String receiveId = "";
    String time = "";
    String type = "";

    public ReceiveMessage() {
    }

    public ReceiveMessage(String data, String receiveId, String time, String type) {
        this.data = data;
        this.receiveId = receiveId;
        this.time = time;
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(String receiveId) {
        this.receiveId = receiveId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ReceiveMessage{" +
                "data='" + data + '\'' +
                ", receiveId='" + receiveId + '\'' +
                ", time='" + time + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
