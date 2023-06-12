package com.example.glucometric1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Splash extends AppCompatActivity {

    private static int SPLASH_SCREEN = 3000;
    //Variables
    Animation topAmin , bottomAmin;
    ImageView logo;
    TextView logoName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);

        //Animations
        topAmin = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAmin = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        logo = (ImageView) findViewById(R.id.imageSplash);
        logoName = (TextView) findViewById(R.id.textView);

        logo.setAnimation(topAmin);
        logoName.setAnimation(bottomAmin);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splash.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);
    }

}