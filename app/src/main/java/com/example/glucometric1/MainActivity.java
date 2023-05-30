package com.example.glucometric1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    Animation topAmin , bottomAmin, leftAmin;
    ImageView logo;
    TextView logoName, categories_label;

    LinearLayout    linearLayout;
    public void openActivity(AppCompatActivity activity) {
        Intent intent = new Intent(MainActivity.this, activity.getClass());
        startActivity(intent);
    }

    public void changeActivity(View view) {
//        String text = ((TextView) view).getText().toString();
//        Log.i(TAG, "Open " + text + " activity");

        switch (view.getId()) {
            case R.id.imageView_takeSample:
                openActivity(new TakeSampleActivity());
                break;
            case R.id.imageView_measurement:
                openActivity(new MeasurementActivity());
                break;
            case R.id.imageView_dataset:
                openActivity(new DatasetActivity());
                break;
            case R.id.imageView_mlModels:
                openActivity(new MLModelsActivity());
                break;
            case R.id.imageView_ble:
                openActivity(new BLEActivity());
                break;
            case R.id.imageView_setting:
                openActivity(new SettingActivity());
                break;
            default:
                Log.i(TAG, "No activity");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Animations
        topAmin = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAmin = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        leftAmin    = AnimationUtils.loadAnimation(this,R.anim.left_animation);
        //Define
        logo = (ImageView) findViewById(R.id.imageView1);
        logoName = (TextView) findViewById(R.id.textView);
        categories_label = (TextView) findViewById(R.id.categories_label);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout4);
        //Set animation
        logo.setAnimation(topAmin);
        logoName.setAnimation(bottomAmin);
        categories_label.setAnimation(leftAmin);
        linearLayout.setAnimation(leftAmin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Resumed");
    }
}