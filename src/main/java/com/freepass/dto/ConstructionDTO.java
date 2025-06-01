package com.freepass.dto;

public class ConstructionDTO {
    private String name;
    private String contractor;
    private String location;
    private String startDate;
    private String endDate;
    private int x;
    private int y;

    public ConstructionDTO(String name, String contractor, String location, String startDate, String endDate, int x, int y) {
        this.name = name;
        this.contractor = contractor;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.x = x;
        this.y = y;
    }
    public String getName(){
        return name;
    }
    public String getContractor(){
        return contractor;
    }
    public String getLocation(){
        return location;
    }
    public String getStartDate(){
        return startDate;
    }
    public String getEndDate(){
        return endDate;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
}
