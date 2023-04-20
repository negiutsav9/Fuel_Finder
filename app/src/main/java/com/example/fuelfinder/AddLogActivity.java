// import necessary packages
package com.example.fuelfinder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

// Define the class
public class AddLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the custom action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_addlog);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        getSupportActionBar().setElevation(0);

        // Set the navigation bar color
        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        // Set the content view to the activity_add_log.xml layout
        setContentView(R.layout.activity_add_log);

        // Set up the back button in the action bar
        ImageView backButton = getSupportActionBar().getCustomView().findViewById(R.id.BackButton);
        backButton.setOnClickListener((View v) -> {
            finish();
        });

        // Set up the scan button and its onClickListener
        Button scanButton = findViewById(R.id.ScanButton);
        scanButton.setOnClickListener((View v) -> {
            startActivity(new Intent(getApplicationContext(), ScanActivity.class));
        });

        // Set up the type button and its onClickListener
        Button typeButton = findViewById(R.id.TypeButton);
        typeButton.setOnClickListener((View v) -> {
            startActivity(new Intent(getApplicationContext(), ManualEntryActivity.class));
        });

    }
}