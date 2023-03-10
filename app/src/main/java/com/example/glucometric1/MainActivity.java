package com.example.glucometric1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public void setActivity(View view) {
        TextView textView = (TextView) view;
        String text = textView.getText().toString();
        Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "User choose " + text + " activity");

        switch (view.getId()) {
            case R.id.textViewTakeSample:
                Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "Change to Take sample activity");
                break;
            case R.id.textViewMeasurement:
                Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "Change to Measurement activity");
                break;
            case R.id.textViewDataset:
                Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "Change to Dataset activity");
                break;
            case R.id.textViewMLModels:
                Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "Change to ML models activity");
                break;
            case R.id.textViewInfo:
                Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "Change to Info activity");
                break;
            case R.id.textViewSetting:
                Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "Change to Setting activity");
                break;
            default:
                Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "No activity");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}