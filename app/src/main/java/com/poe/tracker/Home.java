package com.poe.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;


public class Home extends AppCompatActivity {

    //Declaration
    //Firebase Auth
    private FirebaseAuth firebaseAuth;

    //array list to store the collections from the database
    private ArrayList<ModelCollection> collectionArrayList;

    //adapter
    private AdapterCollections adapterCollections;

    ImageButton ibSettings, ibAddCollection,ibAchievements, ibGame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homescreen);

        ibSettings = findViewById(R.id.ib_SettingsHome);
        ibAddCollection = findViewById(R.id.ib_AddCollection);
        ibGame = findViewById(R.id.ib_Game);
        ibAchievements = findViewById(R.id.ib_Achievement);
        EditText et_Collection_Search = findViewById(R.id.et_search_home);

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadCollections();

        //edit text search function
        et_Collection_Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when the user types each letter
                try {
                    adapterCollections.getFilter().filter(s);
                }
                catch (Exception ignored){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //nothing will happen here
            }
        });

        ibGame.setOnClickListener(v ->
                startActivity(new Intent(Home.this, Before_Game.class)));

        ibAchievements.setOnClickListener(v -> startActivity(new Intent(Home.this, Achievements.class)));

        //Navigates to the settings screen
        ibSettings.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, Profile_Settings.class));
            finish();
        });

        //Navigates to the add collection screen
        ibAddCollection.setOnClickListener(v -> startActivity(new Intent(Home.this, AddCollection.class)));


    }

    private void loadCollections() {

        //Arraylist
        collectionArrayList = new ArrayList<>();

        //get all the collections from firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("collections").child(""+firebaseAuth.getUid());
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
                adapterCollections = new AdapterCollections(Home.this,collectionArrayList);

                ImageView ivSad_Dude = findViewById(R.id.ivSadDude);
                TextView tvSadDudeMonologue = findViewById(R.id.tvSadDudeMonologue);
                TextView tvSadDudeInstruction = findViewById(R.id.tvSadDudeInstruction);
                EditText tvSearch = findViewById(R.id.et_search_home);

                //Shows or hides views depending if there are collections to show
                if(collectionArrayList.size() == 0)
                    {
                        ivSad_Dude.setVisibility(View.VISIBLE);
                        tvSadDudeMonologue.setVisibility(View.VISIBLE);
                        tvSadDudeInstruction.setVisibility(View.VISIBLE);
                        tvSearch.setVisibility(View.GONE);

                    }
                    else
                        {
                            ivSad_Dude.setVisibility(View.GONE);
                            tvSadDudeMonologue.setVisibility(View.GONE);
                            tvSadDudeInstruction.setVisibility(View.GONE);
                            tvSearch.setVisibility(View.VISIBLE);
                        }

                //set adapter to recyclerview
                RecyclerView rvCollection = findViewById(R.id.rv_AllCollections);
                rvCollection.setAdapter(adapterCollections);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




}

//
//    Reference list
//
//    Ajay S (2013). How to close activity and go back to previous activity in android. [online] Stack Overflow. Available at: https://stackoverflow.com/a/15393947 [Accessed 2 Jun. 2021].
//
//        Chen, S. (2020). material design - Android Background Drawable Not Working in Button Since Android Studio 4.1. [online] Stack Overflow. Available at: https://stackoverflow.com/a/65095656.
//
//        Paradva, N. (2020). java - What do I use now that Handler() is deprecated? [online] Stack Overflow. Available at: https://stackoverflow.com/a/63851895 [Accessed 2 Jun. 2021].
//
//        Pervaiz, A. (2021). Book App Firebase | 03 Add Book Category | Android Studio | Java. [online] www.youtube.com. Available at: https://youtu.be/TkBos_Flc4k?list=PLs1bCj3TvmWmtQbEzNewkf-UhBJ2pFr5n [Accessed 2 Jun. 2021].
//
//        Vujovic, F. (2015). How To Create Pop Up Window In Android - Faster Method. [online] www.youtube.com. Available at: https://www.youtube.com/watch?v=wxqgtEewdfo [Accessed 2 Jun. 2021].