package com.example.fuelfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fuelfinder.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    // View binding object for this activity
    ActivitySignUpBinding binding;
    // Firebase authentication object
    FirebaseAuth firebaseAuth;
    // Progress bar object to show progress during user creation
    ProgressBar progressBar;
    // Firebase Firestore object
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Hide action bar
        getSupportActionBar().hide();

        // Set navigation bar color
        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Initialize Firebase authentication and Firestore objects
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialize progress bar
        progressBar = new ProgressBar(this);

        // Set onClickListener for Sign Up button
        binding.signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user inputs for name, email, password, and confirm password
                String name = binding.name.getText().toString();
                String email = binding.email.getText().toString().trim();
                String password = binding.password.getText().toString();
                String confirm_password = binding.confirmPassword.getText().toString();

                // Check if password and confirm password match
                if(password.equals(confirm_password)){
                    // Show progress bar while user is being created
                    progressBar.setVisibility(View.VISIBLE);
                    // Create user with email and password using Firebase authentication
                    firebaseAuth.createUserWithEmailAndPassword(email,password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // If user is successfully created, go to login activity and show toast message
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class) );
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignUpActivity.this,"User Created", Toast.LENGTH_LONG).show();
                                    // Create a new user document in Firestore with user's name and email
                                    firebaseFirestore.collection("User")
                                            .document(FirebaseAuth.getInstance().getUid())
                                            .set(new UserModel(name, email));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@androidx.annotation.NonNull Exception e) {
                                    // If there's an error creating the user, show error message and hide progress bar
                                    Toast.makeText(SignUpActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                } else {
                    // If password and confirm password don't match, show warning message
                    binding.warning.setText("Passwords do not match. Try Again!");
                }
            }
        });

        // Set onClickListener for Sign In button
        binding.signIn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
}