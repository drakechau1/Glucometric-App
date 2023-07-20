package com.example.glucometric1.bluetoothle;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BleScanner {
    public static final String TAG = "BLEScanner";
    public static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private List<BluetoothDevice> scannedDevices = null;

    public BleScanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @SuppressLint("MissingPermission")
    public void startScanning(ScanCallback callback) {
        this.scanCallback = callback;
        scannedDevices = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setLegacy(false)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();

            Log.i(TAG, "startScanning");
            if (bluetoothLeScanner != null)
                bluetoothLeScanner.startScan(null, settings, scanCallback);
        }
    }

    @SuppressLint("MissingPermission")
    public void stopScanning() {
        Log.d(TAG, "stopScanning");
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(scanCallback);
    }

    public List<BluetoothDevice> getScannedDevices() {
        return scannedDevices;
    }

    public BluetoothDevice getDevice(int index) {
        return scannedDevices.get(index);
    }

    public void addScannedDevice(BluetoothDevice device) {
        if (!scannedDevices.contains(device))
            scannedDevices.add(device);
    }

    public void removeScannedDevice(BluetoothDevice device) {
        scannedDevices.remove(device);
    }

    public void clearScannedDevices() {
        scannedDevices.clear();
    }
}
