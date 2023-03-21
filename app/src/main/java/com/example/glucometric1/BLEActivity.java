package com.example.glucometric1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.Toolbar;

import com.example.glucometric1.BLE.bleLoggingFragment;
import com.example.glucometric1.BLE.bleScannerFragment;
import com.example.glucometric1.takesample.AddSampleFragment;
import com.example.glucometric1.takesample.LoggingFragment;
import com.example.glucometric1.takesample.RecordFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class BLEActivity extends AppCompatActivity {
    private static final String TAG = BLEActivity.class.getSimpleName();

    TabLayout tabLayout;
    ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Log.i(TAG, "onCreate");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.ble_activity_toolBar);
        setActionBar(myToolbar);

        // assign variable
        tabLayout=findViewById(R.id.ble_activity_tabLayout);
        viewPager2=findViewById(R.id.ble_activity_viewPager2);

        tabLayout.addTab(tabLayout.newTab().setText("Scanner"));
        tabLayout.addTab(tabLayout.newTab().setText("Logging"));

        Fragment2Adapter fragment2Adapter = new Fragment2Adapter(this);
        viewPager2.setAdapter(fragment2Adapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position)
                {
                    case 0:
                        tabLayout.getTabAt(0).select();
                        break;
                    case 1:
                        tabLayout.getTabAt(1).select();
                        break;
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getId()) {
                    case 0:
                        viewPager2.setCurrentItem(0);
                        break;
                    case 1:
                        viewPager2.setCurrentItem(1);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public class Fragment2Adapter extends FragmentStateAdapter {
        public Fragment2Adapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position)
            {
                case 0: return new bleScannerFragment();
                case 1: return new bleLoggingFragment();
            }
            return new bleLoggingFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}