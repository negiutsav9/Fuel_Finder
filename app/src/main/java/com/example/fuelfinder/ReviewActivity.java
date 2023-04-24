package com.example.fuelfinder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.List;


// This class defines the ReviewActivity and implements the OnMapReadyCallback interface.
public class ReviewActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseAuth firebaseAuth;              //Firebase authorization instance
    FirebaseFirestore firebaseFirestore;    //Firebase Cloud Firestore instance

    // Date of the fuel entry
    String date;
    // Time of the fuel entry
    String time;
    // Place ID of the fuel station
    String placeID;
    double total_cost, fuel_refill, odometer, fuel_eco;
    // Type of fuel used
    String fuel_type;
    // UI TextViews for displaying the fuel entry details
    TextView text_cost, text_capacity, text_rate, text_type, text_odometer, text_economy, text_date, text_time;
    // PlacesClient for getting place details
    PlacesClient placesClient;

    // Boolean to check if the fuel entry is being updated
    boolean isUpdate;

    // Called when the activity is starting
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        placesClient = Places.createClient(this);

        //Setting up action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_review);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        getSupportActionBar().setElevation(0);

        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        //Assigning the current instance of Firebase Authorization and Firebase Cloud Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //Get values from previous activity
        Intent fetch = getIntent();
        date = fetch.getStringExtra("Date");
        time = fetch.getStringExtra("Time");
        placeID = fetch.getStringExtra("PlaceID");
        total_cost = fetch.getDoubleExtra("Cost", 0);
        fuel_refill = fetch.getDoubleExtra("Capacity", 0);
        fuel_type = fetch.getStringExtra("Type");
        odometer = fetch.getDoubleExtra("Odometer", 0);
        fuel_eco = fetch.getDoubleExtra("Economy", 0);
        isUpdate = (fetch.getStringExtra("DocID") != null);

        //Updating UI
        text_date = findViewById(R.id.date);
        text_time = findViewById(R.id.time);
        text_cost = findViewById(R.id.cost);
        text_capacity = findViewById(R.id.fuel);
        text_rate = findViewById(R.id.rate);
        text_economy = findViewById(R.id.fuel_eco);
        text_type = findViewById(R.id.type);
        text_odometer = findViewById(R.id.odometer);

        ReviewActivity.this.findViewById(R.id.date).post(()->{
            text_date.setText(date);
        });
        ReviewActivity.this.findViewById(R.id.time).post(()->{
            text_time.setText(time);
        });
        ReviewActivity.this.findViewById(R.id.cost).post(()->{
           text_cost.setText("$" + total_cost);
        });
        ReviewActivity.this.findViewById(R.id.fuel).post(()->{
            text_capacity.setText(fuel_refill + " gallons");
        });
        ReviewActivity.this.findViewById(R.id.rate).post(()->{
            text_rate.setText(String.format("%.2f $/gallon", (double)(total_cost / fuel_refill)));
        });
        ReviewActivity.this.findViewById(R.id.fuel_eco).post(()->{
            text_economy.setText(fuel_eco + " mpg");
        });
        ReviewActivity.this.findViewById(R.id.type).post(()->{
            text_type.setText(fuel_type);
        });
        ReviewActivity.this.findViewById(R.id.odometer).post(()->{
            text_odometer.setText(odometer + " miles");
        });

        //Creating a map layout
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Save button onClick listener
        Button save = findViewById(R.id.save_button);
        save.setOnClickListener((View v) -> {
            if(!isUpdate){
                // Create a new document reference to the User's Logs collection with a generated ID
                DocumentReference ref = firebaseFirestore.collection("User").document(firebaseAuth.getUid())
                        .collection("Logs").document();
                // Set the document data with a new LogModel object
                ref.set(new LogModel(ref.getId(), date, time, total_cost,fuel_refill,(total_cost / fuel_refill),
                        placeID, fuel_eco,(int)odometer, fuel_type, false));
                // Add a timestamp to the document and show a success message on completion
                ref.update("timestamp", FieldValue.serverTimestamp())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ReviewActivity.this,"Log Added", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                            }
                        });
            } else {
                // Get the existing document reference from the fetch Intent extra
                DocumentReference ref = firebaseFirestore.collection("User").document(firebaseAuth.getUid())
                        .collection("Logs").document(fetch.getStringExtra("DocID"));
                // Create a new LogModel object with the same document ID as the existing document
                LogModel updatedLog = new LogModel(ref.getId(), date, time, total_cost,fuel_refill,(total_cost / fuel_refill),
                        placeID, fuel_eco,(int)odometer, fuel_type, false);
                // Update the existing document with the new data and show a success message on completion
                ref.update(updatedLog.convertToHashMap());
                ref.update("timestamp", FieldValue.serverTimestamp())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ReviewActivity.this,"Log Updated", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                            }
                        });
            }

        });

        //Edit button onClick listener
        Button edit = findViewById(R.id.edit_button);
        edit.setOnClickListener((View v) -> {
            // Close the current activity and return to the previous one
            this.finish();
        });
    }

    // Map ready callback method
    @Override
    public void onMapReady(GoogleMap googleMap) {
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

        // Fetch the place information from the Places API and add a marker to the map
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i("Place API", "Place found: " + place.getName());
            LatLng pointer = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            googleMap.addMarker(new MarkerOptions().position(pointer));
            // Move the camera to the location of the marker and zoom in
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointer,16));
            // Zoom in, animating the camera.
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 500, null);
        });
    }
}