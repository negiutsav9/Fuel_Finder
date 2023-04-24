package com.example.fuelfinder;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import android.util.Log;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class LogSummaryFragment extends Fragment {
    private GraphView graphView;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy");

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

                        long currUnixTime = System.currentTimeMillis() / 1000;
                        // determine whether this year is a leap year
                        Instant instant = Instant.ofEpochSecond(currUnixTime);
                        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        int year = dateTime.getYear();
                        boolean isLeap = Year.of(year).isLeap();
                        final long SECONDS_IN_LEAP_YEAR = 31622400;
                        final long SECONDS_IN_NON_LEAP_YEAR = 31536000;
                        long secondsInYear = isLeap ? SECONDS_IN_LEAP_YEAR : SECONDS_IN_NON_LEAP_YEAR;

                        // parse data in firestore database
                        for (DocumentSnapshot d : logData) {
                            LogModel log = d.toObject(LogModel.class);
                            long logUnixTime = log.stringToDate().getTime(); // unix time when log was created (ignoring time of day)
                            // only want to display data in the last year
                            if(currUnixTime - logUnixTime < secondsInYear)
                                logDataFinal.add(log);
                        }
                        Collections.sort(logDataFinal);     // sort the log data
                        graphView = returnValue.findViewById(R.id.graph);
                        DataPoint[] dataPoints = new DataPoint[logDataFinal.size()];
                        Date[] dateArray = new Date[logDataFinal.size()];
                        String[] stringDates = new String[logDataFinal.size()];
                        for (int i = 0; i < logDataFinal.size(); i++) {
                            double curCost = logDataFinal.get(i).getTotal_cost();
                            String dateString = logDataFinal.get(i).getDate();
                            String numberOnly= dateString.replaceAll("[^0-9]", "");
                            // FIXME: check days with 1 digit
                            String dayString = numberOnly.substring(0, 2);
                            String yearString = numberOnly.substring(4, 6);
                            Log.d("stringDates", "TEST");
                            // Handles x-axis labeling for dates
                            if (dateString.contains("January")) {
                                stringDates[i] = "1/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("February")) {
                                stringDates[i] = "2/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("March")) {
                                stringDates[i] = "3/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("April")) {
                                if (i == 0 || i == (logDataFinal.size() - 1)) {
                                    stringDates[i] = "4/" + dayString + "/" + yearString;
                                    Log.d("stringDates", stringDates[i]);
                                }
                            }
                            else if (dateString.contains("May")){
                                stringDates[i] = "5/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("June")){
                                stringDates[i] = "6/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("July")){
                                stringDates[i] = "7/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("August")){
                                stringDates[i] = "8/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("September")){
                                stringDates[i] = "9/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("October")){
                                stringDates[i] = "10/" + dayString + "/" + yearString;
                            }
                            else if (dateString.contains("November")){
                                stringDates[i] = "11/" + dayString + "/" + yearString;
                            }
                            else {
                                stringDates[i] = "12/" + dayString + "/" + yearString;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                            LocalDate localDate = LocalDate.parse(dateString, formatter);
                            Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                            dateArray[i] = curDate;
                            DataPoint dataPoint = new DataPoint(curDate, curCost);
                            dataPoints[i] = dataPoint;
                        }
                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);

                        // Set X-axis label formatter
                        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                            @Override
                            public String formatLabel(double value, boolean isValueX) {
                                if (isValueX) {
                                    // Convert Unix time to date format
                                    Date date = new Date((long) value);
                                    return dateFormat.format(date);
                                } else {
                                    // Use default formatting for Y-axis labels
                                    return super.formatLabel(value, isValueX);
                                }
                            }
                        });

                        graphView.addSeries(series);
                        series.setColor(Color.parseColor("#F44336"));
                        series.setDrawDataPoints(true);

                        // bounds the y-axis to max and min cost
//                        graphView.getViewport().setMinY(logDataFinal.get(0).getTotal_cost());
//                        graphView.getViewport().setMaxY(logDataFinal.get(logDataFinal.size() - 1).getTotal_cost());
//                        graphView.getViewport().setYAxisBoundsManual(true);

//                        graphView.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
//                        graphView.getViewport().setScrollable(true);  // activate horizontal scrolling
//                        graphView.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
//                        graphView.getViewport().setScrollableY(true);  // activate vertical scrolling

                        // StaticLabelsFormatter needs at least 2 labels for x-axis - this handles that case
                        if (logDataFinal.size() == 1) {
                            Toast.makeText(getActivity().getApplicationContext(), "Can not plot data with 1 log. Must have at least 2 logs", Toast.LENGTH_LONG).show();
                        }
                        else {
                            // use static labels for horizontal labels
//                            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//                            staticLabelsFormatter.setHorizontalLabels(stringDates);
//                            graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

                        }
                        // hide the x-axis
                        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

                        graphView.getViewport().setScrollable(true);
                        graphView.getViewport().setScalable(true);  // activate horizontal zooming and scrolling

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