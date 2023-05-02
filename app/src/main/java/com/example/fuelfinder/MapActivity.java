package com.example.fuelfinder;


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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private PlacesClient placesClient;
    private final HashMap<String, HashMap<String, Object>> nearbyPlaces = new HashMap<>();
    private ArrayList<Marker> nearby_markers = new ArrayList<>();
    private HashMap<String, HashMap<String, ArrayList<Double>>> fuelPrices = new HashMap<>();
    private HashMap<String, ArrayList<Double>> selectedPrices;
    private CardView markerDetail;
    private TextView markerName;
    private TextView markerAddress;
    private TextView markerPhone;
    private TextView markerPrice;
    private TextView markerRating;
    private TextView priceHeader;
    private ImageView route_button;
    private String fuelStationName;
    private String longitude;
    private String latitude;
    private FirebaseFirestore firebaseFirestore;
    private TextView price_85, price_86, price_87, price_88, price_89, price_90, price_91, price_92;
    private TextView price_93, price_diesel, price_flexFuel;


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

        //firebase instance
        firebaseFirestore = FirebaseFirestore.getInstance();

        markerDetail = findViewById(R.id.markerDetail);
        markerName = findViewById(R.id.Name);
        markerAddress = findViewById(R.id.address);
        markerPhone = findViewById(R.id.phone_number);
        priceHeader = findViewById(R.id.price_header);
        markerPrice = findViewById(R.id.price_lvl);
        markerRating = findViewById(R.id.rating);
        route_button = findViewById(R.id.route);
        price_85 = findViewById(R.id.price_85);
        price_86 = findViewById(R.id.price_86);
        price_87 = findViewById(R.id.price_87);
        price_88 = findViewById(R.id.price_88);
        price_89 = findViewById(R.id.price_89);
        price_90 = findViewById(R.id.price_90);
        price_91 = findViewById(R.id.price_91);
        price_92 = findViewById(R.id.price_92);
        price_93 = findViewById(R.id.price_93);
        price_diesel = findViewById(R.id.price_diesel);
        price_flexFuel = findViewById(R.id.price_flexFuel);

        route_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mapUri = "geo:0,0?q="+ fuelStationName.replace(" ", "+") + "@" + latitude +"," + longitude;
                Uri gmmIntentUri = Uri.parse(mapUri);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        //Navigation Bar
        getWindow().setNavigationBarColor(getColor(R.color.orange_red));
        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);
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

                //getting data from firebase
                firebaseFirestore.collection("Prices").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        if(!queryDocumentSnapshots.isEmpty()){
                            for(DocumentSnapshot d: list){
                                PriceDataModel price = d.toObject(PriceDataModel.class);
                                if(!fuelPrices.containsKey(price.getPlaceID())){
                                    fuelPrices.put(price.getPlaceID(), new HashMap<String, ArrayList<Double>>());

                                }
                                Log.d("Place ID detected", fuelPrices.keySet().toString());
                                if(!fuelPrices.get(price.getPlaceID()).containsKey(price.getFuel_type())){
                                    fuelPrices.get(price.getPlaceID()).put(price.getFuel_type(), new ArrayList<Double>());
                                }
                                Log.d("Fuel Type Detected", fuelPrices.get(price.getPlaceID()).keySet().toString());
                                fuelPrices.get(price.getPlaceID()).get(price.getFuel_type()).add(price.getEstimated_rate());
                                //removing outlier by calculating the z-index of each arraylist and removing the values having z-index greater than 2
                                double mean;
                                double std;
                                double total_sum = 0;
                                //getting the mean of the ArrayList
                                for(double p : fuelPrices.get(price.getPlaceID()).get(price.getFuel_type())){
                                    total_sum += p;
                                }
                                mean = total_sum/fuelPrices.get(price.getPlaceID()).get(price.getFuel_type()).size();
                                //getting the standard deviation of the ArrayList
                                double sum = 0;
                                for(double q : fuelPrices.get(price.getPlaceID()).get(price.getFuel_type())){
                                    sum += Math.pow((q - mean), 2);
                                }
                                std = Math.sqrt(sum/(fuelPrices.get(price.getPlaceID()).get(price.getFuel_type()).size() - 1));
                                //calculate z-index and remove the data with z-index greater than 2
                                for(double r : fuelPrices.get(price.getPlaceID()).get(price.getFuel_type())){
                                    double z_score = (r - mean)/std;
                                    if(Math.abs(Math.round(z_score)) > 5){
                                        fuelPrices.get(price.getPlaceID()).get(price.getFuel_type()).remove(r);
                                    }
                                }
                            }
                        }
                    }
                });


                handler.post(() -> {
                    for(Map.Entry<String, HashMap<String, Object>> mapElement : nearbyPlaces.entrySet()){
                        final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER, Place.Field.PRICE_LEVEL, Place.Field.RATING);
                        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(mapElement.getKey(), placeFields);
                        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                           Place place = response.getPlace();
                           //creating markers
                           LatLng pointer = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                           nearby_markers.add(map.addMarker(new MarkerOptions().position(pointer).snippet(mapElement.getKey())));
                            //adding the nearby fuel station data to hashmap
                            mapElement.getValue().put("Name", place.getName());
                            mapElement.getValue().put("Latitude", place.getLatLng().latitude);
                            mapElement.getValue().put("Longitude", place.getLatLng().longitude);
                            mapElement.getValue().put("Address", place.getAddress());
                            mapElement.getValue().put("Phone", place.getPhoneNumber());
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

                            //getting price from the FireStore result
                            selectedPrices = fuelPrices.get(marker.getSnippet());

                            //pop up the marker detail
                            markerDetail.setVisibility(View.VISIBLE);
                            HashMap<String, Object> details = nearbyPlaces.get(marker.getSnippet());
                            fuelStationName =  details.get("Name").toString();
                            latitude = details.get("Latitude").toString();
                            longitude = details.get("Longitude").toString();
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
                            if(selectedPrices != null){
                                if(selectedPrices.get("85") != null){
                                    Log.d("Available Prices", selectedPrices.keySet().toString());
                                    Log.d("85 Prices", String.format("%.2f", average(selectedPrices.get("85"))));
                                    if(average(selectedPrices.get("85")) >= 0){
                                    } else {
                                        price_85.setText(String.format("N/A"));
                                    }
                                }
                                if(selectedPrices.get("86") != null){
                                    if(average(selectedPrices.get("86")) >= 0){
                                        price_86.setText(String.format("%.2f", average(selectedPrices.get("86"))));
                                    }
                                }
                                if(selectedPrices.get("87") != null){
                                    if(average(selectedPrices.get("87")) >= 0){
                                        price_87.setText(String.format("%.2f", average(selectedPrices.get("87"))));
                                    }
                                }
                                if(selectedPrices.get("88") != null){
                                    if(average(selectedPrices.get("88")) >= 0){
                                        price_88.setText(String.format("%.2f", average(selectedPrices.get("88"))));
                                    }
                                }
                                if(selectedPrices.get("89") != null){
                                    if(average(selectedPrices.get("89")) >= 0){
                                        price_89.setText(String.format("%.2f", average(selectedPrices.get("89"))));
                                    }
                                }
                                if(selectedPrices.get("90") != null){
                                    if(average(selectedPrices.get("90")) >= 0){
                                        price_90.setText(String.format("%.2f", average(selectedPrices.get("90"))));
                                    }
                                }
                                if(selectedPrices.get("91") != null){
                                    if(average(selectedPrices.get("91")) >= 0){
                                        price_91.setText(String.format("%.2f", average(selectedPrices.get("91"))));
                                    }
                                }
                                if(selectedPrices.get("92") != null){
                                    if(average(selectedPrices.get("92")) >= 0){
                                        price_92.setText(String.format("%.2f", average(selectedPrices.get("92"))));
                                    }
                                }
                                if(selectedPrices.get("93") != null){
                                    if(average(selectedPrices.get("93")) >= 0){
                                        price_93.setText(String.format("%.2f", average(selectedPrices.get("93"))));
                                    }
                                }
                                if(selectedPrices.get("Diesel") != null){
                                    if(average(selectedPrices.get("Diesel")) >= 0){
                                        price_diesel.setText(String.format("%.2f", average(selectedPrices.get("Diesel"))));
                                    }
                                }
                                if(selectedPrices.get("Flex Fuel") != null){
                                    if(average(selectedPrices.get("Flex Fuel")) >= 0){
                                        price_flexFuel.setText(String.format("%.2f", average(selectedPrices.get("Flex Fuel"))));
                                    }
                                }
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
        selectedPrices = null;
    }

    public double average(ArrayList<Double> alist){
        if(alist == null){
            return 0;
        }
        double sum = 0;
        for(double a : alist){
            sum += a;
        }
        return sum/alist.size();
    }
}