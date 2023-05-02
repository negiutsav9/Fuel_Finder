package com.example.fuelfinder;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

public class LogSummaryFragment extends Fragment {
    //private GraphView graphView;
    private ArrayList<Toast> toastList = new ArrayList<Toast>();

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy");

    private GraphView[] graphs = new GraphView[12];
    String selectedQuantity = "Cost";
    String selectedTime = "Month";

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
        Spinner spinnerType = returnValue.findViewById(R.id.logDropdownType);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.logDropdownOptions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerType.setAdapter(adapter);

        Spinner spinnerTime = returnValue.findViewById(R.id.logDropdownTime);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.logDropdownOptions2, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTime.setAdapter(adapter2);

        firebaseFirestore.collection("User").document(firebaseAuth.getUid()).collection("Logs").orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> logData = queryDocumentSnapshots.getDocuments();
                        List<LogModel> logDataYear = new ArrayList<>();
                        List<LogModel> logDataMonth = new ArrayList<>();
                        List<LogModel> logDataAll = new ArrayList<>();

                        // get all data in firestore database, stored in associated lists by time period
                        for (DocumentSnapshot d : logData) {
                            LogModel log = d.toObject(LogModel.class);
                            long logUnixTime = log.stringToDate().getTime(); // unix time when log was created (ignoring time of day)

                            if(getInTimePeriod(logUnixTime, true))
                                logDataYear.add(log);
                            if(getInTimePeriod(logUnixTime, false))
                                logDataMonth.add(log);
                            logDataAll.add(log);
                        }
                        Collections.sort(logDataYear);     // sort the log data
                        Collections.sort(logDataMonth);
                        Collections.sort(logDataAll);

                        // graph of cost for the last month
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GCM);
                            graphs[0] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataMonth.size()];
                            Date[] dateArray = new Date[logDataMonth.size()];
                            for (int i = 0; i < logDataMonth.size(); i++) {
                                double curCost = logDataMonth.get(i).getTotal_cost();
                                String dateString = logDataMonth.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataMonth, "Cost");
                        }

                        // graph of cost for the last year
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GCY);
                            graphs[1] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataYear.size()];
                            Date[] dateArray = new Date[logDataYear.size()];
                            for (int i = 0; i < logDataYear.size(); i++) {
                                double curCost = logDataYear.get(i).getTotal_cost();
                                String dateString = logDataYear.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataYear, "Cost");
                        }

                        // graph of cost for all time
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GCA);
                            graphs[2] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataAll.size()];
                            Date[] dateArray = new Date[logDataAll.size()];
                            for (int i = 0; i < logDataAll.size(); i++) {
                                double curCost = logDataAll.get(i).getTotal_cost();
                                String dateString = logDataAll.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataAll, "Cost");
                        }
                        ////////////////
                        // graph of gallons purchased for the last month
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GGM);
                            graphs[3] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataMonth.size()];
                            Date[] dateArray = new Date[logDataMonth.size()];
                            for (int i = 0; i < logDataMonth.size(); i++) {
                                double curCost = logDataMonth.get(i).getGallons_of_fuel();
                                String dateString = logDataMonth.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataMonth, "Gallons Purchased");
                        }

                        // graph of gallons purchased for the last year
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GGY);
                            graphs[4] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataYear.size()];
                            Date[] dateArray = new Date[logDataYear.size()];
                            for (int i = 0; i < logDataYear.size(); i++) {
                                double curGals = logDataYear.get(i).getGallons_of_fuel();
                                String dateString = logDataYear.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curGals);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataYear, "Gallons Purchased");
                        }



                        // graph of gallons purchased for all time
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GGA);
                            graphs[5] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataAll.size()];
                            Date[] dateArray = new Date[logDataAll.size()];
                            for (int i = 0; i < logDataAll.size(); i++) {
                                double curCost = logDataAll.get(i).getGallons_of_fuel();
                                String dateString = logDataAll.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataAll, "Gallons Purchased");
                        }
                        //////////////////
                        // graph of odometer for the last month
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GOM);
                            graphs[6] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataMonth.size()];
                            Date[] dateArray = new Date[logDataMonth.size()];
                            for (int i = 0; i < logDataMonth.size(); i++) {
                                double curCost = logDataMonth.get(i).getOdometer_reading();
                                String dateString = logDataMonth.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataMonth, "Odometer");
                        }

                        // graph of odometer purchased for the last year
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GOY);
                            graphs[7] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataYear.size()];
                            Date[] dateArray = new Date[logDataYear.size()];
                            for (int i = 0; i < logDataYear.size(); i++) {
                                double curOdo = logDataYear.get(i).getOdometer_reading();
                                String dateString = logDataYear.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curOdo);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataYear, "Odometer");
                        }

                        // graph of odometer for all time
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GOA);
                            graphs[8] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataAll.size()];
                            Date[] dateArray = new Date[logDataAll.size()];
                            for (int i = 0; i < logDataAll.size(); i++) {
                                double curCost = logDataAll.get(i).getOdometer_reading();
                                String dateString = logDataAll.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataAll, "Odometer");
                        }
                        //////////////
                        // graph of fuel economy for the last month
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GEM);
                            graphs[9] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataMonth.size()];
                            Date[] dateArray = new Date[logDataMonth.size()];
                            for (int i = 0; i < logDataMonth.size(); i++) {
                                double curCost = logDataMonth.get(i).getMiles_per_gallon();
                                String dateString = logDataMonth.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataMonth, "Fuel Economy");
                        }

                        // graph of fuel economy for the last year
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GEY);
                            graphs[10] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataYear.size()];
                            Date[] dateArray = new Date[logDataYear.size()];
                            for (int i = 0; i < logDataYear.size(); i++) {
                                double curEco = logDataYear.get(i).getMiles_per_gallon();
                                String dateString = logDataYear.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curEco);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataYear, "Fuel Economy");
                        }

                        // graph of fuel economy for all time
                        {
                            GraphView graphView = returnValue.findViewById(R.id.GEA);
                            graphs[11] = graphView;
                            DataPoint[] dataPoints = new DataPoint[logDataAll.size()];
                            Date[] dateArray = new Date[logDataAll.size()];
                            for (int i = 0; i < logDataAll.size(); i++) {
                                double curCost = logDataAll.get(i).getMiles_per_gallon();
                                String dateString = logDataAll.get(i).getDate();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                                LocalDate localDate = LocalDate.parse(dateString, formatter);
                                Date curDate = Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                dateArray[i] = curDate;
                                DataPoint dataPoint = new DataPoint(curDate, curCost);
                                dataPoints[i] = dataPoint;
                            }
                            setUpGraph(graphView, dataPoints, logDataAll, "Fuel Economy");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedQuantity = adapterView.getItemAtPosition(i).toString();
                graphChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String spinnerSelection = adapterView.getItemAtPosition(i).toString();
                if(spinnerSelection.equals("Last Month")) selectedTime = "Month";
                else if(spinnerSelection.equals("Last Year")) selectedTime = "Year";
                else selectedTime = "All Time";
                graphChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return returnValue;
    }

    private void graphChange() {
        int visIndex = -1;
        if(selectedQuantity.equals("Cost")) {
            if(selectedTime.equals("Month")) visIndex = 0;
            else if(selectedTime.equals("Year")) visIndex = 1;
            else visIndex = 2;
        }
        else if(selectedQuantity.equals("Gallons Purchased")) {
            if(selectedTime.equals("Month")) visIndex = 3;
            else if(selectedTime.equals("Year")) visIndex = 4;
            else visIndex = 5;
        }
        else if(selectedQuantity.equals("Odometer")) {
            if(selectedTime.equals("Month")) visIndex = 6;
            else if(selectedTime.equals("Year")) visIndex = 7;
            else visIndex = 8;
        }
        else {
            if(selectedTime.equals("Month")) visIndex = 9;
            else if(selectedTime.equals("Year")) visIndex = 10;
            else visIndex = 11;
        }

        for(int index = 0; index < 12; index++) // set all to invisible->one will be set to visible according to spinners
            if(graphs[index] != null) graphs[index].setVisibility(View.INVISIBLE);

        if(graphs[visIndex] != null) graphs[visIndex].setVisibility(View.VISIBLE);
    }

    private boolean getInTimePeriod(long logUnixTime, boolean doYear) {
        long currUnixTime = System.currentTimeMillis();
        // determine whether this year is a leap year
        Instant instant = Instant.ofEpochSecond(currUnixTime);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        int year = dateTime.getYear();
        boolean isLeap = Year.of(year).isLeap();
        if(doYear) {
            final long SECONDS_IN_LEAP_YEAR = 31622400;
            final long SECONDS_IN_NON_LEAP_YEAR = 31536000;
            long secondsInYear = isLeap ? SECONDS_IN_LEAP_YEAR : SECONDS_IN_NON_LEAP_YEAR;
            return currUnixTime - logUnixTime < (secondsInYear * 1000);
        }
        else { // do month
            int month = dateTime.getMonthValue();
            int numDaysInMonth;
            switch(month) {
                case 2:
                    numDaysInMonth = isLeap ? 29 : 28;
                    break;
                case 4:
                case 9:
                case 11:
                    numDaysInMonth = 30;
                    break;
                default:
                    numDaysInMonth = 31;
            }
            long secondsInMonth = Math.abs(numDaysInMonth * 86400 * 1000);
            return currUnixTime - logUnixTime < secondsInMonth;
        }
    }

    private void setUpGraph(GraphView graphView, DataPoint[] dataPoints, List<LogModel> logs, String yLabel) {
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
        series.setThickness(10);
        series.setDataPointsRadius(20);
        series.setOnDataPointTapListener((series2, dataPoint) -> {
            // Get X and Y values of the tapped point
            Date xDate = new Date((long) dataPoint.getX());
            double x = xDate.getTime();
            double y = dataPoint.getY();

            // Do something with the values, e.g. display them in a Toast message
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy");
            if(yLabel.equals("Cost"))
                Toast.makeText(getActivity().getApplication(), "Date: " + dateFormat.format(xDate) + "\n" + yLabel + ": $" + String.format("%.2f", y), Toast.LENGTH_SHORT).show();
            else Toast.makeText(getActivity().getApplication(), "Date: " + dateFormat.format(xDate) + "\n" + yLabel + ": " + y, Toast.LENGTH_SHORT).show();
        });

        // StaticLabelsFormatter needs at least 2 labels for x-axis - this handles that case
        if (logs.size() == 1) {
            //Toast.makeText(getActivity().getApplicationContext(), "Can not plot data with 1 log. Must have at least 2 logs", Toast.LENGTH_LONG).show();
        }

        // hide the x-axis
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
    }
}