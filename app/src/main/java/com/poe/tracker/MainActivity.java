package com.poe.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_login = findViewById(R.id.btn_log_in);
        Button btn_register = findViewById(R.id.btn_Register);
        TextView tv_Offline = findViewById(R.id.tv_Offline);

        //If user clicks this, it takes them to the login screen
        btn_login.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this,Login.class));
            finish();
        });

        //If user clicks this, it takes them to the register screen
        btn_register.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this,Register.class));
            finish();
        });

        //If user clicks this, it was supposed to continue without an account
        tv_Offline.setOnClickListener(v -> Toast.makeText(getApplicationContext(),
        "This Doesn't Do Anything for now",
        Toast.LENGTH_SHORT).show());

    }
}

/* Reference list

Ahamed, F. (2017). How to pass variables to another activity in android? [online] Stack Overflow. Available at: https://stackoverflow.com/a/43704142.

Arthur (2021). ArthurHub/Android-Image-Cropper. [online] GitHub. Available at: https://github.com/ArthurHub/Android-Image-Cropper/ [Accessed 12 Jul. 2021].

Axarydax (2011). how to get an uri of an image resource in android. [online] Stack Overflow. Available at: https://stackoverflow.com/a/4896272 [Accessed 12 Jul. 2021].

bumptech (2019). bumptech/glide. [online] GitHub. Available at: https://github.com/bumptech/glide.

Flaticon (2013). Flaticon, the largest database of free vector icons. [online] Flaticon. Available at: https://www.flaticon.com/ [Accessed 17 Jul. 2021].

Francis, S. (2019). CUSTOM TOAST VIEWS IN ANDROID. [online] Medium. Available at: https://medium.com/@segunfrancis/custom-toast-views-in-android-1e1e7352351c [Accessed 11 Jul. 2021].

HB (2002). android - Byte array of image into imageview. [online] Stack Overflow. Available at: https://stackoverflow.com/a/13854787 [Accessed 12 Jul. 2021].

Javi (2011). java - Quickest way to get content type. [online] Stack Overflow. Available at: https://stackoverflow.com/a/5802223 [Accessed 18 Jul. 2021].

Mistri, M. (2012). database - Android: How to pass data of one activity with the button click in another activity. [online] Stack Overflow. Available at: https://stackoverflow.com/a/6592507 [Accessed 13 Jul. 2021].

Pervaiz, A. (2021). Book App Firebase | 03 Add Book Category | Android Studio | Java. [online] www.youtube.com. Available at: https://www.youtube.com/watch?v=TkBos_Flc4k [Accessed 14 Jul. 2021].

Řádek, L. (2020). android - Error StrictMode$AndroidBlockGuardPolicy.onNetwork. [online] Stack Overflow. Available at: https://stackoverflow.com/a/22395546 [Accessed 18 Jul. 2021].

RorschachDev (2013). Android windowSoftInputMode=“adjustPan” scroll some more. [online] Stack Overflow. Available at: https://stackoverflow.com/a/18923547 [Accessed 10 Jul. 2021].

Solanki, K. (2021). android - Datepicker: How to popup datepicker when click on edittext. [online] Stack Overflow. Available at: https://stackoverflow.com/a/14933515 [Accessed 12 Jul. 2021].

Ziem (2015). How to use SharedPreferences in Android to store, fetch and edit values. [online] Stack Overflow. Available at: https://stackoverflow.com/a/11027631 [Accessed 14 Jul. 2021].
‌

 */