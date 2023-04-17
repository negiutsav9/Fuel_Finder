package com.example.fuelfinder;

public class PriceDataModel {

    private String id;
    private String date;
    private String time;
    private double estimated_rate;
    private String placeID;
    private String fuel_type;

    public PriceDataModel(){}

    public PriceDataModel(String id, String date, String time, double estimated_rate, String placeID, String fuel_type) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.estimated_rate = estimated_rate;
        this.placeID = placeID;
        this.fuel_type = fuel_type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getEstimated_rate() {
        return estimated_rate;
    }

    public void setEstimated_rate(double estimated_rate) {
        this.estimated_rate = estimated_rate;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getFuel_type() {
        return fuel_type;
    }

    public void setFuel_type(String fuel_type) {
        this.fuel_type = fuel_type;
    }
}
