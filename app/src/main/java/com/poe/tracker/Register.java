package com.poe.tracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class Register extends AppCompatActivity {

    //Declaration
    Button btn_register;
    EditText et_firstname,et_email,et_password,et_ConfirmPassword;
    TextView tv_Login;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tv_Login = findViewById(R.id.tv_Log_in);
        btn_register = findViewById(R.id.btn_register);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        tv_Login.setOnClickListener(v -> startActivity(new Intent(Register.this,Login.class)));

        btn_register.setOnClickListener(v -> validateData());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Register.this,MainActivity.class));
    }

    private String name ="",email="",password="";
    private void validateData() {
        //before creating account, lets do some data validation

        et_firstname = findViewById(R.id.et_Register_First_name);
        et_email = findViewById(R.id.et_Register_Email);
        et_password = findViewById(R.id.et_Register_Password);
        et_ConfirmPassword= findViewById(R.id.et_Confirm_Password);

        //get data
        name = et_firstname.getText().toString().trim();
        email = et_email.getText().toString().trim();
        password = et_password.getText().toString().trim();
        String confirmPass = et_ConfirmPassword.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(name)){
            Toast.makeText(getApplicationContext(),"Enter your name...", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(getApplicationContext(),"Invalid email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Enter your password", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirmPass)){
            Toast.makeText(getApplicationContext(),"Enter confirm password", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmPass)){
            Toast.makeText(getApplicationContext(),"Passwords do not match", Toast.LENGTH_SHORT).show();
        }
        else {
            createUserAccount();
        }
    }

    private void createUserAccount() {

        //show progress

        progressDialog.setMessage("Creating account..");
        progressDialog.show();

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(authResult -> {
            //account creation success(), now add in firebase realtime database
            updateUserInfo();
        }).addOnFailureListener(e -> {
            //account creation failed
            progressDialog.dismiss();
            Toast.makeText(Register.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info...");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current user uid, since user is registered so we can get now
        String uid = firebaseAuth.getUid();

        //setup data to add in db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileImage",""); //add empty, will do later
        hashMap.put("userType","user");
        hashMap.put("timestamp",timestamp);

        //set date to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        assert uid != null;
        ref.child(uid).setValue(hashMap).addOnSuccessListener(unused -> {

            // Adds achievements to users account
            addAchievements();

            // Makes sure user is created than makes sure achievements has enough
            // time to upload.
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();
                //since user account is created start user on dashboard.
                startActivity(new Intent(Register.this, Home.class));
                finish();
            }, 2000); // 2 seconds

        }).addOnFailureListener(e -> {
            //data failed adding to the database
            progressDialog.dismiss();
            Toast.makeText(Register.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Adds achievements to the users account
    private void addAchievements() {

        //Creates new array list and assigns values to it
        ArrayList<String> achievements = new ArrayList<>();

//        achievements.add("Add a collection for the first time");
//        achievements.add("Add a item for the first time");
//        achievements.add("Share Collection 5 Times");
//        achievements.add("Share Item 5 times");
//        achievements.add("Add 5 collections");
        achievements.add("Add 5 items");

        //timestamp

        //get current user uid, since user is registered so we can get now
        String uid = firebaseAuth.getUid();

        for(String achievement : achievements) {

            //setup data to add in db
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("achievement_id",""+"achi01");
            hashMap.put("name",achievement);
            hashMap.put("completed","false");
            //hashMap.put("timestamp",timestamp);

            //set date to database
            assert uid != null;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("achievements").child(uid);
            ref.child("achi01").setValue(hashMap).addOnSuccessListener(unused -> {
                //success
            }).addOnFailureListener(e -> {
                //oops
            });
        }

    }
}