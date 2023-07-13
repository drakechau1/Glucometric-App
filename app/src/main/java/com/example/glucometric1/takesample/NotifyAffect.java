package com.example.glucometric1.takesample;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

public class NotifyAffect {
    private FragmentActivity activity;
    private Vibrator vibrator;

    public NotifyAffect(FragmentActivity activity) {
        if (activity == null) {
            Log.e("NotifyAffect", "Null activity");
            return;
        }
        this.activity = activity;
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void makeVibrate(long millis) {
        // this effect creates the vibration of default amplitude for *millis* second
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect1 = VibrationEffect.createOneShot(millis,
                    VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.cancel();
            vibrator.vibrate(vibrationEffect1);
        }
    }

    public void makeSuccess(String noty) {
        makeVibrate(100);
        Toast.makeText(this.activity,
                noty,
                Toast.LENGTH_SHORT).show();
    }

    public void makeFailed(String noty) {
        makeVibrate(200);
        Toast.makeText(this.activity,
                noty,
                Toast.LENGTH_SHORT).show();
    }
}
