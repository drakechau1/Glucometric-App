package com.example.glucometric1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class BLEActivity extends AppCompatActivity {
    private static final String TAG = BLEActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Log.i(TAG, "onCreate");

    }
}