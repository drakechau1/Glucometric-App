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

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */

public class BLEGATTService extends Service {
    public static final String TAG = "BLEGATTService";


    public static final String UIT_GLUCOSE_CMD = "3c02556c-4700-4957-812e-b7d297a55600";   // write command to device
    public static final String UIT_GLUCOSE_DATA = "3309a511-784f-2d6c-da11-7722d4c08945";   // real device characteristic

    //public static final String TEST_DEVICE_ADDRESS = "B8:8E:82:35:91:D5";   // Huawei band 6
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_CHARACTERISTIC_CHANGE = "com.example.bluetooth.le.ACTION_CHARACTERISTIC_CHANGE";
    public final static String ACTION_DEVICE_ERROR = "com.example.bluetooth.le.ACTION_DEVICE_ERROR";
    public final static int STATE_CONNECTED = 1;
    public final static int STATE_DISCONNECTED = 0;
    public final static int STATE_DEVICE_ERROR = 2;
    public final static int REQUEST_MTU = 515;


    //
    //
    // Assigned variables
    private final Binder binder = new LocalBinder();

    //
    //
    // Unassigned variables
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bleDevice;
    private BluetoothGatt bluetoothGatt;
    private Handler handler;
    public int connectionState = 0;

    public static IntentFilter intentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_CHARACTERISTIC_CHANGE);
        intentFilter.addAction(ACTION_DEVICE_ERROR);
        return intentFilter;
    }


    //
    //
    // Inner class
    public class LocalBinder extends Binder {
        public BLEGATTService getService() {
            return BLEGATTService.this;
        }
    }

    //
    //
    // Public methods
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

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }

    public int getBondState() {
        return checkBLEPermission() ? bleDevice.getBondState() : -1;
    }

    public void close() {
        if (!checkBLEPermission()) return;
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.disconnect();
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
            Log.d(TAG, "Try to connect to " + address);
            bleDevice = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = bleDevice.connectGatt(this, false, bluetoothGattCallback, TRANSPORT_LE);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Device not found with provided address");
            return false;
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    public void requestMtu(int mtu) {
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");

            return;
        }
        Log.d(TAG, "MTU requesting...");
        if (bluetoothGatt.requestMtu(mtu)) {
            Log.d(TAG, "requestMTU successfully");
        } else {
            Log.d(TAG, "requestMTU failed");
        }
    }

    @SuppressLint("MissingPermission")
    public int readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return 0;
        }
        bluetoothGatt.readCharacteristic(characteristic);
        return 1;
    }

    @SuppressLint("MissingPermission")
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return 0;
        }
        bluetoothGatt.writeCharacteristic(characteristic);
        return 1;
    }


    //
    //
    // Private methods
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
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
                        handler.postDelayed(() -> {
                            if (!checkBLEPermission()) return;
                            boolean result = gatt.discoverServices();
                            if (!result) {
                                Log.w(TAG, "discoverServices failed to start");
                                return;
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
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                    Log.w(TAG, "Closed GATT, BLE device disconnected");
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                }
            }
            else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION)
            {
                connectionState = STATE_DEVICE_ERROR;
                bluetoothGatt.close();
                bluetoothGatt = null;
                Log.e(TAG, "Closed GATT, BLE device has been disconnected");
                broadcastUpdate(ACTION_DEVICE_ERROR);
            }
            else
            {
                Log.e(TAG, "Gatt status error: " + status);
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

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "characteristic changed");
            broadcastUpdate(ACTION_CHARACTERISTIC_CHANGE, characteristic);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, String.format("MTU: %d", mtu));
            } else {
                Log.d(TAG, String.format("MTU: null"));
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UIT_GLUCOSE_DATA.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra("char_uuid", characteristic.getUuid().toString());
        intent.putExtra("data_value", new String(characteristic.getValue()));
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

    private boolean checkBLEPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;
    }

}

