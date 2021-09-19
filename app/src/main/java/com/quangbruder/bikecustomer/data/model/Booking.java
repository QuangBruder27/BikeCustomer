package com.quangbruder.bikecustomer.data.model;

/**
     * A placeholder item representing a piece of content.
     */
public class Booking {

    public String id;
    public String bikeId;
    public String beginTime;
    public String endTime;
    public String distance;

    public Booking() {
    }

    public Booking(String id, String bikeId, String beginTime, String endTime, String distance) {
        this.id = id;
        this.bikeId = bikeId;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", bikeId='" + bikeId + '\'' +
                ", startTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }
}