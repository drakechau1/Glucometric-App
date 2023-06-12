package com.example.glucometric1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MLModelsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlmodels);

        Log.i(Thread.currentThread().getStackTrace()[2].getClassName().toString(), "Opened");

    }
}