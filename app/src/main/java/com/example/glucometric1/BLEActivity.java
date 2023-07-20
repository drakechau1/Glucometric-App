package com.example.glucometric1;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.airbnb.lottie.LottieDrawable;
import com.amrdeveloper.lottiedialog.LottieDialog;
import com.example.glucometric1.bluetoothle.BLEDevicesAdapter;
import com.example.glucometric1.bluetoothle.BLEGATTService;
import com.example.glucometric1.bluetoothle.BleScanner;

import java.util.List;

public class BLEActivity extends AppCompatActivity {
    private static final String TAG = "BLEActivity";

    // Widget variables
    private Button btnConnect;
    private ImageButton btnScan;
    private LinearLayout selected_item;
    private ListView lvBleDevices;
    private ImageView imageView_device;

    // BLE Variables
    private int isConnected = BLEGATTService.STATE_DISCONNECTED;
    private BleScanner bleScanner;
    private BLEGATTService bleGattService;

    // Lottie Dialog
    private LottieDialog lottieDialog;

    // Constants
    private static final int PERMISSION_REQUEST_BLUETOOTH = 1;

    // Service Connection
    private final ServiceConnection bleGattConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            BLEGATTService.LocalBinder binder = (BLEGATTService.LocalBinder) iBinder;
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

    // BroadcastReceiver for GATT Service
    private final BroadcastReceiver bleGattReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BLEGATTService.ACTION_GATT_CONNECTED:
                    isConnected = BLEGATTService.STATE_CONNECTED;
                    break;
                case BLEGATTService.ACTION_GATT_DISCONNECTED:
                    isConnected = BLEGATTService.STATE_DISCONNECTED;
                    break;
                case BLEGATTService.ACTION_DEVICE_ERROR:
                    isConnected = BLEGATTService.STATE_DEVICE_ERROR;
                    break;
                default:
                    break;
            }
        }
    };

    // Scan Callback for BLE Scanner
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (ActivityCompat.checkSelfPermission(BLEActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            BluetoothDevice device = result.getDevice();
            if (device.getName() != null && !bleScanner.getScannedDevices().contains(device)) {
                bleScanner.addScannedDevice(device);
                updateDeviceList();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("Error code", String.valueOf(errorCode));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Log.d(TAG, "onCreate");

        initView();
        initBLEScanner();
        initListeners();

        btnConnect.setEnabled(false);
    }

    private void initView() {
        btnScan = findViewById(R.id.buttonBLEScan);
        btnConnect = findViewById(R.id.buttonBLEConnect);
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
    }

    private void initBLEScanner() {
        // Use this check to determine whether Bluetooth classic is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bleScanner = new BleScanner(bluetoothManager.getAdapter());
    }

    private void initListeners() {
        btnScan.setOnClickListener(view -> {
            if (bleScanner != null) {
                startScanning();
                lvBleDevices.setVisibility(View.VISIBLE);
                lottieDialog.show();
            } else {
                Log.i(TAG, "bleScannerService is null");
            }
        });

        btnConnect.setOnClickListener(view -> {
            if (isConnected == BLEGATTService.STATE_DISCONNECTED) {
                String name = ((TextView) selected_item.findViewById(R.id.ble_device_name)).getText().toString();
                String address = ((TextView) selected_item.findViewById(R.id.ble_device_address)).getText().toString();
                Intent intent = new Intent(this, TakeSampleActivity.class);
                intent.putExtra("ble_device_name", name);
                intent.putExtra("ble_device_address", address);
                Toast.makeText(this, "Bluetooth device successfully connected: ", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else {
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

            btnConnect.setEnabled(true);

            String glucoseDevice = "UIT-GLUCOSE-202334684"; // Example ID of device to get long ID

            if (glucoseDevice.length() == name.length()) {
                imageView_device.setImageResource(R.drawable.healthcare_cover);
            } else {
                imageView_device.setImageResource(R.drawable.icon_bluetooth2png);
            }
        });
    }

    private void startScanning() {
        bleScanner.startScanning(scanCallback);
        new Handler().postDelayed(this::stopScanning, bleScanner.SCAN_PERIOD);
    }

    private void stopScanning() {
        bleScanner.stopScanning();
        updateDeviceList();
        lottieDialog.dismiss();
    }

    private void updateDeviceList() {
        BLEDevicesAdapter bleDevicesAdapter = new BLEDevicesAdapter(getApplicationContext(), bleScanner.getScannedDevices());
        lvBleDevices.setAdapter(bleDevicesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        Intent gattServiceIntent = new Intent(this, BLEGATTService.class);
        bindService(gattServiceIntent, bleGattConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(bleGattReceiver, BLEGATTService.intentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bleGattReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(bleGattConnection);
        Log.d(TAG, "onDestroy");
    }
}
