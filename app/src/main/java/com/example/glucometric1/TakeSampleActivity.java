package com.example.glucometric1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.glucometric1.takesample.AddSampleFragment;
import com.example.glucometric1.takesample.MyFragment2Adapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TakeSampleActivity extends AppCompatActivity {
    private final static String TAG = "TakeSampleActivity";
    private ViewPager2 mViewPager2;
    private BottomNavigationView mBottomNavigationView;

    private String ble_device_name;
    private String ble_device_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_sample);

        Intent intent = getIntent();
        ble_device_name = intent.getStringExtra("ble_device_name");
        ble_device_address = intent.getStringExtra("ble_device_address");

        Log.i(Thread.currentThread().getStackTrace()[2].getClassName().toString(), "Opened");

        mViewPager2 = (ViewPager2) findViewById(R.id.viewPager2);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);

        Bundle bundle = new Bundle();
        String myMessage = "Stack Overflow is cool!";
        bundle.putString("ble_device_name", ble_device_name);
        bundle.putString("ble_device_address", ble_device_address);

        MyFragment2Adapter mAdapter = new MyFragment2Adapter(this, bundle);
        mViewPager2.setAdapter(mAdapter);

        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_take_sample).setChecked(true);
                        break;
                    case 1:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_dataset).setChecked(true);
                        break;
                    case 2:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_logging).setChecked(true);
                        break;
                    default:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_take_sample).setChecked(true);
                }
            }
        });

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_take_sample:

                        mViewPager2.setCurrentItem(0);
                        break;
                    case R.id.nav_dataset:
                        mViewPager2.setCurrentItem(1);
                        break;
                    case R.id.nav_logging:
                        mViewPager2.setCurrentItem(2);
                        break;
                    default:
                        mViewPager2.setCurrentItem(0);
                        break;
                }
                return true;
            }
        });
    }
}