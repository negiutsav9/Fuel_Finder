package com.example.fuelfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AddLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_add_log);

        ImageView backButton = findViewById(R.id.BackButton);
        backButton.setOnClickListener((View v) -> {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
        });
    }
}