package com.iiatimd.portfolioappv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1500);
//                    SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
//                    boolean isLoggedIn = userPref.getBoolean("isLoggedIn", false);
//
//                    if (isLoggedIn) {
//                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
//                        finish();
//                    } else {
//                        isFirstTime();
//                    }
                    isFirstTime();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void isFirstTime() {
        SharedPreferences preferences = getApplication().getSharedPreferences("onBoard", Context.MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime",true);

        if (isFirstTime) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            // start OnBoard activity
            startActivity(new Intent(MainActivity.this,OnBoardActivity.class));
        } else {
            //start Login Activity
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        }
        finish();
    }
}