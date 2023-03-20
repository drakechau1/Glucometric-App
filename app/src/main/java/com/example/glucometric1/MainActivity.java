package com.example.glucometric1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public void openActivity(AppCompatActivity activity) {
        Intent intent = new Intent(MainActivity.this, activity.getClass());
        startActivity(intent);
    }

    public void changeActivity(View view) {
        String text = ((TextView) view).getText().toString();
        Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(), "Open " + text + " activity");

        switch (view.getId()) {
            case R.id.textViewTakeSample:
                openActivity(new TakeSampleActivity());
                break;
            case R.id.textViewMeasurement:
                openActivity(new MeasurementActivity());
                break;
            case R.id.textViewDataset:
                openActivity(new DatasetActivity());
                break;
            case R.id.textViewMLModels:
                openActivity(new MLModelsActivity());
                break;
            case R.id.textViewBLE:
                openActivity(new BLEActivity());
                break;
            case R.id.textViewSetting:
                openActivity(new SettingActivity());
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Thread.currentThread().getStackTrace()[2].getClassName(), "Resumed");
    }
}