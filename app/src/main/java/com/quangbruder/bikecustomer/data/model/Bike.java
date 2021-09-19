package com.quangbruder.bikecustomer.data.model;


public class Bike {

    private String bikeId;

    private String latitude;

    private String longtitude;

    public Bike(){};

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    @Override
    public String toString() {
        return "Bike{" +
                "bikeId='" + bikeId + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longtitude='" + longtitude + '\'' +
                '}';
    }
}
