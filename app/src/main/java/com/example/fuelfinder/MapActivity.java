package com.example.fuelfinder;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationBarView;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap map;

    private PlacesClient placesClient;

    private HashMap<String, HashMap<String, Object>> nearbyPlaces = new HashMap<>();

    private ArrayList<String> nearby_placeID = new ArrayList<>();
    private ArrayList<Marker> nearby_markers = new ArrayList<>();

    private CardView markerDetail;
    private TextView markerName;
    private TextView markerAddress;
    private TextView markerPhone;
    private TextView markerPrice;
    private TextView markerRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.apiKey);

        // Create a new PlacesClient instance
        placesClient = Places.createClient(this);

        //Setting up action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_maps);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        getSupportActionBar().setElevation(0);

        markerDetail = findViewById(R.id.markerDetail);
        markerName = findViewById(R.id.Name);
        markerAddress = findViewById(R.id.address);
        markerPhone = findViewById(R.id.phone_number);
        markerPrice = findViewById(R.id.price_lvl);
        markerRating = findViewById(R.id.rating);

        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        // Initialize and assign variable
        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.map);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dashboard:
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.map:
                        return true;
                    case R.id.user:
                        startActivity(new Intent(getApplicationContext(), User.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {

        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style_json));
        }

        map.setOnMapClickListener(this);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            map.setMyLocationEnabled(true);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //Extracting info about nearby gas station using background thread
            StringBuilder url_string = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            url_string.append("location="+ location.getLatitude() + "," + location.getLongitude());
            url_string.append("&radius=10000");
            url_string.append("&type=gas_station");
            url_string.append("&key=" + BuildConfig.apiKey);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                //Background work here
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                StringBuffer buffer = new StringBuffer();
                try {
                    URL url = new URL(url_string.toString());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line+"\n");
                    }

                    JSONObject jObject = new JSONObject(buffer.toString());
                    JSONArray jsonArray = (JSONArray) jObject.get("results");
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject entry = new JSONObject(jsonArray.get(i).toString());
                        nearbyPlaces.put(entry.get("place_id").toString(), new HashMap<String, Object>());
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                handler.post(() -> {
                    for(Map.Entry<String, HashMap<String, Object>> mapElement : nearbyPlaces.entrySet()){
                        final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.PHONE_NUMBER, Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI, Place.Field.PRICE_LEVEL, Place.Field.RATING);
                        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(mapElement.getKey(), placeFields);
                        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                           Place place = response.getPlace();
                           //creating markers
                           LatLng pointer = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                           nearby_markers.add(map.addMarker(new MarkerOptions().position(pointer).snippet(mapElement.getKey())));
                            //adding the nearby fuel station data to hashmap
                            mapElement.getValue().put("Name", place.getName());
                            //mapElement.getValue().put("Latitude", place.getLatLng().latitude);
                            //mapElement.getValue().put("Longitude", place.getLatLng().longitude);
                            mapElement.getValue().put("Address", place.getAddress());
                            mapElement.getValue().put("Phone", place.getPhoneNumber());
                            //mapElement.getValue().put("Images_MD", place.getPhotoMetadatas());
                            mapElement.getValue().put("Price", place.getPriceLevel());
                            mapElement.getValue().put("Rating", place.getRating());
                        });
                    }

                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            //center the marker
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(marker.getPosition())      // Sets the center of the map to location user
                                    .zoom(15)                   // Sets the zoom
                                    .tilt(40)                   // Sets the tilt of the camera to 40 degrees
                                    .build();                   // Creates a CameraPosition from the builder
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            //pop up the marker detail
                            markerDetail.setVisibility(View.VISIBLE);
                            HashMap<String, Object> details = nearbyPlaces.get(marker.getSnippet());
                            markerName.setText(details.get("Name").toString());
                            markerAddress.setText(details.get("Address").toString());
                            if(details.get("Phone") != null){
                                markerPhone.setText(details.get("Phone").toString());
                            }
                            if(details.get("Price") != null){
                                markerPrice.setText(details.get("Price").toString());
                            }
                            if(details.get("Rating") != null){
                                markerRating.setText(details.get("Rating").toString());
                            }
                            return true;
                        }
                    });
                });
            });
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markerDetail.setVisibility(View.GONE);
    }
}