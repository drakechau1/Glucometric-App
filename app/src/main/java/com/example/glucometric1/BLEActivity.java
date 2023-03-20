package com.example.glucometric1;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.glucometric1.bluetoothle.LeDeviceListAdapter;

public class BLEActivity extends AppCompatActivity {
    private final static String TAG = BLEActivity.class.getSimpleName();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static boolean scanning;
    private final Handler handler = new Handler();
    Button btnScan, btnStop, btnConnect;
    TextView bleDeviceAddress, bleConnectionStatus;
    ListView listBLEDevices;
    ExpandableListView listGattService;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private LeDeviceListAdapter leDeviceListAdapter;

    // Device scan callback.
    private final ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    leDeviceListAdapter.addDevice(result.getDevice());
                    leDeviceListAdapter.notifyDataSetChanged();


                    @SuppressLint("MissingPermission") final String deviceName = result.getDevice().getName();
                    if (deviceName != null && deviceName.length() > 0)
                        Log.i("ScanCallback", "Device name: " + result.getDevice().toString());

                    Log.i("ScanCallback", "Device address: " + result);
                }
            };


    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        // Stops scanning after a predefined scan period.
        handler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(leScanCallback);
            }
        }, SCAN_PERIOD);
        bluetoothLeScanner.startScan(leScanCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopScanLeDevice() {
        bluetoothLeScanner.stopScan(leScanCallback);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Log.i(TAG, "Opened");

        btnScan = (Button) findViewById(R.id.buttonBLEScan);
        btnStop = (Button) findViewById(R.id.buttonBLEStop);
        btnConnect = (Button) findViewById(R.id.buttonBLEConnect);
        bleDeviceAddress = (TextView) findViewById(R.id.textViewBLEDeviceAddress);
        bleConnectionStatus = (TextView) findViewById(R.id.textViewBLEConnectionState);
        listBLEDevices = (ListView) findViewById(R.id.listViewBLEDevices);
        listGattService = (ExpandableListView) findViewById(R.id.listViewGattService);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothManager == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "null bluetoothLeScanner", Toast.LENGTH_LONG);
        }
        leDeviceListAdapter = new LeDeviceListAdapter(this);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("btnScan", "Scanning BLE devices");
                scanLeDevice();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("btnStop", "Stop BLE scanner");
                stopScanLeDevice();
            }
        });
    }
}