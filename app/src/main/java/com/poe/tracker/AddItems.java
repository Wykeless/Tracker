package com.poe.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import android.view.Gravity;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

public class AddItems extends AppCompatActivity {

    //Declaration

    //Firebase auth
    private FirebaseAuth firebaseAuth;

    //Progress dialog
    private ProgressDialog progressDialog;

    private Uri globalItemUri;
    private String globalCollectionId;
    private int count = 0;
    private String globalImageUrl;

    private static final int REQUEST_IMAGE_CAPTURE_PERMISSION = 100;

    private String item_name = "";
    private String item_desc = "";
    private String item_date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_items);

        //Gets the data sent to the addItem activity from the collection_item activity
        Bundle extras = getIntent().getExtras();
        globalCollectionId = extras.getSerializable("collectionId").toString();

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        final Calendar myCalendar = Calendar.getInstance();

        EditText itemDate = findViewById(R.id.et_itemDate);
        Button btn_addItem = findViewById(R.id.btn_AddItem);
        ImageButton ib_addItemImage = findViewById(R.id.ib_AddItemImage);
        ImageButton ibAddRemoveItemImage = findViewById(R.id.addRemoveItemImage);
        ImageButton ibRemoveItemDate = findViewById(R.id.addRemoveItemDate);

        //(Solanki, 2021)
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(itemDate, myCalendar);
        };

        //button click events

        itemDate.setOnClickListener(v -> new DatePickerDialog(AddItems.this, R.style.DatePicker2,date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        ibRemoveItemDate.setOnClickListener(v -> {
            itemDate.setText("");
            ibRemoveItemDate.setVisibility(View.GONE);
        });

        ib_addItemImage.setOnClickListener(v -> {

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AddItems.this, R.style.CustomBottomSheetDialogTheme);
            View sheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.options_image_sheet, (LinearLayout) findViewById(R.id.imageOptionDialog));


            sheetView.findViewById(R.id.btn_add_a_url).setOnClickListener(v1 -> {
                showCustomDialog();
                bottomSheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btn_open).setOnClickListener(v12 -> {

                if (ActivityCompat.checkSelfPermission(AddItems.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                    // Request permission - this is asynchronous
                    ActivityCompat.requestPermissions(AddItems.this,
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

        ibAddRemoveItemImage.setOnClickListener(v -> {
            ib_addItemImage.setImageResource(R.drawable.ic_add_photo);
            count = 0;
            ibAddRemoveItemImage.setVisibility(View.GONE);
        });

        btn_addItem.setOnClickListener(v -> validateData());
    }



    private void validateData() {
        EditText etItemName = findViewById(R.id.et_ItemName);
        EditText etItemDesc = findViewById(R.id.et_ItemDesc);
        EditText etItemDate = findViewById(R.id.et_itemDate);

        String custom_message;

        //get data
        item_name = etItemName.getText().toString().trim();
        item_desc = etItemDesc.getText().toString().trim();
        item_date = etItemDate.getText().toString().trim();

        if (TextUtils.isEmpty(item_name)) {
            custom_message = "Please enter item name.";
            customToast(custom_message);
        }
        else {
            switch (count) {
                case 0:
                    addItemsToFirebase("");
                    break;
                case 1:
                    addItemsToFirebase(globalImageUrl);
                    break;
                case 2:
                    Uri imageUri = globalItemUri;
                    uploadImageToStorage(imageUri);
                    break;
            }
        }
    }

    //Function to display the custom dialog.
    private void showCustomDialog() {
        Dialog dialog = new Dialog(AddItems.this);
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
                    ImageButton ibRemoveImage = findViewById(R.id.addRemoveItemImage);

                    //If the content type is an image it sets the image view
                    if(contentType.startsWith("image/")){
                        ImageView ivItemImage = findViewById(R.id.ib_AddItemImage);
                        Glide   .with(AddItems.this)
                                .load(url)
                                .centerCrop()
                                .placeholder(R.drawable.image_holder)
                                .into(ivItemImage);

                        globalImageUrl = String.valueOf(url).trim();
                        count=0;
                        count=1;
                        ibRemoveImage.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                    else{
                        Toast.makeText(AddItems.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Toast.makeText(AddItems.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        cancelUrl.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // once permissions granted, gives user option to take picture or open gallery
    private void startCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    // results from above method is set to imageview showing the user the result after crop
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                ImageButton ibAddItemImage = findViewById(R.id.ib_AddItemImage);
                ImageButton ibAddRemoveItemImage = findViewById(R.id.addRemoveItemImage);

                Glide.with(this)
                        .load(resultUri)
                        .centerCrop()
                        .into(ibAddItemImage);
                globalItemUri = resultUri;
                ibAddRemoveItemImage.setVisibility(View.VISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to upload the images chosen to the firebase storage
    private void uploadImageToStorage(Uri imageUri) {

        //show progress
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();

        //time
        long timestamp;
        timestamp = System.currentTimeMillis();
        String userId = firebaseAuth.getUid();

        //path to image in firebase storage
        String filePathAndName = "Images/"+userId+"/"+"Items/"+timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    //get image url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    String uploadedImageUrl = ""+uriTask.getResult();

                    //upload to firebase db
                    addItemsToFirebase(uploadedImageUrl);
                })
                .addOnFailureListener(e -> {
                    //Log.d(TAG,"onFailure: Image upload failed due to"+e.getMessage());
                    Toast.makeText(AddItems.this,"Image upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method for adding data to firebase database
    private void addItemsToFirebase(String uploadedImageUrl) {
        progressDialog.setMessage("Adding Item...");

        //get timestamp
        Long timestamp = System.currentTimeMillis();

        //Setup info to add in firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("item_id", "" + timestamp);
        hashMap.put("item_name", "" + item_name);
        hashMap.put("item_desc", "" + item_desc);
        hashMap.put("item_acquisition_date",item_date);
        hashMap.put("url", "" + uploadedImageUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("user_id", "" + firebaseAuth.getUid());

        //Add to firebase database...Database Root > items > collectionId > item info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("items");
        ref.child(""+globalCollectionId).child("" + timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Toast.makeText(AddItems.this, "Item successfully added.", Toast.LENGTH_SHORT).show();
            resetActivity();
        }).addOnFailureListener(e -> {
            //Failing to add collection
            progressDialog.dismiss();
            Toast.makeText(AddItems.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // updates the date edit text
    private void updateLabel(EditText itemDate, Calendar myCalendar) {
        String myFormat = "dd MMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
        ImageButton ibRemoveItemDate = findViewById(R.id.addRemoveItemDate);
        ibRemoveItemDate.setVisibility(View.VISIBLE);
        itemDate.setText(sdf.format(myCalendar.getTime()));
    }

    //Custom toast notification //(Francis, 2019)
    private void customToast(String custom_message){
        // Get the custom_toast.xml layout
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast,
                      findViewById(R.id.custom_toast_layout_id));

        // set the message
        TextView text = layout.findViewById(R.id.tv_toastText);
        text.setText(custom_message);

        // Toast...
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP, 0, 320);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout); //deprecated, couldn't find an alternative, but still works for my app
        toast.show();
    }

    //Method to clear the edit texts once the collection is added
    private void resetActivity(){

        //resets fields after the data is added
        EditText etItemName = findViewById(R.id.et_ItemName);
        EditText etItemDesc = findViewById(R.id.et_ItemDesc);
        EditText etItemDate = findViewById(R.id.et_itemDate);

        ImageButton ibAddItemImage = findViewById(R.id.ib_AddItemImage);
        ImageButton ibRemoveItemDate = findViewById(R.id.addRemoveItemDate);
        ImageButton ibRemoveItemImage = findViewById(R.id.addRemoveItemImage);

        ibRemoveItemDate.setVisibility(View.GONE);
        ibRemoveItemImage.setVisibility(View.GONE);

        etItemName.setText("");
        etItemDesc.setText("");
        etItemDate.setText("");

        ibAddItemImage.setImageResource(R.drawable.ic_add_photo);
    }

}
/* Reference list

Francis, S. (2019). CUSTOM TOAST VIEWS IN ANDROID. [online] Medium. Available at: https://medium.com/@segunfrancis/custom-toast-views-in-android-1e1e7352351c [Accessed 11 Jul. 2021].

Solanki, K. (2021). android - Datepicker: How to popup datepicker when click on edittext. [online] Stack Overflow. Available at: https://stackoverflow.com/a/14933515 [Accessed 12 Jul. 2021].

 */