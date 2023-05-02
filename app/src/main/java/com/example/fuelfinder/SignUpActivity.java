package com.example.fuelfinder;

// importing the required classes
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

    // Declaring required variables
    ActivitySignUpBinding binding;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // hiding the action bar at the top of the screen
        getSupportActionBar().hide();

        // sets color of navigation bar to default orange-red
        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        // call parent
        super.onCreate(savedInstanceState);
        // inflate layout
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // get instances of FirebaseAuth and FirebaseFirestore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // create new progress bar
        progressBar = new ProgressBar(this);    // visible when user clicks sign up button

        binding.signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.name.getText().toString();
                String email = binding.email.getText().toString().trim();
                String password = binding.password.getText().toString();
                String confirm_password = binding.confirmPassword.getText().toString();

                // checking if entered passwords match
                if(password.equals(confirm_password)){
                    // displays progress bar
                    progressBar.setVisibility(View.VISIBLE);
                    // creates user with Firebase Authentication
                    firebaseAuth.createUserWithEmailAndPassword(email,password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class) );
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignUpActivity.this,"User Created", Toast.LENGTH_LONG).show();
                                    // user name and email stored in 'User' collection of FirebaseFirestore
                                    firebaseFirestore.collection("User")
                                            .document(FirebaseAuth.getInstance().getUid())
                                            .set(new UserModel(name, email));
                                }
                            })
                            // user creation fails 
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@androidx.annotation.NonNull Exception e) {
                                    // display error message as Toast
                                    Toast.makeText(SignUpActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                    // hides progress bar
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                // Failure message
                } else {
                    binding.warning.setText("Passwords do not match. Try Again!");
                }
            }
        });

        // the 'onClick' listener of the 'Sign In' button starts the 'LogActivity
        binding.signIn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
}
