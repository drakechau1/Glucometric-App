package com.example.glucometric1;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.glucometric1.bluetoothle.BLEDevicesAdapter;
import com.example.glucometric1.bluetoothle.BLEScannerService;

public class BLEActivity extends AppCompatActivity {
    private final static String TAG = "BLEActivity";

    // Widget variables
    Button btnScan, btnStop, btnConnect;
    LinearLayout selected_item;
    ListView lvBleDevices;

    // Assigned variables


    // Unassigned variables
    BLEScannerService bleScannerService;


    // Binding connections
    private final ServiceConnection bleScannerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BLEScannerService.LocalBinder binder = (BLEScannerService.LocalBinder) iBinder;
            bleScannerService = binder.getService();
            if (bleScannerService != null) {
                if (!bleScannerService.initialize()) {
                    Log.e(TAG, "Ble scanner init failed");
                    return;
                }
                Log.d(TAG, "Ble scanner init successfully");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    // BroadcastReceiver handler
    private final BroadcastReceiver bleScannerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BLEScannerService.ACTION_SCAN_DONE:
                    Log.d(TAG, "BLEScannerService.ACTION_SCAN_DONE");
                    Log.d(TAG, String.format("Total of ble device: %d", bleScannerService.getBleDevices().size()));
                    Toast.makeText(getApplicationContext(), "Scan successfully", Toast.LENGTH_SHORT).show();
                    break;
                case BLEScannerService.ACTION_SCAN_NEW_DEVICE:
                    BLEDevicesAdapter bleDevicesAdapter = new BLEDevicesAdapter(getApplicationContext(), bleScannerService.getBleDevices());
                    lvBleDevices.setAdapter(bleDevicesAdapter);
                    lvBleDevices.deferNotifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    // Override functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Log.d(TAG, "onCreate");

        btnScan = findViewById(R.id.buttonBLEScan);
        btnStop = findViewById(R.id.buttonBLEStop);
        btnConnect = findViewById(R.id.buttonBLEConnect);
        lvBleDevices = findViewById(R.id.listview_ble_devices);
        selected_item = findViewById(R.id.selected_device_info);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        // Checks if Bluetooth is supported on the device.
        if (bluetoothManager == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bleScannerService != null) {
                    Log.i("btnScan", "Scanning BLE devices");
                    bleScannerService.startScan();
                } else {
                    Log.i("btnScan", "bleScannerService is null");
                }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bleScannerService != null) {
                    Log.i("btnStop", "Stop BLE scanner");
                    bleScannerService.stopScan();
                } else {
                    Log.i("btnStop", "bleScannerService is null");
                }
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        lvBleDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView device_name = selected_item.findViewById(R.id.ble_device_name);
                TextView device_address = selected_item.findViewById(R.id.ble_device_address);
                String name = ((TextView) view.findViewById(R.id.ble_device_name)).getText().toString();
                String addr = ((TextView) view.findViewById(R.id.ble_device_address)).getText().toString();
                device_name.setText(name);
                device_address.setText(addr);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        Intent gattServiceIntent = new Intent(this, BLEScannerService.class);
        bindService(gattServiceIntent, bleScannerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        unbindService(bleScannerConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bleScannerReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(bleScannerReceiver, BLEScannerService.intentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}