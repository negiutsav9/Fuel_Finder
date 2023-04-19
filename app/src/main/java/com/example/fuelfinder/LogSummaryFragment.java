package com.example.fuelfinder;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fuelfinder.databinding.FragmentLogSummaryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import android.util.Log;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class LogSummaryFragment extends Fragment {
    private GraphView graphView;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnValue = inflater.inflate(R.layout.fragment_log_summary, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        Spinner spinner = returnValue.findViewById(R.id.logDropdown);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.logDropdownOptions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        firebaseFirestore.collection("User").document(firebaseAuth.getUid()).collection("Logs").orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> logData = queryDocumentSnapshots.getDocuments();
                        List<LogModel> logDataFinal = new ArrayList<>();
                        for (DocumentSnapshot d : logData) {
                            logDataFinal.add(d.toObject(LogModel.class));
                        }
                        Collections.sort(logDataFinal);
                        graphView = returnValue.findViewById(R.id.graph);
                        DataPoint[] dataPoints = new DataPoint[logDataFinal.size()];
                        Date[] dateArray = new Date[logDataFinal.size()];
                        for (int i = 0; i < logDataFinal.size(); i++) {
                            double curCost = logDataFinal.get(i).getTotal_cost();
                            String dateString = logDataFinal.get(i).getDate();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                            LocalDate localDate = LocalDate.parse(dateString, formatter);
                            Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                            dateArray[i] = curDate;
                            DataPoint dataPoint = new DataPoint(curDate, curCost);
                            dataPoints[i] = dataPoint;
                        }
                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
                        graphView.addSeries(series);
                        series.setColor(Color.parseColor("#F44336"));
//                        graphView.getViewport().setXAxisBoundsManual(true);
//                        graphView.getViewport().setMinX(0);
//                        graphView.getViewport().setMaxX(1);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return returnValue;
    }
}