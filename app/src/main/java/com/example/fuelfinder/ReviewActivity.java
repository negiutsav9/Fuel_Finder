package com.example.fuelfinder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;


public class ReviewActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseAuth firebaseAuth;              //Firebase authorization instance
    FirebaseFirestore firebaseFirestore;    //Firebase Cloud Firestore instance

    String date;
    String time;
    double longitude, latitude;
    double total_cost, fuel_refill, odometer, fuel_eco;
    String fuel_type;
    TextView text_cost, text_capacity, text_rate, text_type, text_odometer, text_economy, text_date, text_time, text_longitude, text_latitude;

    private LocationManager locationManager;

    private GoogleMap map;

    //Location LastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

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
        longitude = fetch.getDoubleExtra("Longitude", 0);
        latitude = fetch.getDoubleExtra("Latitude", 0);
        total_cost = fetch.getDoubleExtra("Cost", 0);
        fuel_refill = fetch.getDoubleExtra("Capacity", 0);
        fuel_type = fetch.getStringExtra("Type");
        odometer = fetch.getDoubleExtra("Odometer", 0);
        fuel_eco = fetch.getDoubleExtra("Economy", 0);

        //Updating UI
        text_date = findViewById(R.id.date);
        text_time = findViewById(R.id.time);
        text_cost = findViewById(R.id.cost);
        text_capacity = findViewById(R.id.fuel);
        text_rate = findViewById(R.id.rate);
        text_economy = findViewById(R.id.fuel_eco);
        text_type = findViewById(R.id.type);
        text_odometer = findViewById(R.id.odometer);

        ReviewActivity.this.findViewById(R.id.date).post(() -> {
            text_date.setText(date);
        });
        ReviewActivity.this.findViewById(R.id.time).post(() -> {
            text_time.setText(time);
        });
        ReviewActivity.this.findViewById(R.id.cost).post(() -> {
            text_cost.setText("$" + total_cost);
        });
        ReviewActivity.this.findViewById(R.id.fuel).post(() -> {
            text_capacity.setText(fuel_refill + " gallons");
        });
        ReviewActivity.this.findViewById(R.id.rate).post(() -> {
            text_rate.setText(String.format("%.2f $/gallon", (double) (total_cost / fuel_refill)));
        });
        ReviewActivity.this.findViewById(R.id.fuel_eco).post(() -> {
            text_economy.setText(fuel_eco + " mpg");
        });
        ReviewActivity.this.findViewById(R.id.type).post(() -> {
            text_type.setText(fuel_type);
        });
        ReviewActivity.this.findViewById(R.id.odometer).post(() -> {
            text_odometer.setText(odometer + " miles");
        });

        //Creating a map layout
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Amena : setting default location
        // Construct a PlacesClient
        //Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        //placesClient = Places.createClient(this);

        //Save button onClick listener
        Button save = findViewById(R.id.save_button);
        save.setOnClickListener((View v) -> {
            firebaseFirestore.collection("User").document(firebaseAuth.getUid())
                    .collection("Logs").document()
                    .set(new LogModel(date, time, total_cost, fuel_refill, (total_cost / fuel_refill),
                            latitude, longitude, fuel_eco, (int) odometer, fuel_type))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ReviewActivity.this, "Log Added", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
                        }
                    });
        });

        //Edit button onClick listener
        Button edit = findViewById(R.id.edit_button);
        edit.setOnClickListener((View v) -> {
            this.finish();
        });
    }

    //    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        LatLng pointer = new LatLng(latitude, longitude);
//        googleMap.addMarker(new MarkerOptions()
//                .position(pointer));
//
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointer,16));
//        // Zoom in, animating the camera.
//        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
//        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 500, null);
//    }
    //Amena:
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        // Add a marker in Sydney and move the camera
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
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        //if(location != null) {
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
        LatLng loc = new LatLng(latitude,longitude);
        map.addMarker(new MarkerOptions().position(loc).title("New Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLng(loc));
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(new LatLng(latitude, longitude))      // Sets the center of the map to location user
//                .zoom(15)                   // Sets the zoom
//                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                .build();                   // Creates a CameraPosition from the builder
    }
        //map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


//        Intent intent = new Intent (this, ManualEntryActivity.class);
//        longitude = intent.getDoubleExtra("Longitude", 0);
//        latitude = intent.getDoubleExtra("Latitude", 0);
//        map = googleMap;
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
//        if (location != null) {
//            map.setMyLocationEnabled(true);
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
//
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
//                    .zoom(15)                   // Sets the zoom
//                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                    .build();                   // Creates a CameraPosition from the builder
//            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//            googleMap.setMyLocationEnabled(true);
//            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//
//        }
    }
