package com.poe.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class SplashScreen extends AppCompatActivity {

    //Declaration

    //Firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Checks if user is logged in, if not takes user to the
        // Main Activity (Login/register screen) after 2 seconds
        //Method to check if user is logged in, and if so takes user to home screen
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserLoggedIn, 2000); // 2 seconds
    }

    //Method to check if user is logged in, and if they are takes user to home screen
    private void checkUserLoggedIn() {
        // Gets the current user if they logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            // User not logged in, take user to the Main Activity screen (login/register screen)
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        }
        else
            {
                // User is logged in, takes user to the home screen
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot snapshot)
                    {
                        //Get user type
                        String userType = ""+snapshot.child("userType").getValue();
                        //check user type
                        if (userType.equals("user")){
                            //this is a user, open home
                            startActivity(new Intent(SplashScreen.this, Home.class));
                            finish();
                        }
                        else if(userType.equals("admin")){
                            //this is for an admin, but still takes user to home screen (i don't need this)
                            startActivity(new Intent(SplashScreen.this, Home.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError error)
                    {
                        // Nothing happens if they cancel it
                        finish();
                    }
                });
        }
    }
}