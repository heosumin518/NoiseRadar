package com.freepass.controller;

public class Construction {
    public String name;
    public String contractor;
    public String location;
    public String startDate;
    public String endDate;
    public int x;
    public int y;

    public Construction(String name, String contractor, String location, String startDate, String endDate, int x, int y) {
        this.name = name;
        this.contractor = contractor;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.x = x;
        this.y = y;
    }
}
