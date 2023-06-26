package com.example.glucometric1.bluetoothle;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BLEScannerService extends Service {
    public final static String ACTION_SCAN_DONE = "com.example.glucometric1.bluetoothle.ACTION_SCAN_DONE";
    public final static String ACTION_SCAN_NEW_DEVICE = "com.example.glucometric1.bluetoothle.ACTION_SCAN_NEW_DEVICE";
    private static final String TAG = "BLEScannerService";
    // Assigned variables
    private static final boolean SCAN_STATUS_SCANNING = true;
    private static final boolean SCAN_STATUS_STOPPED = false;
    private static final long SCAN_PERIOD = 10000;

    private final Handler handler = new Handler();
    private final List<ScanFilter> scanFilters;
    private final ArrayList<BluetoothDevice> listBleDevice;
    private final Binder binder = new LocalBinder();
    // Private functions
    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            if (device.getName() != null && !listBleDevice.contains(device)) {
                Log.d(TAG, String.format("device: %s", device.getName()));
                Log.d(TAG, String.format("mac: %s", device.getAddress()));
                Log.d(TAG, "--------");
                listBleDevice.add(device);
                broadcastUpdate(ACTION_SCAN_NEW_DEVICE);
            }
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults");
            Log.d(TAG, String.format("number of device: %d", results.size()));
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed");
        }
    };
    // Unsigned variables
    private boolean isScanning;
    private BluetoothLeScannerCompat bleScanner;
    private ScanSettings scanSettings;

    // Public functions
    public BLEScannerService() {
        isScanning = false;
        scanFilters = new ArrayList<>();
        listBleDevice = new ArrayList<>();
    }

    // intentFilter
    public static IntentFilter intentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SCAN_DONE);
        intentFilter.addAction(ACTION_SCAN_NEW_DEVICE);
        return intentFilter;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean initialize() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter");

            return false;
        }

        bleScanner = BluetoothLeScannerCompat.getScanner();
        scanSettings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(10000)
                .setUseHardwareBatchingIfSupported(true)
                .build();

        if (bleScanner == null) {
            Log.e(TAG, "Unable to obtain a BLEScanner");
            return false;
        }
        return true;
    }

    public void startScan() {
        Log.d(TAG, "startScan");
        if (bleScanner == null) {
            Log.i(TAG, "startScan: bleScanner is null");
            return;
        }

        if (isScanning == SCAN_STATUS_STOPPED) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, SCAN_PERIOD);
            listBleDevice.removeAll(listBleDevice);
//            bleScanner.startScan(scanFilters, scanSettings, scanCallback);
            bleScanner.startScan(scanCallback);
            isScanning = SCAN_STATUS_SCANNING;
        }
    }

    public void stopScan() {
        Log.d(TAG, "stopScan");
        if (bleScanner == null) {
            Log.i(TAG, "stopScan: bleScanner is null");
            return;
        }

        if (isScanning == SCAN_STATUS_SCANNING) {
            Log.d(TAG, "ble stop scanning");
            bleScanner.stopScan(scanCallback);
            isScanning = SCAN_STATUS_STOPPED;
            broadcastUpdate(ACTION_SCAN_DONE);
        }
    }

    public ArrayList<BluetoothDevice> getBleDevices() {
        return listBleDevice;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // Inner classes
    public class LocalBinder extends Binder {
        public BLEScannerService getService() {
            Log.d(TAG, "get bleScannerService binder");
            return BLEScannerService.this;
        }
    }
}
