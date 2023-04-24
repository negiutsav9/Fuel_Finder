package com.example.fuelfinder;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;

//define model class for each log entry in the app
public class LogModel {
    private String id;                  // unique id for log entry
    private String date;                // date of the log entry
    private String time;                // time of log entry
    private double total_cost;          // total cost of fuel purchase
    private double gallons_of_fuel;     // amount of fuel purchased in gallons
    private double estimated_rate;      //estimated cost per gallon of fuel
    private double miles_per_gallon;    // miles per gallon for the vehicle
    private double odometer_reading;    // odometer reading for the vehicle at time of log entry
    private String fuel_type;           // type of fuel purchased
    private boolean showMenu = false;   // whether the log entry menu is currently visible
    private String placeID;             // place ID of the fuel station where the purchase was made


    // default constructor required for Firebase
    public LogModel(){}

    // constructor to create a new og entry object
    public LogModel(String id, String date, String time, double total_cost, double gallons_of_fuel,
                    double estimated_rate, String placeID, double miles_per_gallon,
                     double odometer_reading, String fuel_type, boolean showMenu) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.total_cost = total_cost;
        this.gallons_of_fuel = gallons_of_fuel;
        this.estimated_rate = estimated_rate;
        this.placeID = placeID;
        this.miles_per_gallon = miles_per_gallon;
        this.odometer_reading = odometer_reading;
        this.fuel_type = fuel_type;
        this.showMenu = showMenu;
    }

    // define getter and setter methods for each attribute of the log entry
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getGallons_of_fuel() {
        return gallons_of_fuel;
    }

    public void setGallons_of_fuel(double gallons_of_fuel) {
        this.gallons_of_fuel = gallons_of_fuel;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public boolean isShowMenu() {
        return showMenu;
    }

    public void setShowMenu(boolean showMenu) {
        this.showMenu = showMenu;
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

    public double getEstimated_rate() {
        return estimated_rate;
    }

    public void setEstimated_rate(double estimated_rate) {
        this.estimated_rate = estimated_rate;
    }

    public double getMiles_per_gallon() {
        return miles_per_gallon;
    }

    public void setMiles_per_gallon(double miles_per_gallon) {
        this.miles_per_gallon = miles_per_gallon;
    }

    public double getOdometer_reading() {
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

    public HashMap<String, Object> convertToHashMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", getId());
        map.put("date", getDate());
        map.put("time", getTime());
        map.put("total_cost", getTotal_cost());
        map.put("gallons_of_fuel", getGallons_of_fuel());
        map.put("odometer_reading", getOdometer_reading());
        map.put("estimated_rate", getEstimated_rate());
        map.put("miles_per_gallon",getMiles_per_gallon());
        map.put("fuel_type",getFuel_type());
        map.put("placeID",getPlaceID());
        return map;
    }
}