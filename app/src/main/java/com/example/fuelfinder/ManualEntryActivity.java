package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class ManualEntryActivity extends AppCompatActivity {

    private final String TAG = "Info";
    private DatePickerDialog datePickerDialog;
    private Button dateButton;
    private Button timeButton;
    String date;
    int hours, minutes;
    String time;
    double longitude, latitude;
    double total_cost, fuel_refill, odometer, fuel_eco;
    String fuel_type;

    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);


        //Setting up action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_manualentry);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        getSupportActionBar().setElevation(0);

        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        // Initialize the SDK
        Places.initialize(getApplicationContext(), "AIzaSyBEP24tbWMHcUY75yXzCBySAGKXF2ZoJ8A");

        // Create a new PlacesClient instance
        //PlacesClient placesClient = Places.createClient(this);

        ImageView backButton = getSupportActionBar().getCustomView().findViewById(R.id.BackButton);
        backButton.setOnClickListener((View v) -> startActivity(new Intent(getApplicationContext(), AddLogActivity.class)));

        //UI Instantiations
        dateButton = findViewById(R.id.datePickerButton);
        timeButton = findViewById(R.id.timePickerButton);
        Button review = findViewById(R.id.review_button);
        EditText cost_edit = findViewById(R.id.cost);
        EditText fuel_refill_edit = findViewById(R.id.refill);
        EditText fuel_type_edit = findViewById(R.id.type);
        EditText odometer_edit = findViewById(R.id.odometer);
        EditText fuel_eco_edit = findViewById(R.id.fuel_eco);


        //Date Picker Code
        initDatePicker();
        dateButton.setText(getTodayDate());
        dateButton.setOnClickListener((View v) -> {
            datePickerDialog.show();
            datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.teal_200));
            datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.orange_red));
        });
        date = dateButton.getText().toString();

        //Time Picker Code
        Calendar now = Calendar.getInstance();
        timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)));
        timeButton.setOnClickListener((View v) -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHours, int selectedMinutes) {
                    hours = selectedHours;
                    minutes = selectedMinutes;
                    timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hours, minutes));
                }
            };

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hours, minutes, true);
            timePickerDialog.setTitle("Select Time");
            timePickerDialog.show();
            timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.teal_200));
            timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.orange_red));
        });
        time = timeButton.getText().toString();

        //Places AutoComplete View
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setCountry("US");
        autocompleteFragment.setTypesFilter(Arrays.asList("gas_station"));
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
                longitude = place.getLatLng().longitude;
                latitude = place.getLatLng().latitude;
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Get the current location using LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // If location is not null, use it as the default value for the AutocompleteSupportFragment
//       // if (location != null) {
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            Log.d(TAG, "Current location: " + latitude + ", " + longitude);
//            String defaultLocation = latitude + "," + longitude;
//            autocompleteFragment.setText(defaultLocation);
//        }

        //On Clicking Review
        review.setOnClickListener((View v) -> {
            Intent reviewIntent = new Intent(getApplicationContext(), ReviewActivity.class);
            if(!cost_edit.getText().toString().trim().equals("")){
                total_cost = Double.parseDouble(cost_edit.getText().toString().trim());
            } else {
                total_cost = 0;
            }
            if(!fuel_refill_edit.getText().toString().trim().equals("")){
                fuel_refill = Double.parseDouble(fuel_refill_edit.getText().toString().trim());
            }else{
                fuel_refill = 0;
            }
            fuel_type = fuel_type_edit.getText().toString().trim();
            if(!odometer_edit.getText().toString().trim().equals("")){
                odometer = Double.parseDouble(odometer_edit.getText().toString().trim());
            }else{
                odometer = 0;
            }
            if(!fuel_eco_edit.getText().toString().trim().equals("")){
                fuel_eco = Double.parseDouble(fuel_eco_edit.getText().toString().trim());
            }else{
                fuel_eco = 0;
            }

            reviewIntent.putExtra("Date", date);
            reviewIntent.putExtra("Time", time);
            reviewIntent.putExtra("Longitude", longitude);
            reviewIntent.putExtra("Latitude", latitude);
            reviewIntent.putExtra("Cost", total_cost);
            reviewIntent.putExtra("Capacity", fuel_refill);
            reviewIntent.putExtra("Type", fuel_type);
            reviewIntent.putExtra("Odometer", odometer);
            reviewIntent.putExtra("Economy", fuel_eco);
            startActivity(reviewIntent);
        });
    }

    private String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            date = makeDateString(day, month, year);
            dateButton.setText(date);
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + ", " + year;
    }

    private String getMonthFormat(int month) {
        if(month == 1){
            return "January";
        } else if (month == 2){
            return "February";
        } else if (month == 3){
            return "March";
        } else if (month == 4){
            return "April";
        } else if (month == 5){
            return "May";
        } else if (month == 6){
            return "June";
        } else if (month == 7){
            return "July";
        } else if (month == 8){
            return "August";
        } else if (month == 9){
            return "September";
        } else if (month == 10){
            return "October";
        } else if (month == 11){
            return "November";
        } else {
            return "December";
        }
    }

    //location
//    private final LocationListener locationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            // Use the location object to get latitude and longitude
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//            Log.d(TAG, "Current location: " + latitude + ", " + longitude);
//            // Set the default value of the AutocompleteSupportFragment
//            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
//                    getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
//            autocompleteFragment.setText(latitude + "," + longitude);
//            // Stop location updates to conserve battery
//            locationManager.removeUpdates(locationListener);
//        }

//        @Override
//        public void onProviderEnabled(String provider) {}
//
//        @Override
//        public void onProviderDisabled(String provider) {}
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {}
//    };
}