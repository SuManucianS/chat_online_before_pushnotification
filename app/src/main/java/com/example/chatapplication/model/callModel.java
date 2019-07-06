package com.example.chatapplication.model;

public class callModel {
    private String calling;
    private String receiving;

    public callModel(String calling, String receiving) {
        this.calling = calling;
        this.receiving = receiving;
    }

    public callModel() {
    }

    public String getCalling() {
        return calling;
    }

    public void setCalling(String calling) {
        this.calling = calling;
    }

    public String getReceiving() {
        return receiving;
    }

    public void setReceiving(String receiving) {
        this.receiving = receiving;
    }
}
