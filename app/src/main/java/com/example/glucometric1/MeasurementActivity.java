package com.example.glucometric1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbnb.lottie.LottieDrawable;
import com.amrdeveloper.lottiedialog.LottieDialog;
import com.example.glucometric1.custom_textview.PoppinsMediumTextView;

import java.util.Calendar;

public class MeasurementActivity extends AppCompatActivity {

    TextView textViewConnectionStatus;
    public String ble_device_name;
    public String ble_device_address;
    private PoppinsMediumTextView Greeting;
    private Spinner spinnerSex;
    public LottieDialog lottieDialog_measurement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);
        Log.i(Thread.currentThread().getStackTrace()[2].getClassName().toString(), "Opened");

        Intent intent = getIntent();
        ble_device_name = intent.getStringExtra("ble_device_name");
        ble_device_address = intent.getStringExtra("ble_device_address");

        textViewConnectionStatus = (TextView)findViewById(R.id.textViewConnectionStatus);
        Greeting = (PoppinsMediumTextView)findViewById(R.id.Greeting_textview);
        spinnerSex = (Spinner)findViewById(R.id.spinnerSex);
        lottieDialog_measurement = new LottieDialog(this)
                .setAnimation(R.raw.medical_shield)
                .setAnimationViewHeight(2000)
                .setAnimationViewHeight(2000)
                .setAnimationRepeatCount(LottieDrawable.INFINITE)
                .setAutoPlayAnimation(true)
                .setMessage("Measure your blood sugar...")
                .setMessageTextSize(18)
                .setDialogBackground(Color.WHITE)
                .setDialogHeight(1000)
                .setDialogWidth(1000)
                .setCancelable(false);

        Calendar calendar = Calendar.getInstance();
        int jam = calendar.get(Calendar.HOUR_OF_DAY);
        if(jam >= 0 && jam <16){
            Greeting.setText("Good Morning");
        }else if(jam >=12 && jam <16){
            Greeting.setText("Good Afternoon");
        }else if(jam >=16 && jam <21){
            Greeting.setText("Good Evening");
        }else if(jam >=21 && jam <24){
            Greeting.setText("Good Night");
        }else {
            Greeting.setText("UIT GLUCOMETRIC Hello");
        }

        // Config sex spinner
        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MeasurementActivity.this, R.array.sex, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinnerSex.setAdapter(adapter);
        }
    }
}