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

    private int mColumnCount = 1;
    private ArrayList<LogModel> logModelArrayList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private MyLogRecyclerViewAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_list, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        logModelArrayList = new ArrayList<>();
        adapter = new MyLogRecyclerViewAdapter(logModelArrayList);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);

            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    adapter.closeMenu();
                }
            });

        }

        firebaseFirestore.collection("User").document(firebaseAuth.getUid()).collection("Logs").orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        if (!queryDocumentSnapshots.isEmpty()) {
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
                        Toast.makeText(view.getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show();
                    }
                });
        return view;
    }

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
            if (logsArrayList.get(position).isShowMenu()){
                return SHOW_MENU;
            } else {
                return HIDE_MENU;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType == HIDE_MENU){
                Places.initialize(parent.getContext(), "AIzaSyBEP24tbWMHcUY75yXzCBySAGKXF2ZoJ8A");
                placesClient = Places.createClient(parent.getContext());
                return new LogViewHolder(FragmentLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {
                return new MenuViewHolder(FragmentLogMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
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

                String placeID = log.getPlaceID();
                final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    double latitude,longitude;
                    Place place = response.getPlace();
                    ((LogViewHolder) holder).getFuelStationTV().setText(place.getName());
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
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
                AtomicReference<Double> latitude = new AtomicReference<>((double) 0);
                AtomicReference<Double> longitude = new AtomicReference<>((double) 0);
                AtomicReference<String> fuelStationName = new AtomicReference<>();
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
                        Intent editIntent = new Intent(getContext(), ManualEntryActivity.class);
                        editIntent.putExtra("DocID", log.getId());
                        editIntent.putExtra("DateEdit", log.getDate());
                        editIntent.putExtra("TimeEdit", log.getTime());
                        editIntent.putExtra("PlaceIDEdit", placeID);
                        editIntent.putExtra("CostEdit", log.getTotal_cost());
                        editIntent.putExtra("CapacityEdit", log.getGallons_of_fuel());
                        editIntent.putExtra("TypeEdit", log.getFuel_type());
                        editIntent.putExtra("OdometerEdit", log.getOdometer_reading());
                        editIntent.putExtra("EconomyEdit", log.getMiles_per_gallon());
                        startActivity(editIntent);
                    }
                });

                ((MenuViewHolder) holder).getShareOption().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
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
                        String intent_txt = date_txt + "\n" + time_txt + "\n" + fs_txt + "\n" + cost_txt + "\n" + capacity_txt + "\n" + rate_txt + "\n" + type_txt + "\n" + odometer_txt + "\n" + economy_txt + "\n" + ad_txt;
                        sendIntent.putExtra(Intent.EXTRA_TEXT, intent_txt);
                        sendIntent.putExtra(Intent.EXTRA_TITLE, "Fuel Log Entry from Fuel Finder");
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