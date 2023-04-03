/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.glucometric1.bluetoothle;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */

public class BLEGATTService extends Service {
    public static final String TAG = "BLEGATTService";

    public static final String TEST_DEVICE_ADDRESS = "B8:8E:82:35:91:D5";   // Huawei band 6
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static int STATE_CONNECTED = 1;
    public final static int STATE_DISCONNECTED = 0;


    private final Binder binder = new LocalBinder();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bleDevice;
    private BluetoothGatt bluetoothGatt;
    private Handler handler;
    private int connectionState;

    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
//            if (!checkBLEPermission()) return;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    Log.i(TAG, "BLE device connected to " + gatt.getDevice());

                    int bondState = getBondState();
                    if (bondState == BluetoothDevice.BOND_NONE || bondState == BluetoothDevice.BOND_BONDED) {
                        Log.i(TAG, "discovering...");
                        int delayWhenBonded = 0;
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                            delayWhenBonded = 1600;
                        }
                        final int delay = (bondState == BluetoothDevice.BOND_NONE ? delayWhenBonded : 0);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!checkBLEPermission()) return;
                                boolean result = gatt.discoverServices();
                                if (!result) {
                                    Log.w(TAG, "discoverServices failed to start");
                                    return;
                                }
                            }
                        }, delay);

                    } else if (bondState == BluetoothDevice.BOND_BONDING) {
                        Log.w(TAG, "waiting for bonding to complete");
                        return;
                    }

                    connectionState = STATE_CONNECTED;
                    broadcastUpdate(ACTION_GATT_CONNECTED);

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectionState = STATE_DISCONNECTED;
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    close();
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    // Ignore now
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    // Ignore now
                }
            } else {
                Log.e(TAG, "Gatt status error: " + status);
                close();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (!checkBLEPermission()) return;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private boolean checkBLEPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;
    }

    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter");
            return false;
        }
        handler = new Handler();
        return true;
    }

    public boolean connect(String address) {
        if (!checkBLEPermission()) return false;

        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address");
            return false;
        }

        try {
            bleDevice = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = bleDevice.connectGatt(this, false, bluetoothGattCallback, TRANSPORT_LE);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Device not found with provided address");
            return false;
        }

        return true;
    }

    public void close() {
        if (!checkBLEPermission()) return;
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        Log.w(TAG, "Closed GATT, BLE device disconnected");
    }

    public int getBondState() {
        return checkBLEPermission() ? bleDevice.getBondState() : -1;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private boolean clearServicesCache() {
        boolean result = false;
        try {
            Method refreshMethod = bluetoothGatt.getClass().getMethod("refresh");
            if (refreshMethod != null) {
                result = (boolean) refreshMethod.invoke(bluetoothGatt);
            }
        } catch (Exception e) {
            Log.e(TAG, "ERROR: Could not invoke refresh method");
        }
        return result;
    }

    public class LocalBinder extends Binder {
        BLEGATTService getService() {
            return BLEGATTService.this;
        }
    }

}

