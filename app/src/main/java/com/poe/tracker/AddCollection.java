package com.poe.tracker;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AddCollection extends AppCompatActivity {

    //Declaration

    //Firebase auth
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private Uri globalUri;
    private int count = 0;
    private final int REQUEST_IMAGE_CAPTURE_PERMISSION=123;

    private String globalImageUrl;

    private  String collection_name ="";
    private  String collection_desc ="";
    private  int goal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_collection);

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();


        //configure progress dialog
        progressDialog = new ProgressDialog(this); //(Pervaiz, 2021)
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        Button btnAddCollection = findViewById(R.id.btn_AddCollection);

        btnAddCollection.setOnClickListener(v -> validateData());

        ImageButton ibCollectionImage = findViewById(R.id.ib_AddCollectionImage);
        ImageButton ibRemoveImage = findViewById(R.id.addRemoveImage);

        ibCollectionImage.setOnClickListener(v -> {

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AddCollection.this, R.style.CustomBottomSheetDialogTheme);
            View sheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.options_image_sheet,(LinearLayout)findViewById(R.id.imageOptionDialog));


            sheetView.findViewById(R.id.btn_add_a_url).setOnClickListener(v1 -> {
                showCustomDialog();
                bottomSheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btn_open).setOnClickListener(v12 -> {

                if (ActivityCompat.checkSelfPermission(AddCollection.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                    // Request permission - this is asynchronous
                    ActivityCompat.requestPermissions(AddCollection.this,
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
            ibCollectionImage.setImageResource(R.drawable.ic_add_photo);
            count = 0;
            ibRemoveImage.setVisibility(View.GONE);
        });
    }

    private void startCropActivity() { //(Arthur, 2021)
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //(Arthur, 2021)
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                ImageButton ibAddCollectionImage = findViewById(R.id.ib_AddCollectionImage);
                ImageButton ibRemoveImage = findViewById(R.id.addRemoveImage);

                Glide.with(this)
                        .load(resultUri)
                        .centerCrop()
                        .into(ibAddCollectionImage);

                globalUri = resultUri;

                count=0;
                count=2;

                ibRemoveImage.setVisibility(View.VISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

                Toast.makeText(this, ""+error,Toast.LENGTH_SHORT).show();
            }
        }
    }


    //handles back button press
    @Override
    public void onBackPressed()
    {
        finish();
    }



    //Function to display the custom dialog.
    private void showCustomDialog() {
        Dialog dialog = new Dialog(AddCollection.this);
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
                    ImageButton ibRemoveImage = findViewById(R.id.addRemoveImage);

                    //If the content type is an image it sets the image view
                    if(contentType.startsWith("image/")){
                        ImageView ivCollectionBanner = findViewById(R.id.ib_AddCollectionImage);
                        Glide   .with(AddCollection.this)
                                .load(url)
                                .centerCrop()
                                .placeholder(R.drawable.image_holder)
                                .into(ivCollectionBanner);
                        globalImageUrl = String.valueOf(url).trim();
                        count=0;
                        count=1;
                        ibRemoveImage.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                    else{
                        Toast.makeText(AddCollection.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    Toast.makeText(AddCollection.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(AddCollection.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        cancelUrl.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    //Validation method //(Pervaiz, 2021)
    private void validateData() {

        EditText etCollectionName = findViewById(R.id.et_collectionName);
        EditText etCollectionDesc = findViewById(R.id.et_collectionDesc);
        EditText etGoal = findViewById(R.id.et_Goal);

        String custom_message;

        //get data
        collection_name = etCollectionName.getText().toString().trim();
        collection_desc = etCollectionDesc.getText().toString().trim();
        String goalString = etGoal.getText().toString().trim();
        //sets goal value if edit text is not empty
        if(!goalString.equals("")){
        goal = Integer.parseInt(etGoal.getText().toString().trim());
        }

        if (TextUtils.isEmpty(collection_name)) {
            custom_message = "Please enter collection name.";
            customToast(custom_message);
        }

        else {
            switch (count) {
                case 0:
                    addCollectionToFirebase("");
                    break;
                case 1:
                    addCollectionToFirebase(globalImageUrl);
                    break;
                case 2:
                    Uri imageUri = globalUri;
                    uploadImageToStorage(imageUri);
                    break;
            }

        }
    }

    //Method to upload the images chosen to the firebase storage //(Pervaiz, 2021)
    private void uploadImageToStorage(Uri imageUri) {

        // upload image to firebase storage

        //show progress
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();

        //time
        long timestamp = System.currentTimeMillis();
        String userId = firebaseAuth.getUid();

        //path to image in firebase storage
        String filePathAndName = "Images/"+userId+"/"+"Collection/"+timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    //get image url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    String uploadedImageUrl = ""+uriTask.getResult();

                    //upload to firebase db
                    addCollectionToFirebase(uploadedImageUrl);
                })
                .addOnFailureListener(e -> Toast.makeText(AddCollection.this,"Image upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    //Method for adding data to firebase database
    private void addCollectionToFirebase(String uploadedImageUrl) { //(Pervaiz, 2021)
        progressDialog.setMessage("Adding Collection...");

        //get timestamp
        Long timestamp = System.currentTimeMillis();

        //Setup info to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("collection_id",""+timestamp);
        hashMap.put("collection_name",""+collection_name);
        hashMap.put("collection_desc",""+collection_desc);
        hashMap.put("goal",goal);
        hashMap.put("url",""+uploadedImageUrl);
        hashMap.put("timestamp",timestamp);
        hashMap.put("user_id",""+firebaseAuth.getUid());

        //Add to firebase database...Database Root > Collections >collectionId > collection info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("collections");
        ref.child(""+firebaseAuth.getUid()).child(""+timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Toast.makeText(AddCollection.this,"Collection successfully added",Toast.LENGTH_SHORT).show();
            resetActivity();
        }).addOnFailureListener(e -> {
         //Failing to add collection
            progressDialog.dismiss();
            Toast.makeText(AddCollection.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        });

    }

    //Custom toast notification
    private void customToast(String custom_message){
        // Get the custom_toast.xml layout
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout_id));

        // set the message
        TextView text = (TextView) layout.findViewById(R.id.tv_toastText);
        text.setText(custom_message);

        // Toast...
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP, 0, 220);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    //Method to clear the edit texts once the collection is added
    private void resetActivity(){

        EditText etCollectionName = findViewById(R.id.et_collectionName);
        EditText etCollectionDesc = findViewById(R.id.et_collectionDesc);
        EditText etGoal = findViewById(R.id.et_Goal);

        ImageButton ibCollectionImage = findViewById(R.id.ib_AddCollectionImage);
        ImageButton ibRemove = findViewById(R.id.addRemoveImage);

        etCollectionName.setText("");
        etCollectionDesc.setText("");
        etGoal.setText("");
        ibCollectionImage.setImageResource(R.drawable.ic_add_photo);
        ibRemove.setVisibility(View.GONE);
    }

}

/* Reference list

Pervaiz, A. (2021). Book App Firebase | 03 Add Book Category | Android Studio | Java. [online] www.youtube.com. Available at: https://www.youtube.com/watch?v=TkBos_Flc4k [Accessed 14 Jul. 2021].


bumptech (2019). bumptech/glide. [online] GitHub. Available at: https://github.com/bumptech/glide.



 */