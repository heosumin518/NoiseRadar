package com.freepass.dto;

public class ConstructionDTO {
    private String name;
    private String contractor;
    private String location;
    private String startDate;
    private String endDate;
    private int x; // 픽셀 X 좌표
    private int y; // 픽셀 Y 좌표
    private double latitude; // GPS 위도
    private double longitude; // GPS 경도

    public ConstructionDTO(String name, String contractor, String location, String startDate, String endDate, int x, int y) {
        this.name = name;
        this.contractor = contractor;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.x = x;
        this.y = y;
    }

    // 복사 생성자 (안전한 객체 복사를 위해)
    public ConstructionDTO(ConstructionDTO other) {
        this.name = other.name;
        this.contractor = other.contractor;
        this.location = other.location;
        this.startDate = other.startDate;
        this.endDate = other.endDate;
        this.x = other.x;
        this.y = other.y;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
    }

    // Getter methods
    public String getName() { return name; }
    public String getContractor() { return contractor; }
    public String getLocation() { return location; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getX() { return x; }
    public int getY() { return y; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setter methods
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}