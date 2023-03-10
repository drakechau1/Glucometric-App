package com.example.glucometric1.takesample;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyFragment2Adapter extends FragmentStateAdapter {
    public MyFragment2Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0: return new AddSampleFragment();
            case 1: return new RecordFragment();
            case 2: return new LoggingFragment();
        }
        return new AddSampleFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
