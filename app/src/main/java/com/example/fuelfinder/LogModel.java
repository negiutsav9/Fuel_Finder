package com.example.fuelfinder;

import com.google.firebase.firestore.GeoPoint;

import java.sql.Time;
import java.util.Date;
import java.util.Timer;

public class LogModel {
    private String date;
    private String time;
    private double total_cost;
    private double gallons_of_fuel;
    private double estimated_rate;
    private GeoPoint fuel_station;
    private double miles_per_gallon;
    private int odometer_reading;
    private String fuel_type;


    public LogModel(String date, String time, double total_cost, double gallons_of_fuel,
                    double estimated_rate, double latitude, double longitude, double miles_per_gallon,
                    int odometer_reading, String fuel_type) {
        this.date = date;
        this.time = time;
        this.total_cost = total_cost;
        this.gallons_of_fuel = gallons_of_fuel;
        this.estimated_rate = estimated_rate;
        this.fuel_station = new GeoPoint(latitude, longitude);
        this.miles_per_gallon = miles_per_gallon;
        this.odometer_reading = odometer_reading;
        this.fuel_type = fuel_type;
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

    public double getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(double total_cost) {
        this.total_cost = total_cost;
    }

    public double getGallons_of_fuel() {
        return gallons_of_fuel;
    }

    public void setGallons_of_fuel(int gallons_of_fuel) {
        this.gallons_of_fuel = gallons_of_fuel;
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

    public double getMiles_per_gallon() {
        return miles_per_gallon;
    }

    public void setMiles_per_gallon(double miles_per_gallon) {
        this.miles_per_gallon = miles_per_gallon;
    }

    public int getOdometer_reading() {
        return odometer_reading;
    }

    public void setOdometer_reading(int odometer_reading) {
        this.odometer_reading = odometer_reading;
    }

    public String getFuel_type() {
        return fuel_type;
    }

    public void setFuel_type(String fuel_type) {
        this.fuel_type = fuel_type;
    }
}