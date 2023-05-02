package com.example.fuelfinder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.res.Configuration;
import android.net.Uri;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fuelfinder.databinding.FragmentLogBinding;
import com.example.fuelfinder.databinding.FragmentLogMenuBinding;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A fragment representing a list of Items.
 */
public class LogFragment extends Fragment{

    // Number of columns to display in the RecyclerView
    private int mColumnCount = 1;
    // List of LogModel objects to display in the RecyclerView
    private ArrayList<LogModel> logModelArrayList;
    // Firebase Firestore instance for database access
    private FirebaseFirestore firebaseFirestore;
    // Firebase Authentication instance for user authentication
    private FirebaseAuth firebaseAuth;
    // RecyclerView adapter for displaying the log data
    private MyLogRecyclerViewAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set a thread policy to permit network access
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log_list, container, false);

        // Initialize Firebase instances
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        // Create an empty list to hold the LogModel objects
        logModelArrayList = new ArrayList<>();
        // Create a RecyclerView adapter with the empty list
        adapter = new MyLogRecyclerViewAdapter(logModelArrayList);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            // Set the RecyclerView layout manager based on the number of columns too display
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            // Set the RecyclerView to have a fixed size for improved performance
            recyclerView.setHasFixedSize(true);
            // Set the adapter for the RecyclerView
            recyclerView.setAdapter(adapter);

            // Add a scroll listener to the RecyclerView to close any open menus
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    adapter.closeMenu();
                }
            });

        }

        // Query the database for logs belonging to the current user and order them by timestamp in descending order
        firebaseFirestore.collection("User").document(firebaseAuth.getUid()).collection("Logs").orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Retrieve a list of DocumentSnapshot objects representing the logs
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Iterate through the list of logs and add them to the logModelArrayList
                            for (DocumentSnapshot d : list) {
                                // after getting this list we are passing
                                // that list to our object class.
                                LogModel l = d.toObject(LogModel.class);
                                logModelArrayList.add(l);
                            }
                            // after adding the data to recycler view.
                            // we are calling recycler view notifyDataSetChanged
                            // method to notify that data has been changed in recycler view.
                            adapter.notifyDataSetChanged();
                        } else {
                            // if the snapshot is empty we are displaying a toast message.
                            Toast.makeText(view.getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If there is a failure while retrieving the logs, display a toast message
                        Toast.makeText(view.getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show();
                    }
                });
        // Return the view
        return view;
    }

    // Define a custom RecyclerView adapter for displaying the logs
    class MyLogRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int SHOW_MENU = 1;
        private final int HIDE_MENU = 2;

        private ArrayList<LogModel> logsArrayList;
        private PlacesClient placesClient;

        String fuelStationName;
        double latitude, longitude;

        public MyLogRecyclerViewAdapter(ArrayList<LogModel> logsArrayList) {
            this.logsArrayList = logsArrayList;
        }

        @Override
        public int getItemViewType(int position) {
            // Determine the view type based on whether or not the log at this positino should show a menu
            if (logsArrayList.get(position).isShowMenu()){
                return SHOW_MENU;
            } else {
                return HIDE_MENU;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the appropriate view holder based on the view type
            if(viewType == HIDE_MENU){
                // Initialize the Places API client
                Places.initialize(parent.getContext(), BuildConfig.apiKey);
                placesClient = Places.createClient(parent.getContext());
                // Inflate the view holder for a log item without a menu
                return new LogViewHolder(FragmentLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {
                // Inflate the view holder for a log item with a menu
                return new MenuViewHolder(FragmentLogMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            // Get the log at this position in the logArrayList
            LogModel log = logsArrayList.get(position);
            // setting data to our text views from our modal class.
            if(holder instanceof LogViewHolder){
                ((LogViewHolder) holder).getDateTV().post(() -> ((LogViewHolder) holder).getDateTV().setText(log.getDate()));
                ((LogViewHolder) holder).getDateTV().post(() -> ((LogViewHolder) holder).getTimeTV().setText(log.getTime()));
                ((LogViewHolder) holder).getDateTV().post(() -> ((LogViewHolder) holder).getCostTV().setText("$ " + log.getTotal_cost()));
                ((LogViewHolder) holder).getDateTV().post(() -> {
                    ((LogViewHolder) holder).getRefillTV().setText(log.getGallons_of_fuel() + " gallons");
                });
                ((LogViewHolder) holder).getDateTV().post(() -> {
                    ((LogViewHolder) holder).getTypeTV().setText(log.getFuel_type());
                });
                ((LogViewHolder) holder).getDateTV().post(() -> {
                    ((LogViewHolder) holder).getOdometerTV().setText(log.getOdometer_reading() + " miles");
                });
                ((LogViewHolder) holder).getDateTV().post(() -> {
                    ((LogViewHolder) holder).getFuelEcoTV().setText(log.getMiles_per_gallon() + " mpg");
                });

                if(log.getPlaceID() != null){
                    final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                    final FetchPlaceRequest request = FetchPlaceRequest.newInstance(log.getPlaceID(), placeFields);

                    // Fetch the name and location of the fuel station using the Places API client
                    placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                        double latitude,longitude;
                        Place place = response.getPlace();
                        ((LogViewHolder) holder).getFuelStationTV().setText(place.getName());
                        latitude = place.getLatLng().latitude;
                        longitude = place.getLatLng().longitude;
                        int nightModeFlags = getContext().getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                        // Construct a URL for a static map image of the fuel station location
                        String imageUrl;
                        if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
                            imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center="+
                                    latitude+","+longitude+"&zoom=14&scale3&size=170x180&markers=color:red%7Csize:mid%7Clabel:S%7C" +
                                    + latitude+","+longitude+"&maptype=roadmap&key=" + BuildConfig.apiKey +
                                    "&style=element%3Ageometry%7Ccolor%3A0x242f3e&style=element%3Alabels.text.stroke%7Ccolor%3A0x242f3e&" +
                                    "style=element%3Alabels.text.fill%7Ccolor%3A0x746855&style=feature%3Aadministrative.locality" +
                                    "%7Celement%3Alabels.text.fill%7Ccolor%3A0xd59563&style=feature%3Apoi%7Celement%3Alabels.text" +
                                    ".fill%7Ccolor%3A0xd59563&style=feature%3Apoi.park%7Celement%3Ageometry%7Ccolor%3A0x263c3f&" +
                                    "style=feature%3Apoi.park%7Celement%3Alabels.text.fill%7Ccolor%3A0x6b9a76&style=feature%3A" +
                                    "road%7Celement%3Ageometry%7Ccolor%3A0x38414e&style=feature%3Aroad%7Celement%3Ageometry.stroke" +
                                    "%7Ccolor%3A0x212a37&style=feature%3Aroad%7Celement%3Alabels.text.fill%7Ccolor%3A0x9ca5b3&style" +
                                    "=feature%3Aroad.highway%7Celement%3Ageometry%7Ccolor%3A0x746855&style=feature%3Aroad.highway%7C" +
                                    "element%3Ageometry.stroke%7Ccolor%3A0x1f2835&style=feature%3Aroad.highway%7Celement%3Alabels.text" +
                                    ".fill%7Ccolor%3A0xf3d19c&style=feature%3Atransit%7Celement%3Ageometry%7Ccolor%3A0x2f3948&style=" +
                                    "feature%3Atransit.station%7Celement%3Alabels.text.fill%7Ccolor%3A0xd59563&style=feature%3Awater%" +
                                    "7Celement%3Ageometry%7Ccolor%3A0x17263c&style=feature%3Awater%7Celement%3Alabels.text.fill%7Ccolor" +
                                    "%3A0x515c6d&style=feature%3Awater%7Celement%3Alabels.text.stroke%7Ccolor%3A0x17263c";
                        } else {
                            imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center="+
                                    latitude+","+longitude+"&zoom=14&scale3&size=170x180&markers=color:red%7Csize:mid%7Clabel:S%7C" +
                                    + latitude+","+longitude+"&maptype=roadmap&key=" + BuildConfig.apiKey;

                        }
                        ((LogViewHolder) holder).getFuelMapIV().setVisibility(View.VISIBLE);
                        Picasso.with(getContext()).load(imageUrl).into(((LogViewHolder) holder).getFuelMapIV());
                    });
                } else {
                    ((LogViewHolder) holder).getFuelStationTV().setText("");
                    ((LogViewHolder) holder).getFuelMapIV().setVisibility(View.INVISIBLE);
                }


                ((LogViewHolder)holder).getContainer().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showMenu(position);
                        return true;
                    }
                });
            }

            if(holder instanceof MenuViewHolder){
                // Set the date, time, cost, refill amount, fuel type, odometer reading, and fuel economy in the MenuViewHolder
                ((MenuViewHolder) holder).getDateTV().post(() -> {
                    ((MenuViewHolder) holder).getDateTV().setText(log.getDate());
                });
                ((MenuViewHolder) holder).getDateTV().post(() -> {
                    ((MenuViewHolder) holder).getTimeTV().setText(log.getTime());
                });
                ((MenuViewHolder) holder).getDateTV().post(() -> {
                    ((MenuViewHolder) holder).getCostTV().setText("$ " + log.getTotal_cost());
                });
                ((MenuViewHolder) holder).getDateTV().post(() -> {
                    ((MenuViewHolder) holder).getRefillTV().setText(log.getGallons_of_fuel() + " gallons");
                });
                ((MenuViewHolder) holder).getDateTV().post(() -> {
                    ((MenuViewHolder) holder).getTypeTV().setText(log.getFuel_type());
                });
                ((MenuViewHolder) holder).getDateTV().post(() -> {
                    ((MenuViewHolder) holder).getOdometerTV().setText(log.getOdometer_reading() + " miles");
                });
                ((MenuViewHolder) holder).getDateTV().post(() -> {
                    ((MenuViewHolder) holder).getFuelEcoTV().setText(log.getMiles_per_gallon() + " mpg");
                });

                // Fetch the place assoociated with the log's place ID and update the atomic referecnes for latitude, longitude, and fuel station name
                if(log.getPlaceID() != null){
                    final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                    final FetchPlaceRequest request = FetchPlaceRequest.newInstance(log.getPlaceID(), placeFields);

                    placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                        Place place = response.getPlace();
                        fuelStationName = place.getName();
                        longitude = place.getLatLng().longitude;
                        latitude = place.getLatLng().latitude;
                        ((MenuViewHolder) holder).getFuelStationTV().setText(place.getName());
                    });
                } else {
                    ((MenuViewHolder) holder).getFuelStationTV().setText("");
                }

                // Set an OnClickListener for the "View Map" option in the MenuViewHolder that opens Google Maps with the fuel station's location
                if(log.getPlaceID() != null){
                    ((MenuViewHolder) holder).getViewMapOption().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String mapUri = "geo:0,0?q="+ fuelStationName.replace(" ", "+") + "@" + latitude +"," + longitude;
                            Uri gmmIntentUri = Uri.parse(mapUri);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    });
                } else {
                    ((MenuViewHolder) holder).getViewMapOption().setClickable(false);
                }


                ((MenuViewHolder) holder).getEditOption().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Create an intent to launch ManualEntryActivity for editing the log
                        Intent editIntent = new Intent(getContext(), ManualEntryActivity.class);
                        // pass the log's information as extras to the intent
                        editIntent.putExtra("DocID", log.getId()); // the log's unique ID
                        editIntent.putExtra("DateEdit", log.getDate()); // the date of the log entry
                        editIntent.putExtra("TimeEdit", log.getTime()); // the time of the log entry
                        editIntent.putExtra("PlaceIDEdit", log.getPlaceID()); // the ID of the fuel station
                        editIntent.putExtra("CostEdit", log.getTotal_cost()); // the total cost of the fuel purchase
                        editIntent.putExtra("CapacityEdit", log.getGallons_of_fuel()); // the amount of fuel in gallons
                        editIntent.putExtra("TypeEdit", log.getFuel_type()); // the type of fuel
                        editIntent.putExtra("OdometerEdit", log.getOdometer_reading()); // the recorded odometer reading
                        editIntent.putExtra("EconomyEdit", log.getMiles_per_gallon()); // the recorded fuel economy
                        // start the activity for editing the log
                        startActivity(editIntent);
                    }
                });

                ((MenuViewHolder) holder).getShareOption().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Create an intent to share the log's information via other apps
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        // create a string with the log's information
                        String date_txt = "Date: " + log.getDate();
                        String time_txt = "Time: " + log.getTime();
                        String fs_txt = "Fuel Station: " + fuelStationName;
                        String cost_txt = "Cost: $ " + log.getTotal_cost();
                        String capacity_txt = "Refill: " + log.getGallons_of_fuel() + " gallons";
                        String rate_txt = String.format("Rate: %.2f $/gallon", log.getEstimated_rate());
                        String type_txt = "Type: " + log.getFuel_type();
                        String odometer_txt = "Recorded Distance: " + log.getOdometer_reading() + " miles";
                        String economy_txt = "Recorded Economy: " + log.getMiles_per_gallon() + " mpg\n";
                        String ad_txt = "To maintain fuel logs and discover fuel prices of nearby fuel station, download Fuel Finder from Play Store";
                        // concatenate all strings into a single string with newlines
                        String intent_txt = date_txt + "\n" + time_txt + "\n" + fs_txt + "\n" + cost_txt + "\n" + capacity_txt + "\n" + rate_txt + "\n" + type_txt + "\n" + odometer_txt + "\n" + economy_txt + "\n" + ad_txt;
                        // add the log's information as extra to the intent
                        sendIntent.putExtra(Intent.EXTRA_TEXT, intent_txt);
                        sendIntent.putExtra(Intent.EXTRA_TITLE, "Fuel Log Entry from Fuel Finder");
                        // set the intent type to plain text
                        sendIntent.setType("text/plain");

                        // Create a chooser
                        Intent shareIntent = Intent.createChooser(sendIntent, "Fuel Log Entry");
                        startActivity(shareIntent);
                    }
                });

                ((MenuViewHolder) holder).getDeleteOption().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // When delete button is clicked, show an alert to confirm deletion
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Delete Log");
                        deleteDialog.setMessage("This will delete the log and its associated data.");
                        deleteDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // If "Delete" button in dialog is clicked, delete the log data from Firestore and remove it from the list
                                Toast.makeText(getContext(), "Log Deleted", Toast.LENGTH_LONG).show();
                                firebaseFirestore.collection("User").document(firebaseAuth.getInstance().getUid())
                                        .collection("Logs").document(log.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                logsArrayList.remove(position);
                                                notifyItemRemoved(position);
                                            }
                                        });
                            }
                        });
                        // If "Cancel" button in dialog is clicked, simply dismiss the dialog
                        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        // Create and show the alert dialog
                        AlertDialog alert = deleteDialog.create();
                        alert.show();
                        // Set the text colors for dialog buttons
                        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.teal_200));
                        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.orange_red));
                    }
                });

                ((MenuViewHolder) holder).getContainer().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // When the menu container is clicked, close the menu adapter
                        adapter.closeMenu();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            // Return the number of logs in the list
            return logsArrayList.size();
        }

        public void showMenu(int position){
            // Show the menu for the specified log position
            for(int i = 0; i < logsArrayList.size(); i++){
                logsArrayList.get(i).setShowMenu(false);
            }
            logsArrayList.get(position).setShowMenu(true);
            notifyDataSetChanged();
        }

        public boolean isMenuShown(){
            // Check if any log item is showing the menu
            for(int i = 0; i < logsArrayList.size(); i++){
                if(logsArrayList.get(i).isShowMenu()){
                    return true;
                }
            }
            // Return false if no log item is showing the menu
            return false;
        }
        public void closeMenu() {
            // Close menu for all log items
            for(int i=0; i<logsArrayList.size(); i++){
                logsArrayList.get(i).setShowMenu(false);
            }
            // Notify the adapter that the data set has changed
            notifyDataSetChanged();
        }

        public class LogViewHolder extends RecyclerView.ViewHolder {
            // Declare all view variables for a log item
            private final TextView dateTV;
            private final TextView timeTV;
            private final TextView costTV;
            private final TextView refillTV;
            private final TextView typeTV;
            private final TextView odometerTV;
            private final TextView fuelEcoTV;
            private final TextView fuelStationTV;
            private final ImageView fuelMapIV;
            private final CardView container;

            public LogViewHolder(FragmentLogBinding binding) {
                // Initialize all view variables for a log item
                super(binding.getRoot());
                dateTV = itemView.findViewById(R.id.Date);
                timeTV = itemView.findViewById(R.id.Time);
                costTV = itemView.findViewById(R.id.total_cost);
                refillTV = itemView.findViewById(R.id.refill);
                odometerTV = itemView.findViewById(R.id.odometer);
                typeTV = itemView.findViewById(R.id.fuel_type);
                fuelEcoTV = itemView.findViewById(R.id.fuel_eco);
                fuelStationTV = itemView.findViewById(R.id.fuelStation);
                fuelMapIV = itemView.findViewById(R.id.fuelMap);
                container = itemView.findViewById(R.id.LogItem);
            }

            public CardView getContainer() {
                // returns the container card view
                return container;
            }
            public TextView getFuelStationTV() {
                // returns the fuel station text view
                return fuelStationTV;
            }
            public ImageView getFuelMapIV() {
                // returns the fuel map image view
                return fuelMapIV;
            }
            public TextView getDateTV() {
                // returns the date text view
                return dateTV;
            }
            public TextView getTimeTV() {
                // returns the time text view
                return timeTV;
            }
            public TextView getCostTV() {
                // returns the cost text view
                return costTV;
            }
            public TextView getRefillTV() {
                // returns the refill text view
                return refillTV;
            }
            public TextView getTypeTV() {
                // returns the type text view
                return typeTV;
            }
            public TextView getOdometerTV() {
                // returns the odometer text view
                return odometerTV;
            }
            public TextView getFuelEcoTV() {
                // returns the fuel eco text view
                return fuelEcoTV;
            }
        }

        public class MenuViewHolder extends RecyclerView.ViewHolder {
            private final TextView dateTV;
            private final TextView timeTV;
            private final TextView costTV;
            private final TextView refillTV;
            private final TextView typeTV;
            private final TextView odometerTV;
            private final TextView fuelEcoTV;
            private final TextView fuelStationTV;
            private final TableRow viewMapOption;
            private final TableRow editOption;
            private final TableRow shareOption;
            private final TableRow deleteOption;
            private final CardView container;

            public MenuViewHolder(FragmentLogMenuBinding binding) {
                super(binding.getRoot());
                dateTV = itemView.findViewById(R.id.Date);
                timeTV = itemView.findViewById(R.id.Time);
                costTV = itemView.findViewById(R.id.total_cost);
                refillTV = itemView.findViewById(R.id.refill);
                odometerTV = itemView.findViewById(R.id.odometer);
                typeTV = itemView.findViewById(R.id.fuel_type);
                fuelEcoTV = itemView.findViewById(R.id.fuel_eco);
                fuelStationTV = itemView.findViewById(R.id.fuelStation);
                viewMapOption = itemView.findViewById(R.id.viewMap);
                editOption = itemView.findViewById(R.id.edit);
                shareOption = itemView.findViewById(R.id.share);
                deleteOption = itemView.findViewById(R.id.delete);
                container = itemView.findViewById(R.id.LogItem);
            }

            public TextView getFuelStationTV() {
                return fuelStationTV;
            }
            public TextView getDateTV() {
                return dateTV;
            }
            public TextView getTimeTV() {
                return timeTV;
            }
            public TextView getCostTV() {
                return costTV;
            }
            public TextView getRefillTV() {
                return refillTV;
            }
            public TextView getTypeTV() {
                return typeTV;
            }
            public TextView getOdometerTV() {
                return odometerTV;
            }
            public TextView getFuelEcoTV() {
                return fuelEcoTV;
            }
            public TableRow getViewMapOption() {
                return viewMapOption;
            }
            public TableRow getEditOption() {
                return editOption;
            }
            public TableRow getShareOption() {
                return shareOption;
            }
            public TableRow getDeleteOption() {
                return deleteOption;
            }
            public CardView getContainer() {
                return container;
            }
        }
    }
}