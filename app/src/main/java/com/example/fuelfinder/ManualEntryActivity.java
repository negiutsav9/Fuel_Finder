package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

public class ManualEntryActivity extends AppCompatActivity {

    private final String TAG = "Info";
    private DatePickerDialog datePickerDialog;
    private Button dateButton;
    private Button timeButton;
    String date;
    int hours, minutes;
    String time;
    String placeID;
    double total_cost, fuel_refill, odometer, fuel_eco;
    String fuel_type;


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
        Places.initialize(getApplicationContext(), BuildConfig.apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);

        // Back button to return to previous activity
        ImageView backButton = getSupportActionBar().getCustomView().findViewById(R.id.BackButton);
        backButton.setOnClickListener((View v) -> {
            finish();
        });

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


        //Time Picker Code
        Calendar now = Calendar.getInstance();
        timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d",now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)));
        timeButton.setOnClickListener((View v) -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHours, int selectedMinutes) {
                    hours = selectedHours;
                    minutes = selectedMinutes;
                    timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hours, minutes));
                }
            };
            //Create and show a TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hours, minutes, true);
            timePickerDialog.setTitle("Select Time");
            timePickerDialog.show();
            //Change the text color of the positive and negative buttons in the dialog
            timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.teal_200));
            timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.orange_red));
        });


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
                placeID = place.getId();
            }
            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        //Receiving and handling bundles from scan and log edit
        Intent manualEntryFetch = getIntent();
        if (manualEntryFetch.getStringExtra("DateEdit") != null) {
            date = manualEntryFetch.getStringExtra("DateEdit");
            dateButton.setText(date);
        }
        if (manualEntryFetch.getStringExtra("TimeEdit") != null) {
            time = manualEntryFetch.getStringExtra("TimeEdit");
            timeButton.setText(time);
        }
        if (manualEntryFetch.getStringExtra("PlaceIDEdit") != null) {
            placeID = manualEntryFetch.getStringExtra("PlaceIDEdit");
        }
        if (manualEntryFetch.getDoubleExtra("CostEdit", -10) != -10) {
            total_cost = manualEntryFetch.getDoubleExtra("CostEdit", 0);
            cost_edit.setText(total_cost+"");
        } else if (manualEntryFetch.getDoubleExtra("CostScan", -10) != -10){
            total_cost = manualEntryFetch.getDoubleExtra("CostScan", 0);
            if(total_cost > 0)
                cost_edit.setText(total_cost+"");
        }
        if (manualEntryFetch.getDoubleExtra("CapacityEdit", -10) != -10) {
            fuel_refill = manualEntryFetch.getDoubleExtra("CapacityEdit", 0);
            fuel_refill_edit.setText(fuel_refill+"");
        } else if (manualEntryFetch.getDoubleExtra("CapacityScan", -10) != -10){
            fuel_refill = manualEntryFetch.getDoubleExtra("CapacityScan",0);
            if(fuel_refill > 0)
                fuel_refill_edit.setText(fuel_refill+"");
        }
        if (manualEntryFetch.getStringExtra("TypeEdit") != null) {
            fuel_type = manualEntryFetch.getStringExtra("TypeEdit");
            fuel_type_edit.setText(fuel_type);
        }
        if (manualEntryFetch.getDoubleExtra("OdometerEdit", -10) != -10) {
            odometer = manualEntryFetch.getDoubleExtra("OdometerEdit", 0);
            odometer_edit.setText(odometer+"");
        } else if (manualEntryFetch.getDoubleExtra("OdometerScan",-10) != -10){
            odometer = manualEntryFetch.getDoubleExtra("OdometerScan",0);
            if(odometer > 0)
                odometer_edit.setText(odometer+"");
        }
        if (manualEntryFetch.getDoubleExtra("EconomyEdit",-10) != -10) {
            fuel_eco = manualEntryFetch.getDoubleExtra("EconomyEdit", 0);
            fuel_eco_edit.setText(fuel_eco+"");
        } else if (manualEntryFetch.getDoubleExtra("EconomyScan",-10) != -10){
            fuel_eco = manualEntryFetch.getDoubleExtra("EconomyScan", 0);
            if(fuel_eco > 0)
                fuel_eco_edit.setText(fuel_eco+"");
        }

        //On Clicking Review
        review.setOnClickListener((View v) -> {
            date = dateButton.getText().toString();
            time = timeButton.getText().toString();
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

            if(manualEntryFetch.getStringExtra("DocID") != null){
                reviewIntent.putExtra("DocID", manualEntryFetch.getStringExtra("DocID"));
            }

            reviewIntent.putExtra("Date", date);
            reviewIntent.putExtra("Time", time);
            reviewIntent.putExtra("PlaceID", placeID);
            reviewIntent.putExtra("Cost", total_cost);
            reviewIntent.putExtra("Capacity", fuel_refill);
            reviewIntent.putExtra("Type", fuel_type);
            reviewIntent.putExtra("Odometer", odometer);
            reviewIntent.putExtra("Economy", fuel_eco);
            startActivity(reviewIntent);
        });
    }

    // Get current date
    private String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    // Initializes a DatePickerDialog to allow user to select a date
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                date = makeDateString(day, month, year);
                dateButton.setText(date);
            }
        };
        // Get current date to use as initial date for the DatePickerDialog
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
    }

    // Formats a day, month, year as a string
    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + ", " + year;
    }

    // This method returns the name of the month given its number
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
}