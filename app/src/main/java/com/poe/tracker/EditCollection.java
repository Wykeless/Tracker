package com.poe.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
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
import java.util.HashMap;

public class EditCollection extends AppCompatActivity {

    private String collectionId, globalUrl,globalImageUrl = null, userID;
    private ProgressDialog progressDialog;
    private EditText etName, etDesc, etGoal;
    private Uri globalUri;
    private int count = 0;
    private final int REQUEST_IMAGE_CAPTURE_PERMISSION=123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection);

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getUid();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        if(getIntent().getExtras() != null)
            {
                String collection_id = getIntent().getStringExtra("collection_id");
                String collection_name = getIntent().getStringExtra("collection_name");
                String collection_desc = getIntent().getStringExtra("collection_desc");
                String collection_goal = getIntent().getStringExtra("collection_goal");
                String collection_imageUrl = getIntent().getStringExtra("collection_imageUrl");

                collectionId = collection_id;
                globalUrl = collection_imageUrl;

                ImageButton ivCollectionBanner = findViewById(R.id.iv_editCollectionImage);
                ImageButton ibRemoveImage = findViewById(R.id.editRemoveImage);
                Button btnSaveCollectionDetails = findViewById(R.id.btn_SaveNewCollectionDetails);

                if(collection_imageUrl.equals("")){
                    ivCollectionBanner.setImageResource(R.drawable.ic_add_photo);
                    ibRemoveImage.setVisibility(View.GONE);
                }
                else{
                    //set the edit image using url
                    Glide   .with(this)
                            .load(collection_imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.image_holder)
                            .into(ivCollectionBanner);
                }

                etName = findViewById(R.id.et_editCollectionName);
                etDesc = findViewById(R.id.et_editCollectionDesc);
                etGoal = findViewById(R.id.et_editCollectionGoal);

                etName.setText(collection_name);
                etDesc.setText(collection_desc);
                etGoal.setText(collection_goal);

                ivCollectionBanner.setOnClickListener(v -> {

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(EditCollection.this, R.style.CustomBottomSheetDialogTheme);
                    View sheetView = LayoutInflater.from(getApplicationContext())
                            .inflate(R.layout.options_image_sheet,(LinearLayout)findViewById(R.id.imageOptionDialog));
                    sheetView.findViewById(R.id.btn_add_a_url).setOnClickListener(v1 -> {
                        showCustomDialog();
                        bottomSheetDialog.dismiss();
                    });

                    sheetView.findViewById(R.id.btn_open).setOnClickListener(v12 -> {
                        if (ActivityCompat.checkSelfPermission(EditCollection.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED)
                                {
                                    final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                                    // Request permission - this is asynchronous
                                    ActivityCompat.requestPermissions(EditCollection.this,
                                            permissions,REQUEST_IMAGE_CAPTURE_PERMISSION);
                                }
                                else
                                {
                                    //We have permission, so take the photo or pick one from gallery
                                    startCropActivity();
                                }
                        bottomSheetDialog.dismiss();
                    });

                    bottomSheetDialog.setContentView(sheetView);
                    bottomSheetDialog.show();
                });

                ibRemoveImage.setOnClickListener(v -> {
                    ivCollectionBanner.setImageResource(R.drawable.ic_add_photo);
                    count=0;
                    count+=3;
                    ibRemoveImage.setVisibility(View.GONE);
                });


                btnSaveCollectionDetails.setOnClickListener(v -> validate(userID));

            }

    }

    //Function to display the custom dialog.
    private void showCustomDialog() {
        Dialog dialog = new Dialog(EditCollection.this);
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

            //Checks if the link provided is valid
            if(Patterns.WEB_URL.matcher(urlImage).matches() && !TextUtils.isEmpty(urlImage)
                    && urlImage.contains("http://") || urlImage.contains("https://")){

                //A solution to doing http calls in main ui
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

                    try{
                        //Gets the content type
                        String contentType = connection.getContentType();
                        ImageButton ibRemoveImage = findViewById(R.id.editRemoveImage);

                        //If the content type is an image it sets the image view
                        if(contentType.startsWith("image/")){
                            ImageView ivCollectionBanner = findViewById(R.id.iv_editCollectionImage);
                            Glide   .with(EditCollection.this)
                                    .load(url)
                                    .centerCrop()
                                    .placeholder(R.drawable.image_holder)
                                    .into(ivCollectionBanner);
                            globalImageUrl = urlImage;
                            count=0;
                            count+=1;
                            ibRemoveImage.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(EditCollection.this, "Url provided is not a image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception e){
                        Toast.makeText(EditCollection.this, "Url provided is not a image", Toast.LENGTH_SHORT).show();
                    }


            }
            else{
                Toast.makeText(EditCollection.this, "Url provided is not a image", Toast.LENGTH_SHORT).show();
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
                ImageView ivCollectionBanner = findViewById(R.id.iv_editCollectionImage);
                ImageButton ibRemoveImage = findViewById(R.id.editRemoveImage);

                Glide.with(this)
                        .load(resultUri)
                        .centerCrop()
                        .into(ivCollectionBanner);
                globalUri = resultUri;
                count=0;
                count+=2;
                ibRemoveImage.setVisibility(View.VISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error,Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void validate(String uid) {

        EditText et_collection_name = findViewById(R.id.et_editCollectionName);

        //get data
        String collection_name = et_collection_name.getText().toString().trim();


        if (TextUtils.isEmpty(collection_name)) {
            Toast.makeText(EditCollection.this, "Please enter Collection name.", Toast.LENGTH_SHORT).show();
        }
        else {
            //Confirms that user wants to update the collection
              confirmDialog(uid);
        }

    }

    //Function to display the custom dialog.
    @SuppressLint("SetTextI18n")
    private void confirmDialog(String uid) {
        Dialog dialog = new Dialog(EditCollection.this);
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


        title.setText("Update Collection?");
        logout.setText("Update");

        logout.setOnClickListener(v -> {
            switch(count) {
                case 0:
                    addUpdateItemInfo(globalUrl);
                    break;
                case 1:
                    addUpdateItemInfo(globalImageUrl);
                    break;
                case 2:
                    Uri imageUri = globalUri;
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


    //Method to upload the images chosen to the firebase storage
    private void uploadImageToStorage(String uid, Uri imageUri) {

        //show progress
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();

        //time
        long timestamp = System.currentTimeMillis();

        //path to image in firebase storage
        String filePathAndName = "Images/"+uid+"/"+"Collections/"+timestamp;
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
                .addOnFailureListener(e -> Toast.makeText(EditCollection.this,"Image upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    //Method for adding data to firebase database
    private void addUpdateItemInfo(String uploadedImageUrl) {
        progressDialog.setMessage("Updating item...");
        progressDialog.show();

        etName = findViewById(R.id.et_editCollectionName);
        etDesc = findViewById(R.id.et_editCollectionDesc);
        etGoal = findViewById(R.id.et_editCollectionGoal);

        //get data
        String new_name = etName.getText().toString().trim();
        String new_desc = etDesc.getText().toString().trim();
        String goalString = etGoal.getText().toString().trim();
        int new_goal = 0;
        if(!goalString.equals("")){
           new_goal   = Integer.parseInt(etGoal.getText().toString().trim());
        }


        //Setup info to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("collection_name",""+new_name);
        hashMap.put("collection_desc",""+new_desc);
        hashMap.put("goal",new_goal);
        hashMap.put("url",""+uploadedImageUrl);

        //Add to firebase database...Database Root > Collections >collectionId > collection info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("collections").child(userID);
        ref.child(collectionId).updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    finish();
                    //Toast.makeText(EditCollection.this,"Collection successfully updated",Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    //Failing to add collection
                    progressDialog.dismiss();
                    //Toast.makeText(EditCollection.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                });

    }

}