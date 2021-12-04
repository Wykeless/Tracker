package com.poe.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Achievements extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        firebaseAuth = FirebaseAuth.getInstance();
        loadAchievements();
    }

    //Firebase Auth;
    private FirebaseAuth firebaseAuth;

    //array list to store the collections from the database
    private ArrayList<ModelAchievements> achievementsArrayList;

    //adapter
    private AdapterAchievements adapterAchievements;

    private void loadAchievements(){

        //Arraylist
        achievementsArrayList = new ArrayList<>();

        //get all the collections from firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("achievements").child(""+firebaseAuth.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist first before adding data to it
                achievementsArrayList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelAchievements model = ds.getValue(ModelAchievements.class);

                    //add to arraylist
                    achievementsArrayList.add(model);
                }
                //setup adapter
                adapterAchievements = new AdapterAchievements(Achievements.this,achievementsArrayList);

                //set adapter to recyclerview
                RecyclerView rvCollection = findViewById(R.id.rv_Achievements);
                rvCollection.setAdapter(adapterAchievements);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}