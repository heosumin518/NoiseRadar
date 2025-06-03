package com.freepass.dto;

public class ConstructionDTO {
    private String name;
    private String contractor;
    private String location;
    private String startDate;
    private String endDate;
    private int x;
    private int y;

    public ConstructionDTO(String name, String contractor, String location, 
                          String startDate, String endDate, int x, int y) {
        this.name = name;
        this.contractor = contractor;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.x = x;
        this.y = y;
    }

    // Getters
    public String getName() { return name; }
    public String getContractor() { return contractor; }
    public String getLocation() { return location; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getX() { return x; }
    public int getY() { return y; }

    // Setters (위치 재계산을 위해 추가)
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    
    public void setName(String name) { this.name = name; }
    public void setContractor(String contractor) { this.contractor = contractor; }
    public void setLocation(String location) { this.location = location; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}