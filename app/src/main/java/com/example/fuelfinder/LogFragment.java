package com.example.fuelfinder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

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

    private int mColumnCount = 1; // Number of columns to display in the RecyclerView
    private ArrayList<LogModel> logModelArrayList; // List of LogModel objects to display in the RecyclerView
    private FirebaseFirestore firebaseFirestore; // Firebase Firestore instance for database access
    private FirebaseAuth firebaseAuth; // Firebase Authentication instance for user authentication
    private MyLogRecyclerViewAdapter adapter; // RecyclerView adapter for displaying the log data


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
            // Set the RecyclerView layout manager based on the number of columns to display
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

        public MyLogRecyclerViewAdapter(ArrayList<LogModel> logsArrayList) {
            this.logsArrayList = logsArrayList;
        }

        @Override
        public int getItemViewType(int position) {
            // Determine the view type based on whether or not the log at this position should show a menu
            if (logsArrayList.get(position).isShowMenu()){
                return SHOW_MENU;
            } else {
                return HIDE_MENU;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the appropriate view holder based on the view type.
            if(viewType == HIDE_MENU){
                // Initialize the Places API client
                Places.initialize(parent.getContext(), "AIzaSyBEP24tbWMHcUY75yXzCBySAGKXF2ZoJ8A");
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
            // Bind the log data to the appropriate views in the view holder.
            if(holder instanceof LogViewHolder){
                // Set the log date, time, cost, refill, type, odometer reading, and fuel economy
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

                // Get the Place ID for the fuel station associated with this log
                String placeID = log.getPlaceID();
                final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

                // Fetch the name and location of the fuel station using the Places API client
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    double latitude,longitude;
                    Place place = response.getPlace();
                    ((LogViewHolder) holder).getFuelStationTV().setText(place.getName());
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                    // Construct a URL for a static map image of the fuel station location
                    String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center="+
                            latitude+","+longitude+"&zoom=14&scale3&size=170x180&markers=color:red%7Csize:mid%7Clabel:S%7C" +
                            + latitude+","+longitude+"&maptype=roadmap&key=AIzaSyBEP24tbWMHcUY75yXzCBySAGKXF2ZoJ8A";
                    Picasso.with(getContext()).load(imageUrl).into(((LogViewHolder) holder).getFuelMapIV());
                });

                ((LogViewHolder)holder).getContainer().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showMenu(position);
                        return true;
                    }
                });
            }

            if(holder instanceof MenuViewHolder){
                // Initialize atomic references for latitude, longitude, and fuel station name
                AtomicReference<Double> latitude = new AtomicReference<>((double) 0);
                AtomicReference<Double> longitude = new AtomicReference<>((double) 0);
                AtomicReference<String> fuelStationName = new AtomicReference<>();
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

                // Fetch the place associated with the log's place ID and update the atomic references for latitude, longitude, and fuel station name
                String placeID = log.getPlaceID();
                final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    latitude.set(place.getLatLng().latitude);
                    longitude.set(place.getLatLng().longitude);
                    fuelStationName.set(place.getName());
                    ((MenuViewHolder) holder).getFuelStationTV().setText(fuelStationName.get());

                });

                // Set an OnClickListener for the "View Map" option in the MenuViewHolder that opens Google Maps with the fuel station's location
                ((MenuViewHolder) holder).getViewMapOption().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mapUri = "geo:0,0?q="+ fuelStationName.get().replace(" ", "+") + "@" + latitude +"," + longitude;
                        Uri gmmIntentUri = Uri.parse(mapUri);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });

                ((MenuViewHolder) holder).getEditOption().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Create an intent to launch ManualEntryActivity for editing the log
                        Intent editIntent = new Intent(getContext(), ManualEntryActivity.class);
                        // pass the log's information as extras to the intent
                        editIntent.putExtra("DocID", log.getId()); // the log's unique ID
                        editIntent.putExtra("DateEdit", log.getDate()); // the date of the log entry
                        editIntent.putExtra("TimeEdit", log.getTime()); // the time of the log entry
                        editIntent.putExtra("PlaceIDEdit", placeID); // the ID of the fuel station
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
                        String date_txt = "Date: " + log.getDate(); // the date of the log entry
                        String time_txt = "Time: " + log.getTime(); // the time of the log entry
                        String fs_txt = "Fuel Station: " + fuelStationName; // the name of the fuel station
                        String cost_txt = "Cost: $ " + log.getTotal_cost(); // the total cost of fuel purchase
                        String capacity_txt = "Refill: " + log.getGallons_of_fuel() + " gallons"; // the amount of fuel in gallons
                        String rate_txt = String.format("Rate: %.2f $/gallon", log.getEstimated_rate()); // the estimated fuel rate
                        String type_txt = "Type: " + log.getFuel_type(); // the type of fuel
                        String odometer_txt = "Recorded Distance: " + log.getOdometer_reading() + " miles"; // the recorded odometer reading
                        String economy_txt = "Recorded Economy: " + log.getMiles_per_gallon() + " mpg\n"; // the recorded fuel economy
                        String ad_txt = "To maintain fuel logs and discover fuel prices of nearby fuel station, download Fuel Finder from Play Store"; // an advertisement message
                        // concatenate all strings into a single string with newlines
                        String intent_txt = date_txt + "\n" + time_txt + "\n" + fs_txt + "\n" + cost_txt + "\n" + capacity_txt + "\n" + rate_txt + "\n" + type_txt + "\n" + odometer_txt + "\n" + economy_txt + "\n" + ad_txt;
                        // add the log's information as extra to the intent
                        sendIntent.putExtra(Intent.EXTRA_TEXT, intent_txt);
                        sendIntent.putExtra(Intent.EXTRA_TITLE, "Fuel Log Entry from Fuel Finder");
                        // set the intent type to plain text
                        sendIntent.setType("text/plain");

                        Intent shareIntent = Intent.createChooser(sendIntent, "Fuel Log Entry");
                        startActivity(shareIntent);
                    }
                });

                ((MenuViewHolder) holder).getDeleteOption().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Delete Log");
                        deleteDialog.setMessage("This will delete the log and its associated data.");
                        deleteDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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
                        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alert = deleteDialog.create();
                        alert.show();
                        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.teal_200));
                        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.orange_red));
                    }
                });

                ((MenuViewHolder) holder).getContainer().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.closeMenu();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return logsArrayList.size();
        }

        public void showMenu(int position){
            for(int i = 0; i < logsArrayList.size(); i++){
                logsArrayList.get(i).setShowMenu(false);
            }
            logsArrayList.get(position).setShowMenu(true);
            notifyDataSetChanged();
        }

        public boolean isMenuShown(){
            for(int i = 0; i < logsArrayList.size(); i++){
                if(logsArrayList.get(i).isShowMenu()){
                    return true;
                }
            }
            return false;
        }
        public void closeMenu() {
            for(int i=0; i<logsArrayList.size(); i++){
                logsArrayList.get(i).setShowMenu(false);
            }
            notifyDataSetChanged();
        }

        public class LogViewHolder extends RecyclerView.ViewHolder {
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
                return container;
            }
            public TextView getFuelStationTV() {
                return fuelStationTV;
            }
            public ImageView getFuelMapIV() {
                return fuelMapIV;
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