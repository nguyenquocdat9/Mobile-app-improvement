package com.example.myapplication.data.Model.Statistic;

public class PropertyStatisticDetails {
    private String main_img_url;
    private String name;
    private double averagePowerByOneRoom;
    private int timeUsedPerMonthByOneRoom;

    public PropertyStatisticDetails(String main_img_url, String name, double averagePowerByOneRoom, int timeUsedPerMonthByOneRoom) {
        this.main_img_url = main_img_url;
        this.name = name;
        this.averagePowerByOneRoom = averagePowerByOneRoom;
        this.timeUsedPerMonthByOneRoom = timeUsedPerMonthByOneRoom;
    }

    public PropertyStatisticDetails() {
        this.main_img_url = "";
        this.name = "";
        this.averagePowerByOneRoom = 0;
        this.timeUsedPerMonthByOneRoom = 0;
    }

    public String getMain_img_url() {
        return main_img_url;
    }

    public String getName() {
        return name;
    }

    public double getAveragePowerByOneRoom() {
        return averagePowerByOneRoom;
    }

    public int getTimeUsedPerMonthByOneRoom() {
        return timeUsedPerMonthByOneRoom;
    }

    public void setMain_img_url(String main_img_url) {
        this.main_img_url = main_img_url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAveragePowerByOneRoom(double averagePowerByOneRoom) {
        this.averagePowerByOneRoom = averagePowerByOneRoom;
    }

    public void setTimeUsedPerMonthByOneRoom(int timeUsedPerMonthByOneRoom) {
        this.timeUsedPerMonthByOneRoom = timeUsedPerMonthByOneRoom;
    }
}
