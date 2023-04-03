package com.example.glucometric1.bluetoothle;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.glucometric1.R;

import java.util.ArrayList;


public class BLEDevicesAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<BluetoothDevice> mDevices;
    LayoutInflater mInflater;

    public BLEDevicesAdapter(Context context, ArrayList<BluetoothDevice> devices) {
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
        BluetoothDevice device = mDevices.get(i);
        if (device.getName() != null) {
            deviceName.setText(device.getName());
            MACAddress.setText(device.getAddress());
        }
        return view;
    }
}

