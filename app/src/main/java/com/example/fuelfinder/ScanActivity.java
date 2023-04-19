package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

// reference for ML: https://www.youtube.com/watch?v=-7pM5ficYoc

public class ScanActivity extends AppCompatActivity {

    ImageView pumpIV;
    TextView pumpTV;

    ImageView odoIV;
    TextView odoTV;

    ImageView mpgIV;
    TextView mpgTV;

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

        pumpIV = findViewById(R.id.pumpIV);
        pumpTV = findViewById(R.id.pumpTV);
        pumpTV.setText("pump text");

        odoIV = findViewById(R.id.odoIV);
        odoTV = findViewById(R.id.odoTV);
        odoTV.setText("odo text");

        mpgIV = findViewById(R.id.mpgIV);
        mpgTV = findViewById(R.id.mpgTV);
        mpgTV.setText("mpg text");

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

                if(imgType == ImageType.PUMP)
                    pumpTV.setText(s);
                else if(imgType == ImageType.ODO)
                    odoTV.setText(s);
                else if(imgType == ImageType.MPG)
                    mpgTV.setText(s);
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
    }
}