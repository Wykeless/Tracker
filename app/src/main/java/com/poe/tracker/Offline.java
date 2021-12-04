package com.poe.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Offline extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        /*
            Offline in terms of my app means continuing
            without an account and storing data the device
            and once the user logs into or create an account
            the data will be uploaded into the firebase database
            as well
        */


        //planned on saving data on the device using sql lite
    }
}