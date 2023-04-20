package com.example.fuelfinder;

import com.google.firebase.firestore.GeoPoint;

// Class definition
public class FuelPriceModel {
    private String date; // Date of the fuel purchase

    private String time; // Time of the fuel purchase

    private double total_cost; // Total cost of the fuel purchase

    private GeoPoint fuel_station; // Location of the fuel station

    private double estimated_rate; // Estimated fuel price per gallon

    // Constructor Method
    public FuelPriceModel(String date, String time, double total_cost, double latitude, double longitude,
                          double estimated_rate) {
        this.date = date;
        this.time = time;
        this.total_cost = total_cost;
        this.fuel_station = new GeoPoint(latitude, longitude);
        this.estimated_rate = estimated_rate;
    }

    // Getter and setter methods
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

    public double getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(double total_cost) {
        this.total_cost = total_cost;
    }

    public double getEstimated_rate() {
        return estimated_rate;
    }

    public void setEstimated_rate(double estimated_rate) {
        this.estimated_rate = estimated_rate;
    }

    public GeoPoint getFuel_station() {
        return fuel_station;
    }

    public void setLocation(GeoPoint location) {
        this.fuel_station = location;
    }
}
