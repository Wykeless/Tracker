package com.poe.tracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class Login extends AppCompatActivity {

    //Declaration
    Button btn_login;
    TextView tv_create;
    EditText et_email,et_password;
    //progress dialog
    private ProgressDialog progressDialog;
    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //connecting xml elements code variables
        btn_login = findViewById(R.id.btn_login);
        tv_create = findViewById(R.id.tv_log_create_acc);

        //click event for login button
        btn_login.setOnClickListener(v -> validateDate());

        tv_create.setOnClickListener(v -> startActivity(new Intent(Login.this,Register.class)));

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Login.this,MainActivity.class));
    }

    private String email ="",password="";
    private void validateDate() {
        //before logging in, lets do some data validation
        et_email = findViewById(R.id.et_Email);
        et_password = findViewById(R.id.et_Password);

        email = et_email.getText().toString().trim();
        password = et_password.getText().toString().trim();

        //validate
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            customToast("Invalid email...",500);
        }
        else if (TextUtils.isEmpty(password)){
            customToast("Enter your password...",850);
        }
        else{
            logUserIn();
        }

    }

    private void logUserIn() {
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email,password)
            //When user successfully logs in
            .addOnSuccessListener(authResult -> checkUser())

            //When user is unable to log in
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(Login.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking User...");
        //Check if user is user or admin from realtime database
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //Check for user in database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        assert firebaseUser != null;
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                //Get user type
                String userType = ""+snapshot.child("userType").getValue();
                //check user type
                if (userType.equals("user")){
                    //this is a user, open home
                    startActivity(new Intent(Login.this, Home.class));
                    finish();
                }
                else if(userType.equals("admin")){
                    //this is for admin page, which i dont
                    startActivity(new Intent(Login.this, Home.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });

    }


    //Custom toast notification
    private void customToast(String custom_message, int yOffset){
        // Get the custom_toast.xml layout
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout_id));

        // set the message
        TextView text = (TextView) layout.findViewById(R.id.tv_toastText);
        text.setText(custom_message);

        // Toast...
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP, 0, yOffset);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}