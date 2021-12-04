package com.poe.tracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.poe.tracker.databinding.ImageCollectionRowBinding;
import com.poe.tracker.databinding.NoImageCollectionRowBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdapterGame extends RecyclerView.Adapter {

    private final Context context;

    public ArrayList<ModelCollection> collectionArrayList;

    //Binding
    private ImageCollectionRowBinding binding;
    private NoImageCollectionRowBinding noBinding;

    public AdapterGame(Context context, ArrayList<ModelCollection> collectionArrayList) {
        this.context = context;
        this.collectionArrayList = collectionArrayList;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //binding collection_row.xml
        if(viewType == 0){
            binding = ImageCollectionRowBinding.inflate(LayoutInflater.from(context),parent,false);
            return new AdapterGame.CollectionHolderOne(binding.getRoot());
        }
        noBinding = NoImageCollectionRowBinding.inflate(LayoutInflater.from(context),parent,false);
        return new AdapterGame.CollectionHolderTwo(noBinding.getRoot());
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
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        //Get data
        ModelCollection collection_model = collectionArrayList.get(position);
        String collection_id = collection_model.getCollection_id();
        String collection_name = collection_model.getCollection_name();

        String collectionUrl = collection_model.getUrl();
        if(!collectionUrl.equals("")){

            AdapterGame.CollectionHolderOne collectionHolderOne = (AdapterGame.CollectionHolderOne) holder;

            collectionHolderOne.tvCollections.setText(collection_name);
            collectionHolderOne.tvGoalTitle.setVisibility(View.GONE);
            collectionHolderOne.tvGoalAmount.setVisibility(View.GONE);



            loadImageFromUrl(collection_model, collectionHolderOne);

            collectionHolderOne.cv_Collection.setOnClickListener(v -> {

            });

            //Triggers when holding on collection view
            collectionHolderOne.cv_Collection.setOnLongClickListener(v -> true);

        }
        else {
            AdapterGame.CollectionHolderTwo collectionHolderTwo = (AdapterGame.CollectionHolderTwo) holder;
            collectionHolderTwo.tvCollections.setText(collection_name);

                collectionHolderTwo.tvGoalTitle.setVisibility(View.GONE);
                collectionHolderTwo.tvGoalAmount.setVisibility(View.GONE);


            collectionHolderTwo.cv_Collection.setOnClickListener(v -> {

            });

            ////Triggers when holding on collection view
            collectionHolderTwo.cv_Collection.setOnLongClickListener(v -> true);
        }
    }

    @Override
    public int getItemCount() {
        return collectionArrayList.size();
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

    private void loadImageFromUrl(ModelCollection collection_model, AdapterGame.CollectionHolderOne holder) {
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


}
