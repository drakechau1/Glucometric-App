package com.example.glucometric1;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.amrdeveloper.lottiedialog.LottieDialog;
import com.example.glucometric1.bluetoothle.BLEDevicesAdapter;
import com.example.glucometric1.bluetoothle.BLEGATTService;
import com.example.glucometric1.bluetoothle.BLEScannerService;

public class BLEActivity extends AppCompatActivity {
    private final static String TAG = "BLEActivity";

    //
    //
    // Widget variables
    Button btnStop, btnConnect;
    ImageButton btnScan;
    //TextView tvStatus;
    LinearLayout selected_item;
    ListView lvBleDevices;
    ImageView imageView_device;

    LottieDialog lottieDialog;
    //
    //
    // Assigned variables
    int isConnected = BLEGATTService.STATE_DISCONNECTED;
    //
    //
    // Unassigned variables
    BLEScannerService bleScannerService;
    BLEGATTService bleGattService;
    //
    //
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
            } else {
                Log.e(TAG, "Unable to get bleScannerService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private final ServiceConnection bleGattConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            BLEGATTService.LocalBinder binder = ((BLEGATTService.LocalBinder) iBinder);
            bleGattService = binder.getService();
            if (bleGattService != null) {
                if (!bleGattService.initialize()) {
                    Log.e(TAG, "Ble GATT server init failed");
                    return;
                }
                Log.d(TAG, "Ble GATT server init successfully");
            } else {
                Log.e(TAG, "Unable to get bleGattService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //
    //
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
                    Toast.makeText(getApplicationContext(),String.format("%d devices founded",bleScannerService.getBleDevices().size()),Toast.LENGTH_SHORT).show();
                    lottieDialog.dismiss();
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
    private final BroadcastReceiver bleGattReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BLEGATTService.ACTION_GATT_CONNECTED:
                    //tvStatus.setText(getResources().getString(R.string.label_ble_connected));
                    Log.e(TAG, "GATT connected");
                    isConnected = BLEGATTService.STATE_CONNECTED;
                    break;
                case BLEGATTService.ACTION_GATT_DISCONNECTED:
                    //tvStatus.setText(getResources().getString(R.string.label_ble_disconnected));
                    Log.e(TAG, "GATT disconnected");
                    isConnected = BLEGATTService.STATE_DISCONNECTED;
                    break;
                case BLEGATTService.ACTION_DEVICE_ERROR:
                    isConnected = BLEGATTService.STATE_DEVICE_ERROR;
                    Log.e(TAG, "Device turn off");
                default:
                    break;
            }
        }
    };

    //
    //
    // Override functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Log.d(TAG, "onCreate");

        btnScan = findViewById(R.id.buttonBLEScan);
        //btnStop = findViewById(R.id.buttonBLEStop);
        btnConnect = findViewById(R.id.buttonBLEConnect);
        //tvStatus = findViewById(R.id.textviewBLEStatus);
        lvBleDevices = findViewById(R.id.listview_ble_devices);
        selected_item = findViewById(R.id.selected_device_info);
        imageView_device = findViewById(R.id.imageView_device);

        lottieDialog = new LottieDialog(this)
                .setAnimation(R.raw.bluetooth_searching)
                .setAnimationViewHeight(2000)
                .setAnimationViewHeight(2000)
                .setAnimationRepeatCount(LottieDrawable.INFINITE)
                .setAutoPlayAnimation(true)
                .setMessage("Scanning for devices...")
                .setMessageTextSize(18)
                .setDialogBackground(Color.WHITE)
                .setDialogHeight(1000)
                .setDialogWidth(1000)
                .setCancelable(false);

        //Set visible
        btnConnect.setVisibility(View.GONE);
        selected_item.setVisibility(View.GONE);

        // Use this check to determine whether Bluetooth classic is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
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

        btnScan.setOnClickListener(view -> {
            if (bleScannerService != null) {
                Log.i("btnScan", "Scanning BLE devices");
                lvBleDevices.setVisibility(View.VISIBLE);
                bleScannerService.startScan();
                lottieDialog.show();
            } else {
                Log.i("btnScan", "bleScannerService is null");
            }
        });
//        btnStop.setOnClickListener(view -> {
//            if (bleScannerService != null) {
//                Log.i("btnStop", "Stop BLE scanner");
//                bleScannerService.stopScan();
//            } else {
//                Log.i("btnStop", "bleScannerService is null");
//            }
//        });

        btnConnect.setOnClickListener(view -> {
            if (isConnected == BLEGATTService.STATE_DISCONNECTED) {
                String name = ((TextView) selected_item.findViewById(R.id.ble_device_name)).getText().toString();
                String address = ((TextView) selected_item.findViewById(R.id.ble_device_address)).getText().toString();
//                bleGattService.connect(address);
                Intent intent = new Intent(this, TakeSampleActivity.class);
                intent.putExtra("ble_device_name", name);
                intent.putExtra("ble_device_address", address);
                Toast.makeText(this, "Bluetooth device successfully connected:"+ name.toString(), Toast.LENGTH_SHORT).show();
                startActivity(intent);
                //Set visible
                btnConnect.setVisibility(View.GONE);
                selected_item.setVisibility(View.GONE);
                lvBleDevices.setVisibility(View.GONE);
                //btnConnect.setText("Disconnect");
                //            finish();
//            } else if (isConnected == BLEGATTService.STATE_CONNECTED) {
////                bleGattService.close();
//                btnConnect.setText("Connect");
           }
            else
            {
                Toast.makeText(this, "Bluetooth device unsuccessfully connected", Toast.LENGTH_SHORT).show();
            }
        });


        lvBleDevices.setOnItemClickListener((adapterView, view, i, l) -> {
            TextView device_name = selected_item.findViewById(R.id.ble_device_name);
            TextView device_address = selected_item.findViewById(R.id.ble_device_address);
            String name = ((TextView) view.findViewById(R.id.ble_device_name)).getText().toString();
            String address = ((TextView) view.findViewById(R.id.ble_device_address)).getText().toString();
            device_name.setText(name);
            device_address.setText(address);
            String glucose_device = "UIT-GLUCOSE-202334684"; //Example ID of device to get long ID
            //Set Visibility
            btnConnect.setVisibility(View.VISIBLE);
            selected_item.setVisibility(View.VISIBLE);

            if (glucose_device.length() == name.length())
            {
                Drawable myDrawable = getResources().getDrawable(R.drawable.healthcare_cover);
                imageView_device.setImageDrawable(myDrawable);
            }
            else
            {
                Drawable myDrawable = getResources().getDrawable(R.drawable.icon_bluetooth2png);
                imageView_device.setImageDrawable(myDrawable);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        Intent scannerServiceIntent = new Intent(this, BLEScannerService.class);
        bindService(scannerServiceIntent, bleScannerConnection, Context.BIND_AUTO_CREATE);
        Intent gattServiceIntent = new Intent(this, BLEGATTService.class);
        bindService(gattServiceIntent, bleGattConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        unbindService(bleScannerConnection);
        unbindService(bleGattConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bleScannerReceiver);
        unregisterReceiver(bleGattReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(bleScannerReceiver, BLEScannerService.intentFilter());
        registerReceiver(bleGattReceiver, BLEGATTService.intentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(bleScannerConnection);
//        unbindService(bleGattConnection);
        Log.d(TAG, "onDestroy");
    }
}