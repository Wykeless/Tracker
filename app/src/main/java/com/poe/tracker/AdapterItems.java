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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.poe.tracker.databinding.ImageGridItemBinding;
import com.poe.tracker.databinding.NoImageGridItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdapterItems extends RecyclerView.Adapter implements Filterable {


    private final Context context;
    public ArrayList<ModelItems> itemsArrayList,filterList;
    //binding
    private ImageGridItemBinding binding;
    private NoImageGridItemBinding noBinding;
    //instance of our filter class
    private FilterItems filter;


    public AdapterItems(Context context, ArrayList<ModelItems> itemsArrayList) {
        this.context = context;
        this.itemsArrayList = itemsArrayList;
        this.filterList = itemsArrayList;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding image_grid_item.xml
        //binding no_image_grid_item.xml
        if(viewType == 0){
            binding = ImageGridItemBinding.inflate(LayoutInflater.from(context),parent,false);
            return new AdapterItems.ItemHolderOne(binding.getRoot());
        }
        noBinding = NoImageGridItemBinding.inflate(LayoutInflater.from(context),parent,false);
        return new AdapterItems.ItemHolderTwo(noBinding.getRoot());
    }

    class ItemHolderOne extends RecyclerView.ViewHolder {

        //ui views of collection_row.xml
        TextView tvItemName;
        CardView cv_Item;
        ImageView ivItemImage;



        public ItemHolderOne(@NonNull View itemView){
            super(itemView);

            //Ui Views
            tvItemName = binding.tvItemName;
            cv_Item = binding.cvItem;
            ivItemImage = binding.ivItemImage;
        }
    }

    class ItemHolderTwo extends RecyclerView.ViewHolder {

        //ui views of collection_row.xml
        TextView tvItemName;
        CardView cv_Item;

        public ItemHolderTwo(@NonNull View itemView){
            super(itemView);

            //Ui Views
            tvItemName = noBinding.tvItemName;
            cv_Item = noBinding.cvItem;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ModelItems item_model = itemsArrayList.get(position);
        String collectionUrl = item_model.getUrl();
        if(!collectionUrl.equals("")){
            return 0;
        }
        return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //Get data
        ModelItems items_model = itemsArrayList.get(position);
        String item_id = items_model.getItem_id();
        String item_name = items_model.getItem_name();
        String item_desc = items_model.getItem_desc();
        String item_date = items_model.getItem_acquisition_date();
        String item_imageUrl = items_model.getUrl();

        // Shared preferences, gets collection id and image url send from the home screen
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String collection_id_item = preferences.getString("CollectionID", null);
        String collectionURL = preferences.getString("CollectionURL",null);

        //set data
        if(!item_imageUrl.equals("")) {

            AdapterItems.ItemHolderOne itemHolderOne = (AdapterItems.ItemHolderOne) holder;

            itemHolderOne.tvItemName.setText(item_name);

            loadImageFromUrl(items_model,itemHolderOne);

            itemHolderOne.cv_Item.setOnClickListener(v ->
                    sendData(collection_id_item,collectionURL,item_id,item_name,item_desc,item_date,item_imageUrl));

            //Triggers when long clicking on collection view
            itemHolderOne.cv_Item.setOnLongClickListener(v -> {
                openOptionsMenu(position,collection_id_item, item_id);
                return true;
            });
        }
          else {
                AdapterItems.ItemHolderTwo itemHolderTwo = (AdapterItems.ItemHolderTwo) holder;
                    itemHolderTwo.tvItemName.setText(item_name);


                itemHolderTwo.cv_Item.setOnClickListener(v -> sendData(collection_id_item, collectionURL, item_id, item_name, item_desc, item_date, item_imageUrl));

                //Triggers when long clicking on collection view
                itemHolderTwo.cv_Item.setOnLongClickListener(v -> {
                    openOptionsMenu(position, collection_id_item, item_id);
                    return true;
                });
            }
    }

    @Override
    public int getItemCount() {
        return itemsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterItems(filterList,this);
        }
        return filter;
    }

    //Method to load the image view with the images the user added
    private void loadImageFromUrl(ModelItems items_model, ItemHolderOne holder) {
        //using url we can get the file and its metadata from firebase storage
        String imageUrl = items_model.getUrl();
        Glide
                .with(context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.image_holder)
                .into(holder.ivItemImage);
    }

    private void openOptionsMenu(int position, String collection_id_item, String itemId) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);

        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(context)
                .inflate(R.layout.option_panel_sheet, null);

        bottomSheetView.findViewById(R.id.btn_edit).setVisibility(View.GONE);

        Button btn_deleteCollection = bottomSheetView.findViewById(R.id.btn_delete);
        btn_deleteCollection.setText(R.string.deleteItem);

        Button btn_shareCollection = bottomSheetView.findViewById(R.id.btn_share);
        btn_shareCollection.setText(R.string.shareItem);

        //Share collection
        bottomSheetView.findViewById(R.id.btn_share).setOnClickListener(v -> {
            ModelItems items_model = itemsArrayList.get(position);
            String imageUrl = items_model.getUrl();
            String item_name = items_model.getItem_name();
            String item_date = items_model.getItem_acquisition_date();
            String item_notes = items_model.getItem_desc();

            //Share message configuration
            String date;
            if(item_date.equals("")){
                date = "";
            }
            else{
                date = "\n\nDate i got the item:  "+item_date;
            }

            String desc;
            if(item_notes.equals(""))
            {
                desc = "";
            }
            else{
                desc = "\n\nDescription:  "+item_notes;
            }

            String imageLink;
            if(imageUrl.equals(""))
            {
                imageLink = "";
            }
            else{
                imageLink="\n\nImage link: "+imageUrl;
            }

            String shareCollectionMessage = "Item name:  "
                                            + item_name
                                            +date
                                            +desc
                                            +imageLink;

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareCollectionMessage);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            context.startActivity(shareIntent);

            bottomSheetDialog.dismiss();
        });


        //delete collection
        bottomSheetView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            confirmDialog(collection_id_item,itemId);
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    //Function to display the custom dialog.
    @SuppressLint("SetTextI18n")
    private void confirmDialog(String collection_id_item,String itemId) {
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


        title.setText("Delete Item?");
        delete.setText("Delete");

        delete.setOnClickListener(v -> {
            deleteItems(collection_id_item,itemId);
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    //Sends data to the show_item activity
    private void sendData(String collection_id_item, String collectionURL,
                          String item_id, String item_name, String item_desc,
                          String item_date, String item_imageUrl)
        {
        Intent item_info = new Intent(context, Show_item_details.class);
                item_info.putExtra("collection_id",collection_id_item);
                item_info.putExtra("collection_url", collectionURL);
                item_info.putExtra("item_id",item_id);
                item_info.putExtra("item_name",item_name);
                item_info.putExtra("item_desc", item_desc);
                item_info.putExtra("item_date", item_date);
                item_info.putExtra("item_imageUrl", item_imageUrl);
                context.startActivity(item_info);
        }

    //method to delete the item
    private void deleteItems(String id, String item_id) {

        //Firebase db > collections > user id > collections id > collection info
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("items");
        //deletes the items
        itemsRef.child(id).child(item_id)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    //Success
                }).addOnFailureListener(e -> {
                    //doesn't do anything if there is no items in the collection
                });

    }



}
