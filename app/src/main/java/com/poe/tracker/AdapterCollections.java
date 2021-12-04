package com.poe.tracker;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.poe.tracker.databinding.ImageCollectionRowBinding;
import com.poe.tracker.databinding.NoImageCollectionRowBinding;

import java.util.ArrayList;


public class AdapterCollections extends RecyclerView.Adapter implements Filterable {


    private final Context context;
    public ArrayList<ModelCollection> collectionArrayList,filterList;

    //view binding
    private ImageCollectionRowBinding binding;
    private NoImageCollectionRowBinding noBinding;
    //instance of our filter class
    private FilterCollections filter;

    public AdapterCollections(Context context, ArrayList<ModelCollection> collectionArrayList){
        this.context = context;
        this.collectionArrayList = collectionArrayList;
        this.filterList = collectionArrayList;
    }

    //View holder class to hold ui views for grid item and no image grid item
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //(Pervaiz, 2021)
        //binding collection_row.xml
        if(viewType == 0){
            binding = ImageCollectionRowBinding.inflate(LayoutInflater.from(context),parent,false);
            return new CollectionHolderOne(binding.getRoot());
        }
        noBinding = NoImageCollectionRowBinding.inflate(LayoutInflater.from(context),parent,false);
        return new CollectionHolderTwo(noBinding.getRoot());
    }


    @Override
    public int getItemViewType(int position) {
        ModelCollection collection_model = collectionArrayList.get(position);
        String collectionUrl = collection_model.getUrl();
        if(!collectionUrl.equals("")){
            return 0;
        }
        return 1;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Get data
        ModelCollection collection_model = collectionArrayList.get(position);
        String collection_id = collection_model.getCollection_id();
        String collection_name = collection_model.getCollection_name();

        int goal = collection_model.getGoal();


        //goal value
        String goalAmount = String.valueOf(goal);


        String collectionUrl = collection_model.getUrl();
        if(!collectionUrl.equals("")){

            CollectionHolderOne collectionHolderOne = (CollectionHolderOne) holder;

            collectionHolderOne.tvCollections.setText(collection_name);
            if(goal==0){
                collectionHolderOne.tvGoalTitle.setVisibility(View.GONE);
                collectionHolderOne.tvGoalAmount.setVisibility(View.GONE);
            }
            else{
                collectionHolderOne.tvGoalAmount.setText(goalAmount);
            }

            loadImageFromUrl(collection_model, collectionHolderOne);

            collectionHolderOne.cv_Collection.setOnClickListener(v -> sendDataToAnotherActivity(collection_model, collection_id, collection_name,goalAmount));

            //Triggers when long clicking on collection view
            collectionHolderOne.cv_Collection.setOnLongClickListener(v -> {
                openOptionsMenu(position, collection_id);
                return true;
            });

        }
        else {
            CollectionHolderTwo collectionHolderTwo = (CollectionHolderTwo) holder;
            collectionHolderTwo.tvCollections.setText(collection_name);
            if(goal==0){
                collectionHolderTwo.tvGoalTitle.setVisibility(View.GONE);
                collectionHolderTwo.tvGoalAmount.setVisibility(View.GONE);
            }
            else{
                collectionHolderTwo.tvGoalAmount.setText(goalAmount);
            }

            collectionHolderTwo.cv_Collection.setOnClickListener(v -> sendDataToAnotherActivity(collection_model, collection_id, collection_name,goalAmount));

            //Triggers when long clicking on collection view
            collectionHolderTwo.cv_Collection.setOnLongClickListener(v -> {
                openOptionsMenu(position, collection_id);
                return true;
            });
        }
    }


    private void openOptionsMenu(int position, String collectionId) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        //bottomSheetDialog.setCanceledOnTouchOutside(false);
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(context)
                .inflate(R.layout.option_panel_sheet, null);

        //Share collection
        bottomSheetView.findViewById(R.id.btn_share).setOnClickListener(v -> {
            ModelCollection collection_model = collectionArrayList.get(position);
            String imageUrl = collection_model.getUrl();
            String collection_name = collection_model.getCollection_name();
            String collection_goal = String.valueOf(collection_model.getGoal());
            String collection_desc = collection_model.getCollection_desc();

            //Share message configuration
            String goal;
            if(collection_goal.equals("0")){
                goal = "";
            }
            else{
                goal = "\n\nGoal:  "+collection_goal;
            }

            String desc;
            if(collection_desc.equals(""))
            {
                desc="";
            }
            else{
                desc = "\n\nDescription:  "+collection_desc;
            }

            String imageLink;
            if(imageUrl.equals(""))
            {
                imageLink="";
            }
            else{
                imageLink="\n\nImage link: "+imageUrl;
            }

            String shareCollectionMessage = "Collection name:  "+ collection_name
                                           +desc
                                           +goal
                                           +imageLink;

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareCollectionMessage);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            context.startActivity(shareIntent);

            bottomSheetDialog.dismiss();
        });

        //Edit the item
        bottomSheetView.findViewById(R.id.btn_edit).setOnClickListener(v -> {
            ModelCollection collection_model = collectionArrayList.get(position);
            String imageUrl = collection_model.getUrl();
            String collection_id = collection_model.getCollection_id();
            String collection_name = collection_model.getCollection_name();
            String collection_desc = collection_model.getCollection_desc();
            int goal = collection_model.getGoal();
            Intent collection_item = new Intent(v.getContext(), EditCollection.class); //(Ahamed, 2017)
            collection_item.putExtra("collection_id", collection_id);
            collection_item.putExtra("collection_name", collection_name);
            collection_item.putExtra("collection_desc", collection_desc);
            collection_item.putExtra("collection_goal", ""+goal);
            collection_item.putExtra("collection_imageUrl", imageUrl);
            context.startActivity(collection_item);
            bottomSheetDialog.dismiss();
        });

        //delete collection
        bottomSheetView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            confirmDialog(collectionId);
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void sendDataToAnotherActivity(ModelCollection collection_model, String collection_id, String collection_name, String goalAmount){
        String imageUrl = collection_model.getUrl();

        // Shared preferences, used to send the collection id and image url
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("CollectionID",collection_id);
        editor.putString("CollectionURL",imageUrl);
        editor.apply();

        //Sends collection information to collection_items activity
        Intent collection_item = new Intent(context.getApplicationContext(), Collection_Items.class); //(Ahamed, 2017)
        collection_item.putExtra("collection_id",collection_id);
        collection_item.putExtra("collection_name",collection_name);
        collection_item.putExtra("collection_goal", goalAmount);
        collection_item.putExtra("collection_imageUrl", imageUrl);
        context.startActivity(collection_item);
    }



    //Function to display the custom dialog.
    @SuppressLint("SetTextI18n")
    private void confirmDialog(String collectionId) {
        Dialog dialog = new Dialog(context);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.setCanceledOnTouchOutside(false);
        //Yes its called delete dialog
        dialog.setContentView(R.layout.custom_dialog);

        //Initializing the views of the dialog.
        TextView title = dialog.findViewById(R.id.holderText);
        Button delete = dialog.findViewById(R.id.btn_delete_);
        TextView cancel = dialog.findViewById(R.id.btn_cancel);


        title.setText("Delete Collection?");
        delete.setText("Delete");

        delete.setOnClickListener(v -> {
            deleteCategory(collectionId);
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    class CollectionHolderOne extends RecyclerView.ViewHolder {

        //ui views of collection_row.xml
        TextView tvCollections,tvGoalAmount,tvGoalTitle;
        CardView cv_Collection;
        ImageView ivCollectionImage;


        public CollectionHolderOne(@NonNull View itemView){
            super(itemView);

            //Ui Views
            tvCollections = binding.tvCollections;
            tvGoalTitle = binding.tvGoal;
            tvGoalAmount = binding.tvGoalAmount;
            cv_Collection = binding.cvCollection;
            ivCollectionImage = binding.ivCollectionImage;
        }
    }

    class CollectionHolderTwo extends RecyclerView.ViewHolder {

        //ui views of collection_row.xml
        TextView tvCollections,tvGoalAmount,tvGoalTitle;
        CardView cv_Collection;

        public CollectionHolderTwo(@NonNull View itemView){
            super(itemView);

            //Ui Views
            tvCollections = noBinding.tvNoImageCollectionName;
            tvGoalTitle = noBinding.tvNoImageGoal;
            tvGoalAmount = noBinding.tvNoImageGoalAmount;
            cv_Collection = noBinding.cvCollection;
        }
    }

    private void loadImageFromUrl(ModelCollection collection_model, CollectionHolderOne holder) { //(Pervaiz, 2021)
            //using url we can get the file and its metadata from firebase storage
            String imageUrl = collection_model.getUrl();
            //using the imageUrl the glide class before sets the image view
            Glide
                .with(context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.image_holder)
                .into(holder.ivCollectionImage);
    }


    private void deleteCategory(String collectionId) { //(Pervaiz, 2021)

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getUid();

        //Firebase db > collections > user id > collections id > collection info
        assert userId != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("collections").child(userId);
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("items");

        //StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference("Image/").child(""+firebaseAuth.getUid()).child("Items/");
        ref.child(collectionId)
                .removeValue()
                     .addOnSuccessListener(unused -> {
                           //successfully deletes the collection if found
                         //Toast.makeText(context,"Successfully deleted collection",Toast.LENGTH_SHORT).show();
                     })
                .addOnFailureListener(e -> {
                    //failed
                    //Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                });

        //deletes the items as well
        itemsRef.child(collectionId)
                .removeValue()
                .addOnSuccessListener(unused -> {

                }).addOnFailureListener(e -> {
            //doesn't do anything if there is no items in the collection
        });

    }

    @Override
    public int getItemCount() {
        return collectionArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCollections(filterList,this);
        }
        return filter;
    }

}
/* Reference list



 */
