package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.lang.reflect.Array;

// reference for ML: https://www.youtube.com/watch?v=-7pM5ficYoc

public class ScanActivity extends AppCompatActivity {
    ImageView pumpIV;
    String pumpStr = "";

    ImageView odoIV;
    String odoStr = "";
    ImageView mpgIV;
    String mpgStr = "";

    enum ImageType {
        PUMP,
        ODO,
        MPG,
        UNINIT
    }
    ImageType imgType = ImageType.UNINIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting up action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_scan_entry);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        getSupportActionBar().setElevation(0);

        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        ImageView backButton = getSupportActionBar().getCustomView().findViewById(R.id.BackButton);
        backButton.setOnClickListener((View v) -> finish());

        setContentView(R.layout.activity_scan);

        int imageResource = getResources().getIdentifier("@drawable/camera_icon", null, this.getPackageName());

        pumpIV = findViewById(R.id.pumpIV);
        pumpIV.setImageResource(imageResource);

        odoIV = findViewById(R.id.odoIV);
        odoIV.setImageResource(imageResource);

        mpgIV = findViewById(R.id.mpgIV);
        mpgIV.setImageResource(imageResource);

        //check app level permission is granted for Camera
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //grant the permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    public void takePumpImage(View view) {
        imgType = ImageType.PUMP;
        //open the camera => create an Intent object
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);
    }

    public void takeOdoImage(View view) {
        imgType = ImageType.ODO;
        //open the camera => create an Intent object
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);
    }

    public void takeMPGImage(View view) {
        imgType = ImageType.MPG;
        //open the camera => create an Intent object
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        //from bundle, extract the image
        Bitmap bitmap = (Bitmap) bundle.get("data");

        //set image in imageview
        if(imgType == ImageType.PUMP)
            pumpIV.setImageBitmap(bitmap);
        else if(imgType == ImageType.ODO)
            odoIV.setImageBitmap(bitmap);
        else if(imgType == ImageType.MPG) mpgIV.setImageBitmap(bitmap);
        else throw new RuntimeException("unrecognized image type");

        //process the image
        //1. create a FirebaseVisionImage object from a Bitmap object
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        //2. Get an instance of FirebaseVision
        FirebaseVision firebaseVision = FirebaseVision.getInstance();
        //3. Create an instance of FirebaseVisionTextRecognizer
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();
        //4. Create a task to process the image
        Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);
        //5. if task is success
        task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String s = firebaseVisionText.getText();

                if(imgType == ImageType.PUMP) {
                    s = processMLText(s);
                    pumpStr = s;
                    Log.d("scan:", "pump:" + s);
                    // change button color to green--value was read in
                    if(!pumpStr.equals("\n")) { // should be int + "\n" + int
                        Button gasButton = findViewById(R.id.gasPumpButton);
                        gasButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.green));
                        gasButton.setEnabled(false);
                    }
                }

                else if(imgType == ImageType.ODO) {
                    s = processMLText(s);
                    odoStr = s;
                    boolean readSuccess = true;
                    Log.d("scan:", "odo:" + s);
                    try {Integer.parseInt(odoStr);}
                    catch (NumberFormatException nfe) {readSuccess = false;}
                    if(readSuccess) { // check if it's an int
                        // change button color to green--value was read in
                        Button odoButton = findViewById(R.id.odometerButton);
                        odoButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.green));
                        odoButton.setEnabled(false);
                    }
                }
                else if(imgType == ImageType.MPG) {
                    s = processMLText(s);
                    mpgStr = s;
                    Log.d("scan:", "mpg:" + s);
                    boolean readSuccess = true;
                    try {Double.parseDouble(mpgStr);}
                    catch (NumberFormatException nfe) {readSuccess = false;}
                    if(readSuccess) { // check if it's a double
                        // change button color to green--value was read in
                        Button mpgButton = findViewById(R.id.mpgButton);
                        mpgButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.green));
                        mpgButton.setEnabled(false);
                    }
                }
                else throw new RuntimeException("unrecognized text type");
                imgType = ImageType.UNINIT;
            }
        });
        //6. if task is failure
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void continueButtonPress(View v) {
        Toast.makeText(getApplicationContext(), "Continue to Manual Entry...", Toast.LENGTH_SHORT);

        double total_cost = -1;
        try {
            total_cost = Double.valueOf(pumpStr.split("\n")[0]);
        }
        catch(NumberFormatException nfe) {}
        double fuel_refill = -1;
        try {
            fuel_refill = Double.valueOf(pumpStr.split("\n")[1]);
        }
        catch(NumberFormatException nfe) {}
        catch(ArrayIndexOutOfBoundsException ibe) {}
        int odometer = -1;
        try {
            odometer = Integer.valueOf(odoStr);;
        }
        catch(NumberFormatException nfe) {}
        double fuel_eco = -1;
        try {
            fuel_eco = Double.valueOf(mpgStr);
        }
        catch(NumberFormatException nfe) {}

        Intent manualIntent = new Intent(getApplicationContext(), ManualEntryActivity.class);
        manualIntent.putExtra("CostScan", total_cost);
        manualIntent.putExtra("CapacityScan", fuel_refill);
        Log.d("scan:", "transferringOdo:" + odometer);
        manualIntent.putExtra("OdometerScan", odometer);
        manualIntent.putExtra("EconomyScan", fuel_eco);
        startActivity(manualIntent);
    }

    private String processMLText(String text) {
        String procStr = text.trim();
        procStr = procStr.replace('I', '1');
        procStr = procStr.replace('l', '1');
        procStr = procStr.replace('o', '0');
        procStr = procStr.replace('O', '0');
        procStr = removeLinesWithNoNumbers(procStr);
        procStr = procStr.replaceAll("[^0-9\n]", "");
        procStr = procStr.replace(".", ""); // decimal points will be manually added
        boolean readPump = true;
        if(imgType == ImageType.PUMP) { // expecting cost (###.##), gallons (##.###)
            String costStr = "";
            String galStr = "";
            if(procStr.split("\n").length != 2) {
                // failed to read both gas amount and cost
                readPump = false;
            }
            if(readPump) {
                costStr = procStr.substring(0, procStr.indexOf('\n'));
                costStr = costStr.replace("\n", "");
                if(costStr.length() > 3)
                    costStr = costStr.substring(0, costStr.length() - 2) +
                        '.' + costStr.substring(costStr.length() - 3 + 1);
                try {
                    double cost = Double.valueOf(costStr);
                } catch (NumberFormatException nfe) {
                    //failed to read odometer
                }
                if(galStr.length() > 3) { // if long enough, insert decimal
                galStr = procStr.substring(procStr.indexOf('\n') + 1);
                galStr = galStr.replace("\n", "");
                galStr = galStr.substring(0, galStr.length() - 3) +
                        "." + galStr.substring(galStr.length() - 4 + 1); }
                try {
                    double gals = Double.valueOf(galStr);
                } catch (NumberFormatException nfe) {
                    //failed to read odometer
                }
            }
            return costStr + "\n" + galStr;
        }

        else if(imgType == ImageType.ODO) { // expecting odometer to be (#####)
            if(procStr.split("\n").length != 1) {
                //failed to read odometer
            }
            procStr = procStr.trim();
            try {
                double odo = Double.valueOf(procStr);
            }
            catch(NumberFormatException nfe) {
                //failed to read odometer
            }
        }

        else { // imgType == ImageType.MPG, expecting MPG to be (##.#)
            if(procStr.split("\n").length != 1) {
                //failed to read miles per gallon
            }
            procStr = procStr.trim();
            if(procStr.length() > 1) // insert decimal
                procStr = procStr.substring(0, procStr.length() - 1) +
                    '.' + procStr.substring(procStr.length() - 2 + 1);
            try {
                double mpg = Double.valueOf(procStr);
            }
            catch(NumberFormatException nfe) {
                //failed to read miles per gallon
            }
        }

        return procStr;
    }
    public static String removeLinesWithNoNumbers(String str) {
        String newStr = "";
        for(String line : str.split("\n")) {
            for(int i = 0; i < line.length(); i++) {
                if(Character.isDigit(line.charAt(i))) { // only add lines with #'s
                    newStr += line + "\n";
                    break;
                }
            }
        }

        return newStr;
    }
}