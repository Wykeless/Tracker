package com.poe.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Collection_Items extends AppCompatActivity {

    private String collectionId, collectionGoalTotal;
    private FirebaseAuth firebaseAuth;
    private int globalItemCount;

    //array list to store the collections from the database
    private ArrayList<ModelItems> itemsArrayList;

    //adapter
    private AdapterItems adapterItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_item);



        //gets the collection data sent from the selected collection on the home page
        if( getIntent().getExtras() != null)
            {
                String collection_id = getIntent().getStringExtra("collection_id");
                String collection_name = getIntent().getStringExtra("collection_name");
                String collection_goal = getIntent().getStringExtra("collection_goal");
                String collection_imageUrl = getIntent().getStringExtra("collection_imageUrl");

                collectionId = collection_id;
                collectionGoalTotal = collection_goal;

                ImageView ivCollectionBanner = findViewById(R.id.iv_collectionBanner);
                //set the banner image url

                if(collection_imageUrl.equals("")){
                    android.view.ViewGroup.LayoutParams layoutParams = ivCollectionBanner.getLayoutParams();
                    layoutParams.height = 300;
                    ivCollectionBanner.setLayoutParams(layoutParams);
                    ivCollectionBanner.setImageResource(R.color.crimson);
                }
                else{
                    Glide
                         .with(this)
                         .load(collection_imageUrl)
                         .centerCrop()
                         .placeholder(R.drawable.image_holder)
                         .into(ivCollectionBanner);
                    ivCollectionBanner.setAlpha(0.7f);
                }

                TextView col_name= findViewById(R.id.tv_collectionName);
                col_name.setText(collection_name);
            }


        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        loadItems();

        ImageButton ibAddItem = findViewById(R.id.ib_AddItem);
        EditText et_Items_Search = findViewById(R.id.et_search_items);
        ImageButton ibSettings = findViewById(R.id.ib_Settings);

        et_Items_Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterItems.getFilter().filter(s);
                }
                catch (Exception ignored){
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ibAddItem.setOnClickListener(v -> {
            Intent collectionIdSender = new Intent(Collection_Items.this, AddItems.class);
            collectionIdSender.putExtra("collectionId", collectionId);
            startActivity(collectionIdSender);
        });

        ibSettings.setOnClickListener(v -> {
            Intent collectionIdSender = new Intent(Collection_Items.this, Profile_Settings.class);
            startActivity(collectionIdSender);
            finish();
        });

    }

    private void loadItems() {

        //Arraylist
        itemsArrayList = new ArrayList<>();

        //get all the items from firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("items").child(""+collectionId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist first before adding data to it
                itemsArrayList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelItems items_model = ds.getValue(ModelItems.class);

                    //add to arraylist
                    itemsArrayList.add(items_model);
                }
                //setup adapter
                adapterItems = new AdapterItems(Collection_Items.this,itemsArrayList);
                globalItemCount = itemsArrayList.size();
                TextView collectionGoal = findViewById(R.id.tv_collectionGoal);
                ProgressBar goalProgress = findViewById(R.id.progressBar);
                TextView goalTitle = findViewById(R.id.tv_collectionGoal3);
                ConstraintLayout goalHolder = findViewById(R.id.section2CollectionGoal);

                if(Integer.parseInt(collectionGoalTotal)==0){
                    goalProgress.setVisibility(View.GONE);
                    collectionGoal.setVisibility(View.GONE);
                    goalTitle.setVisibility(View.GONE);
                    goalHolder.setVisibility(View.GONE);
                }
                else
                {
                    String itemCount = globalItemCount+"/"+collectionGoalTotal;
                    collectionGoal.setText(itemCount);
                    goalProgress.setProgress(globalItemCount);
                    goalProgress.setMax(Integer.parseInt(collectionGoalTotal));
                }

                //Unlocks achievement
                if(globalItemCount==5){
                    updateAchievements();
                }

                //set adapter to recyclerview
                RecyclerView rvItems = findViewById(R.id.rv_AllItems);
                rvItems.setAdapter(adapterItems);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Cancels
            }
        });
    }

    private void updateAchievements(){

        //getting user id
        String userId = firebaseAuth.getUid();

        //Setup info to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("completed","true");

        assert userId != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("achievements").child(userId);
        ref.child("achi01").updateChildren(hashMap)
                .addOnSuccessListener(unused -> {

                }).addOnFailureListener(e -> {
                });
    }

}