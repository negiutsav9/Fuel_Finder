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

public class AddLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting up action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_addlog);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        getSupportActionBar().setElevation(0);

        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        setContentView(R.layout.activity_add_log);

        ImageView backButton = getSupportActionBar().getCustomView().findViewById(R.id.BackButton);
        backButton.setOnClickListener((View v) -> {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
        });

        Button scanButton = findViewById(R.id.ScanButton);
        scanButton.setOnClickListener((View v) -> {
            startActivity(new Intent(getApplicationContext(), ScanActivity.class));
        });

        Button typeButton = findViewById(R.id.TypeButton);
        typeButton.setOnClickListener((View v) -> {
            startActivity(new Intent(getApplicationContext(), ManualEntryActivity.class));
        });

    }
}