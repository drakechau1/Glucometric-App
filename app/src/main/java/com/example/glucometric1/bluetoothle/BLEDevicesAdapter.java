package com.example.glucometric1.bluetoothle;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.glucometric1.R;

import java.util.ArrayList;
import java.util.List;


public class BLEDevicesAdapter extends BaseAdapter {
    Context mContext;
    List<BluetoothDevice> mDevices;
    LayoutInflater mInflater;

    ImageView imageView_device;
    Animation rightAmin;


    public BLEDevicesAdapter(Context context, List<BluetoothDevice> devices) {
        mContext = context;
        mDevices = devices;
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = mInflater.inflate(R.layout.ble_device_layout, null);
        TextView deviceName = view.findViewById(R.id.ble_device_name);
        TextView MACAddress = view.findViewById(R.id.ble_device_address);
        imageView_device = view.findViewById(R.id.imageView_device);
        LinearLayout itemView_layout_ble = view.findViewById(R.id.itemlistView_ble);
        rightAmin = AnimationUtils.loadAnimation(mContext, R.anim.left_animation);
        BluetoothDevice device = mDevices.get(i);
        if (device.getName() != null) {
            itemView_layout_ble.setAnimation(rightAmin);
            deviceName.setText(device.getName());
            MACAddress.setText(device.getAddress());
            String name = deviceName.getText().toString();
            String glucose_device = "UIT-GLUCOSE-202334684"; //Example ID of device
            if (glucose_device.length() == name.length())
            {
                Drawable myDrawable = ContextCompat.getDrawable(mContext, R.drawable.healthcare_cover);
                imageView_device.setImageDrawable(myDrawable);
            }
            else
            {
                Drawable myDrawable2 = ContextCompat.getDrawable(mContext, R.drawable.icon_bluetooth2png);
                imageView_device.setImageDrawable(myDrawable2);
            }
        }
        return view;
    }
}

