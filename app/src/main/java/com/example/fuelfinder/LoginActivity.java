package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.fuelfinder.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding; //binding object for login activity layout
    FirebaseAuth firebaseAuth; //Firebase Authentication object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getColor(R.color.orange_red));
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        // If user is already logged in, start the Map Activity
        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MapActivity.class));
        }

        // On click listener for sign in button
        binding.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.email.getText().toString().trim(); //format user email
                String password = binding.password.getText().toString(); //format user password
                //Attempts to sign in with email and password
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show(); //toast message indicating successful login
                                startActivity(new Intent(LoginActivity.this, MapActivity.class)); //start map activity
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.warning.setText("Authentication Failed!: Check Username/Password"); //warning message indicating authentication failure
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        //on click listener for reset password button
        binding.resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.email.getText().toString().trim();  //get email entered by user
                if(email == null || email.equals("")) { // check if email has been entered first
                    Context context = getApplicationContext();
                    CharSequence text = "Enter your email to reset password!";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration); //toast message indicating that the user must enter an email first
                    toast.show();
                    return;
                }
                //sending reset password email to user
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LoginActivity.this, "Reset Email Sent", Toast.LENGTH_SHORT).show(); //toast message indicating that the reset password email has been sent
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); //if email sending fails, displaying error message
                            }
                        });
            }
        });

        //on click listener for sign up button
        binding.signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //starting sign up activity
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });


    }
}