package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Removing Action Bar
        getSupportActionBar().hide();

        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        // Initialize and assign variable
        NavigationBarView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.dashboard);

        TextView nameLabel = findViewById(R.id.nameLabel);

        FirebaseFirestore firebaseFireStore = FirebaseFirestore.getInstance();
        firebaseFireStore.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserModel userDetails = documentSnapshot.toObject(UserModel.class);
                    Dashboard.this.findViewById(R.id.nameLabel).post(() -> {
                        nameLabel.setText("Hi, " + userDetails.getName());
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(Dashboard.this, "Failed to load database", Toast.LENGTH_SHORT));

        bottomNavigationView.setOnItemSelectedListener( new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.dashboard:
                        return true;
                    case R.id.user:
                        startActivity(new Intent(getApplicationContext(),User.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> { // change activities to make a log screen
            startActivity(new Intent(getApplicationContext(),AddLogActivity.class));
        });
    }
}