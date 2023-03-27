/**
 * Name: User.java
 *
 * Purpose: Handling the backend functions of User Activity which includes editing
 *          user name and user email, signing out from the user account
 *          and deleting the user account from Firestore
 */


package com.example.fuelfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class User extends AppCompatActivity {

    FirebaseAuth firebaseAuth;              //Firebase authorization instance
    FirebaseFirestore firebaseFirestore;    //Firebase Cloud Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //Removing Action Bar
        getSupportActionBar().hide();

        //Coloring the navigation bar to our theme color
        getWindow().setNavigationBarColor(getColor(R.color.orange_red));

        //Assigning the current instance of Firebase Authorization and Firebase Cloud Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //Getting the text field UI
        EditText textName = findViewById(R.id.name);
        EditText textEmail = findViewById(R.id.email);

        //Getting the user name and e-mail from the Firestore
        firebaseFirestore.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserModel userDetails = documentSnapshot.toObject(UserModel.class);

                        String name = userDetails.getName();
                        textName.setText(name);

                        String email = userDetails.getEmail();
                        textEmail.setText(email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(User.this,e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });


        //Getting the Bottom Navigation Bar UI
        NavigationBarView bottomNavigationView=findViewById(R.id.bottom_navigation);

        //Selecting the current navbar item as User
        bottomNavigationView.setSelectedItemId(R.id.user);

        //Listener to switch activities between User, Dashboard and Maps
        bottomNavigationView.setOnItemSelectedListener( new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.dashboard:
                        startActivity(new Intent(getApplicationContext(),Dashboard.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.user:
                        return true;
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        //Getting the buttons on User UI
        Button edit = findViewById(R.id.edit);
        Button signOut = findViewById(R.id.sign_out);
        Button del_account = findViewById(R.id.del);
        Button done = findViewById(R.id.done);

        //On-click listener for Sign Out Button
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                Toast.makeText(User.this, "Signed Out Successfully", Toast.LENGTH_LONG).show();
                startActivity(new Intent(User.this, LoginActivity.class));
            }
        });

        //On-click listener for Edit Button
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.INVISIBLE);
                signOut.setVisibility(View.INVISIBLE);
                del_account.setVisibility(View.INVISIBLE);
                done.setVisibility(View.VISIBLE);

                textName.setEnabled(true);
                textEmail.setEnabled(true);

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        edit.setVisibility(View.VISIBLE);
                        signOut.setVisibility(View.VISIBLE);
                        del_account.setVisibility(View.VISIBLE);
                        done.setVisibility(View.INVISIBLE);

                        textName.setEnabled(false);
                        textEmail.setEnabled(false);

                        firebaseFirestore.collection("User")
                                .document(FirebaseAuth.getInstance().getUid())
                                .update("name",textName.getText().toString().trim());

                        firebaseFirestore.collection("User")
                                .document(FirebaseAuth.getInstance().getUid())
                                .update("email",textEmail.getText().toString().trim());

                        firebaseAuth.getCurrentUser().updateEmail(textEmail.getText().toString().trim());
                    }
                });
            }
        });

        //On-click listener for Delete Button with Alert Dialog for User confirmation
        del_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               AlertDialog.Builder deleteDialog = new AlertDialog.Builder(User.this);
               deleteDialog.setTitle("Delete Account");
               deleteDialog.setMessage("Deleting an account will delete your logs permanently.");
               deleteDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       Toast.makeText(User.this, "Account Deleted", Toast.LENGTH_LONG).show();
                       firebaseFirestore.collection("User").document(firebaseAuth.getInstance().getUid()).delete();
                       firebaseAuth.getCurrentUser().delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            startActivity(new Intent(User.this, SignUpActivity.class));
                                        }
                                    });
                       finish();
                   }
               });
               deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       dialogInterface.dismiss();
                   }
               });
               AlertDialog alert = deleteDialog.create();
               alert.show();
               alert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.teal_200));
               alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.orange_red));
            }
        });

    }
}