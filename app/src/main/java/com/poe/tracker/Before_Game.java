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

public class Before_Game extends AppCompatActivity {

    //array list to store the collections from the database
    private ArrayList<ModelCollection> collectionArrayList;

    //adapter
    private AdapterGame adapterGameCollections;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // Initialize Firebase Auth
        //Declaration
        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String userid = mAuth.getUid();
        loadCollections(userid);
    }

    //Loads collections with only an image
    private void loadCollections(String userid) {

        //Arraylist
        collectionArrayList = new ArrayList<>();

        //get all the collections from firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("collections").child(userid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist first before adding data to it
                collectionArrayList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCollection model = ds.getValue(ModelCollection.class);

                    //add to arraylist
                    collectionArrayList.add(model);
                }
                //setup adapter
                adapterGameCollections = new AdapterGame(Before_Game.this,collectionArrayList);

                //set adapter to recyclerview
                RecyclerView rvGame = findViewById(R.id.rv_Game);
                rvGame.setAdapter(adapterGameCollections);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}