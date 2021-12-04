package com.poe.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile_Settings extends AppCompatActivity {

    //Declaration
    //Firebase Auth
    private FirebaseAuth firebaseAuth;
    TextView tv_Username;
    Button btn_log_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkLoggedInUser();

        btn_log_out = findViewById(R.id.btn_Log_Out);

        btn_log_out.setOnClickListener(v -> {
            //confirms if user wants to log out
            showCustomDialog();
        });
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(Profile_Settings.this, Home.class));
        finish();
    }


    private void checkLoggedInUser() {
        //Gets current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //When user is not logged in
            startActivity(new Intent(Profile_Settings.this,MainActivity.class));
            finish();
        }
        else{
            //User is logged in
            String email = firebaseUser.getEmail();
            //set in textview of toolbar
            tv_Username = findViewById(R.id.tv_Username);
            tv_Username.setText(email);
        }
    }


    //Function to display the custom dialog.
    @SuppressLint("SetTextI18n")
    private void showCustomDialog() {
        Dialog dialog = new Dialog(Profile_Settings.this);
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


        title.setText("Log out of account?");
        logout.setText("logout");

        logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkLoggedInUser();
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
