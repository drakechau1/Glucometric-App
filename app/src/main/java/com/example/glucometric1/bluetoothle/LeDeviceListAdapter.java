package com.example.glucometric1.bluetoothle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.glucometric1.R;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {
    private final ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflation;

    public LeDeviceListAdapter(Activity activity) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        if (activity != null)
            mInflation = activity.getLayoutInflater();
        else
            Log.i("LeDeviceListAdapter", "Activity is null");
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public ArrayList<BluetoothDevice> getLeDevices() {
        return mLeDevices;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflation.inflate(R.layout.listitem_ble_devices, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        @SuppressLint("MissingPermission") final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
