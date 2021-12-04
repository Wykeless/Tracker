package com.poe.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Show_item_details extends AppCompatActivity {

    //Declaration
    private ProgressDialog progressDialog;

    private String collection_id,item_id,globalItemUrl,globalNewImageUrl;
    private FirebaseAuth mAuth;
    private Uri globalItemUri;

    private int count = 0;

    private static final int REQUEST_IMAGE_CAPTURE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_item_details);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //Declaration
        ImageButton ibEditItemDetails = findViewById(R.id.ib_editItemDetails);
        ImageView ivCollectionItemBanner = findViewById(R.id.iv_collectionItemBanner);
        ImageView ivItemImage = findViewById(R.id.ivItemImageDetails);
        ImageView ivItemEditImage = findViewById(R.id.ivItemEditImageDetails);
        EditText etItemName = findViewById(R.id.et_itemEditNameDetails);
        EditText etItemDesc = findViewById(R.id.et_itemEditDescDetails);
        EditText etItemDate = findViewById(R.id.et_ItemDateDetails);
        EditText etItemEditDate = findViewById(R.id.et_ItemEditDateDetails);

        Button btnSaveDetails = findViewById(R.id.btn_SaveNewDetails);
        ImageButton ibEditRemoveItemDate = findViewById(R.id.editRemoveItemDate);
        ImageButton ibEditRemoveItemImage = findViewById(R.id.editRemoveItemImage);

        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(etItemDate,etItemEditDate, myCalendar);
        };

        ibEditRemoveItemDate.setOnClickListener(v -> {
            etItemDate.setText("");
            etItemEditDate.setText("");
        });

        ibEditRemoveItemImage.setOnClickListener(v -> {
            ivItemImage.setImageResource(R.drawable.ic_add_photo);
            ivItemEditImage.setImageResource(R.drawable.ic_add_photo);
            count=0;
            count+=3;
        });

        //gets the collection data sent from the selected collection on the home page
            if(getIntent().getExtras() != null)
                {
                    //Retrieves data sent from the adapterItems class
                    collection_id = getIntent().getStringExtra("collection_id");
                    item_id = getIntent().getStringExtra("item_id");
                    String collectionURL = getIntent().getStringExtra("collection_url");
                    String item_name = getIntent().getStringExtra("item_name");
                    String item_desc = getIntent().getStringExtra("item_desc");
                    String item_date = getIntent().getStringExtra("item_date");
                    String item_imageUrl = getIntent().getStringExtra("item_imageUrl");

                    globalItemUrl = item_imageUrl;

                    //set the item image using url
                    if(item_imageUrl.equals("")){
                        ivItemImage.setImageResource(R.drawable.ic_add_photo);
                        ivItemEditImage.setImageResource(R.drawable.ic_add_photo);
                    }
                    else{
                            Glide
                                 .with(this)
                                 .load(item_imageUrl)
                                 .centerCrop()
                                 .placeholder(R.drawable.image_holder)
                                 .into(ivItemImage);

                            Glide
                                 .with(this)
                                 .load(item_imageUrl)
                                 .centerCrop()
                                 .placeholder(R.drawable.image_holder)
                                  .into(ivItemEditImage);
                    }

                    if(collectionURL.equals("")){
                        ivCollectionItemBanner.setVisibility(View.GONE);
                    }
                    else{
                        Glide
                                .with(this)
                                .load(collectionURL)
                                .centerCrop()
                                .placeholder(R.drawable.image_holder)
                                .into(ivCollectionItemBanner);
                    }

                    //Setting text
                    etItemName.setText(item_name);
                    etItemDesc.setText(item_desc);
                    etItemDate.setText(item_date);
                    etItemEditDate.setText(item_date);
                }

        //click event for the pencil image button
        ibEditItemDetails.setOnClickListener(v -> {
            etItemName.setClickable(true);
            etItemName.setCursorVisible(true);
            etItemName.setFocusable(true);
            etItemName.setFocusableInTouchMode(true);

            etItemDesc.setClickable(true);
            etItemDesc.setCursorVisible(true);
            etItemDesc.setFocusable(true);
            etItemDesc.setFocusableInTouchMode(true);

            etItemDate.setVisibility(View.GONE);
            etItemEditDate.setVisibility(View.VISIBLE);

            ivItemImage.setVisibility(View.GONE);
            ivItemEditImage.setVisibility(View.VISIBLE);

            ibEditRemoveItemDate.setVisibility(View.VISIBLE);
            ibEditRemoveItemImage.setVisibility(View.VISIBLE);

            btnSaveDetails.setVisibility(View.VISIBLE);
        });
            btnSaveDetails.setOnClickListener(v -> {
                String userId = mAuth.getUid();
                    validate(userId);
            });


            etItemEditDate.setOnClickListener(v -> new DatePickerDialog(Show_item_details.this, R.style.DatePicker2,date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show());



        ivItemEditImage.setOnClickListener(v -> {


            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Show_item_details.this, R.style.CustomBottomSheetDialogTheme);
            View sheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.options_image_sheet, (LinearLayout) findViewById(R.id.imageOptionDialog));


            sheetView.findViewById(R.id.btn_add_a_url).setOnClickListener(v1 -> {
                showCustomDialog();
                bottomSheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btn_open).setOnClickListener(v12 -> {

                if (ActivityCompat.checkSelfPermission(Show_item_details.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                    // Request permission - this is asynchronous
                    ActivityCompat.requestPermissions(Show_item_details.this,
                            permissions, REQUEST_IMAGE_CAPTURE_PERMISSION);
                } else {
                    //We have permission, so take the photo or pick one from gallery
                    startCropActivity();
                }
                bottomSheetDialog.dismiss();
            });
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();

        });
    }


    private void validate(String uid) {
        EditText et_Item_name = findViewById(R.id.et_itemEditNameDetails);
        // EditText et_Item_date = findViewById(R.id.et_ItemEditDateDetails);

        //get data
        String itemEdit_name = et_Item_name.getText().toString().trim();


        if (TextUtils.isEmpty(itemEdit_name)) {
            Toast.makeText(Show_item_details.this,"Please enter item name.",Toast.LENGTH_SHORT).show();
        }
        else {
            //Confirms if user wants to update item
            confirmDialog(uid);
        }

    }

    //Function to display the custom dialog.
    @SuppressLint("SetTextI18n")
    private void confirmDialog(String uid) {
        Dialog dialog = new Dialog(Show_item_details.this);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.setCanceledOnTouchOutside(false);
        //Yes its called delete dialog
        dialog.setContentView(R.layout.custom_dialog);

        //Initializing the views of the dialog.
        TextView title = dialog.findViewById(R.id.holderText);
        Button logout = dialog.findViewById(R.id.btn_delete_);
        TextView cancel = dialog.findViewById(R.id.btn_cancel);


        title.setText("Update Item?");
        logout.setText("Update");

        logout.setOnClickListener(v -> {
            switch(count) {
                case 0:
                    addUpdateItemInfo(globalItemUrl);
                    break;
                case 1:
                    addUpdateItemInfo(globalNewImageUrl);
                    break;
                case 2:
                    Uri imageUri = globalItemUri;
                    uploadImageToStorage(uid,imageUri);
                    break;
                case 3:
                    addUpdateItemInfo("");
                    break;
            }
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    //Function to display the custom dialog.
    private void showCustomDialog() {
        Dialog dialog = new Dialog(Show_item_details.this);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.setCanceledOnTouchOutside(false);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.url_dialog);

        //Initializing the views of the dialog.
        EditText imageUrl = dialog.findViewById(R.id.et_image_url);
        Button addUrl = dialog.findViewById(R.id.btn_add_url);
        TextView cancelUrl = dialog.findViewById(R.id.btn_cancel_url);


        addUrl.setOnClickListener(v -> {

            String urlImage = imageUrl.getText().toString().trim();
            String errorMessage = "Url provided is not a image";
            //Checks if the link provided is valid
            if(Patterns.WEB_URL.matcher(urlImage).matches() && !TextUtils.isEmpty(urlImage)
                    && urlImage.contains("http://") || urlImage.contains("https://")){

                //A solution to doing http calls in main ui (wrong but eh)
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); //(Řádek, 2020)
                StrictMode.setThreadPolicy(policy);

                //Connects to url
                URL url = null; //(Javi, 2011)
                try {
                    url = new URL(imageUrl.getText().toString().trim());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection connection = null;
                try {
                    assert url != null;
                    connection = (HttpURLConnection)  url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    assert connection != null;
                    connection.setRequestMethod("HEAD");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
                try {
                    connection.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    //Gets the content type
                    String contentType = connection.getContentType();
                    ImageButton ibRemoveItemImage = findViewById(R.id.editRemoveItemImage);

                    //If the content type is an image it sets the image view
                    if(contentType.startsWith("image/")){
                        ImageView ivEditItemImage = findViewById(R.id.ivItemEditImageDetails);
                        ImageView ivItemImage = findViewById(R.id.ivItemImageDetails);

                        Glide   .with(Show_item_details.this)
                                .load(url)
                                .centerCrop()
                                .placeholder(R.drawable.image_holder)
                                .into(ivEditItemImage);

                        Glide   .with(Show_item_details.this)
                                .load(url)
                                .centerCrop()
                                .placeholder(R.drawable.image_holder)
                                .into(ivItemImage);

                        globalNewImageUrl = String.valueOf(url).trim();
                        count=0;
                        count=1;
                        ibRemoveItemImage.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                    else{
                        Toast.makeText(Show_item_details.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    Toast.makeText(Show_item_details.this, errorMessage, Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Toast.makeText(Show_item_details.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        cancelUrl.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void startCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                ImageView ivItemImage = findViewById(R.id.ivItemImageDetails);
                ImageView ivItemEditImage = findViewById(R.id.ivItemEditImageDetails);
                ImageButton ibEditRemoveItemImage = findViewById(R.id.editRemoveItemImage);
                Glide.with(this)
                        .load(resultUri)
                        .centerCrop()
                        .into(ivItemImage);
                Glide.with(this)
                        .load(resultUri)
                        .centerCrop()
                        .into(ivItemEditImage);
                globalItemUri = resultUri;
                count=0;
                count+=2;
                ibEditRemoveItemImage.setVisibility(View.VISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateLabel(EditText itemDate,EditText itemEditDate, Calendar myCalendar) {
        String myFormat = "dd MMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        itemDate.setText(sdf.format(myCalendar.getTime()));
        itemEditDate.setText(sdf.format(myCalendar.getTime()));
    }

    //Method to upload the images chosen to the firebase storage
    private void uploadImageToStorage(String uid, Uri imageUri) {

        //show progress
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();
        
        //time
        long timestamp = System.currentTimeMillis();

        //path to image in firebase storage
        String filePathAndName = "Images/"+uid+"/"+"Items/"+timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    //get image url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    String uploadedImageUrl = ""+uriTask.getResult();

                    //upload to firebase db
                    addUpdateItemInfo(uploadedImageUrl);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(Show_item_details.this,
                                "Image upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    //Method for adding data to firebase database
    private void addUpdateItemInfo(String uploadedImageUrl) {
        progressDialog.setMessage("Updating item...");
        progressDialog.show();

        EditText et_ItemEdit_name = findViewById(R.id.et_itemEditNameDetails);
        EditText et_ItemEdit_desc = findViewById(R.id.et_itemEditDescDetails);

        EditText et_Item_date = findViewById(R.id.et_ItemDateDetails);
        EditText et_ItemEdit_date = findViewById(R.id.et_ItemEditDateDetails);

        ImageView ivItemImage = findViewById(R.id.ivItemImageDetails);
        ImageView ivItemEditImage = findViewById(R.id.ivItemEditImageDetails);

        ImageButton ibEditRemoveItemDate = findViewById(R.id.editRemoveItemDate);
        ImageButton ibEditRemoveItemImage = findViewById(R.id.editRemoveItemImage);

        Button btnSaveDetails = findViewById(R.id.btn_SaveNewDetails);

        //get data
        String itemNew_name = et_ItemEdit_name.getText().toString().trim();
        String itemNew_desc = et_ItemEdit_desc.getText().toString().trim();
        String itemNew_date = et_ItemEdit_date.getText().toString().trim();


        //Setup info to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("item_name",""+itemNew_name);
        hashMap.put("item_desc",""+itemNew_desc);
        hashMap.put("item_acquisition_date",""+itemNew_date);
        hashMap.put("url",""+uploadedImageUrl);

        //updating data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("items").child(collection_id);
        ref.child(item_id).updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(Show_item_details.this,"Item successfully updated",Toast.LENGTH_SHORT).show();

                    et_ItemEdit_name.setClickable(false);
                    et_ItemEdit_name.setCursorVisible(false);
                    et_ItemEdit_name.setFocusable(false);
                    et_ItemEdit_name.setFocusableInTouchMode(false);

                    et_ItemEdit_desc.setClickable(false);
                    et_ItemEdit_desc.setCursorVisible(false);
                    et_ItemEdit_desc.setFocusable(false);
                    et_ItemEdit_desc.setFocusableInTouchMode(false);

                    et_Item_date.setVisibility(View.VISIBLE);
                    et_ItemEdit_date.setVisibility(View.GONE);
                    ivItemImage.setVisibility(View.VISIBLE);
                    ivItemEditImage.setVisibility(View.GONE);

                    ibEditRemoveItemDate.setVisibility(View.GONE);
                    ibEditRemoveItemImage.setVisibility(View.GONE);
                    btnSaveDetails.setVisibility(View.GONE);

                }).addOnFailureListener(e -> {
                    //Failing to add collection
                    progressDialog.dismiss();
                    Toast.makeText(Show_item_details.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                });

    }


}