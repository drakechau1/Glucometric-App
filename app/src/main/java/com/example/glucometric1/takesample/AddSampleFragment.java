package com.example.glucometric1.takesample;

import static com.example.glucometric1.bluetoothle.BLEGATTService.STATE_DEVICE_ERROR;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.amrdeveloper.lottiedialog.LottieDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.glucometric1.BLEActivity;
import com.example.glucometric1.MainActivity;
import com.example.glucometric1.R;
import com.example.glucometric1.Splash;
import com.example.glucometric1.bluetoothle.BLEGATTService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddSampleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddSampleFragment extends Fragment {
    private static final String TAG = "AddSampleFragment";
    public static final ArrayList<String> listWavelengthLabels = new ArrayList<>(List.of("410", "435", "460", "485", "510", "535", "560", "585", "610", "645", "680", "705", "730", "760", "810", "860", "900", "940"));
    // TODO: Variables are defined by user
    private static String CSV_FILE_PATH;
    private static String CSV_FILE_NAME;
    private static String APP_SCRIPT_URL;
    private static ArrayList<String> arrayList = new ArrayList<>();
    private static Button btnTakeSample, btnSave, btnRandom;
    private static EditText editTextWavelengthValues, editTextGlucoseValue, editTextNote;
    private static EditText editTextHeight, editTextWeight, editTextAge, editTextTemp;
    private static BarData barData;
    private static BarDataSet barDataSet;
    private static BarChart barchart;
    //private static ProgressBar progressBarSavaData;
    private static ArrayList<BarEntry> barEntriesList;
    private static InputMethodManager imm;
    private static Spinner spinnerSex;
    private NotifyAffect notifyAffect;

    private TextView textViewConnectionStatus;

    private static String ble_device_name;
    private static String ble_device_address;

    private static BluetoothGattCharacteristic uit_glucose_characteristic_data, uit_glucose_characteristic_cmd;

    BLEGATTService bleGattService;

    private  static TextView buttonDisconnectDevice;
    private static boolean isDeviceConnected;

    private LottieDialog lottieDialog;

    // TODO: Rename and change types and number of parameters
    public static AddSampleFragment newInstance(String param1, String param2) {
        AddSampleFragment fragment = new AddSampleFragment();
        return fragment;
    }


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
                bleGattService.connect(ble_device_address);
                Handler mhandler = new Handler();
                mhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bleGattService.requestMtu(BLEGATTService.REQUEST_MTU);
                    }
                }, 1600);

            } else {
                Log.e(TAG, "Unable to get bleGattService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final BroadcastReceiver bleGattReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BLEGATTService.ACTION_GATT_CONNECTED:
                    textViewConnectionStatus.setText("Success connection: " + ble_device_name);
                    buttonDisconnectDevice.setVisibility(View.VISIBLE);
                    isDeviceConnected = true;
                    Log.d(TAG, "BLE connected");
                    break;
                case BLEGATTService.ACTION_GATT_DISCONNECTED:
                    textViewConnectionStatus.setText("Disconnected: " + ble_device_name);
                    Log.e(TAG, "BLE disconnected");
                    isDeviceConnected = false;
                    break;
                case BLEGATTService.ACTION_GATT_SERVICES_DISCOVERED:
                    List<BluetoothGattService> gattServices = bleGattService.getSupportedGattServices();
                    for (BluetoothGattService gattService : gattServices) {
                        Log.d(TAG, "Service uuid: " + gattService.getUuid());
                        List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            String uuid = characteristic.getUuid().toString();
                            Log.d(TAG, "uuid: " + uuid);
                            if (BLEGATTService.UIT_GLUCOSE_DATA.equals(uuid)) {
                                bleGattService.setCharacteristicNotification(characteristic, true);
                                bleGattService.readCharacteristic(characteristic);
                                uit_glucose_characteristic_data = characteristic;
                            } else if (BLEGATTService.UIT_GLUCOSE_CMD.equals(uuid)) {
                                uit_glucose_characteristic_cmd = characteristic;
                            }
                        }
                    }
                    break;
                case BLEGATTService.ACTION_DATA_AVAILABLE:
//                case BLEGATTService.ACTION_CHARACTERISTIC_CHANGE:
                    String uuid = intent.getStringExtra("char_uuid");
                    String data_value = intent.getStringExtra("data_value");
                    Log.d(TAG, "uuid: " + uuid);
                    Log.d(TAG, "value: " + data_value);
//                    Toast.makeText(getContext(), , Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "value " + intent.getStringExtra("data_value"));
                    String[] temp_array = data_value.split(",", 18);
                    Log.d(TAG, String.format("Length: %d", temp_array.length));
                    if (temp_array.length == 18) {
                        // Now convert string into ArrayList
                        arrayList = new ArrayList<String>(Arrays.asList(temp_array));
                        updateBarChart(temp_array);
                    }
                default:
                    break;
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private void initComponent(View view) {
        // Bind component to layout xml by id && service variables
        {
            btnSave = view.findViewById(R.id.btnSave);
            btnTakeSample = view.findViewById(R.id.btnTakeSample);
            btnRandom = view.findViewById(R.id.btnRandom);
            editTextWavelengthValues = view.findViewById(R.id.editTextWavelengthValues);
            editTextNote = view.findViewById(R.id.editTextNote);
            editTextGlucoseValue = view.findViewById(R.id.editTextGlucoseValue);
            editTextHeight = view.findViewById(R.id.editTextHeight);
            editTextWeight = view.findViewById(R.id.editTextWeight);
            editTextAge = view.findViewById(R.id.editTextAge);
            editTextTemp  = view.findViewById(R.id.editTextTemp);
            barchart = view.findViewById(R.id.barchart);
            //progressBarSavaData = view.findViewById(R.id.progressBarSavaData);
            spinnerSex = view.findViewById(R.id.spinnerSex);
            textViewConnectionStatus = view.findViewById(R.id.textViewConnectionStatus);
            textViewConnectionStatus.setText("No Device Connected");
            buttonDisconnectDevice = view.findViewById(R.id.buttonDisconnectDevice);
            lottieDialog = new LottieDialog(getActivity())
                    .setAnimation(R.raw.medical_shield)
                    .setAnimationViewHeight(2000)
                    .setAnimationViewHeight(2000)
                    .setAnimationRepeatCount(LottieDrawable.INFINITE)
                    .setAutoPlayAnimation(true)
                    .setMessage("Updating to Database...")
                    .setMessageTextSize(18)
                    .setDialogBackground(Color.WHITE)
                    .setDialogHeight(1000)
                    .setDialogWidth(1000)
                    .setCancelable(false);

            imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);

            isDeviceConnected = false;

        }

        // Config bar chart
        {
            barchart.fitScreen();
            barchart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(listWavelengthLabels));
            barchart.getXAxis().setLabelCount(listWavelengthLabels.size(), false);
            barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barchart.getXAxis().setLabelRotationAngle(-60);
            barchart.getDescription().setEnabled(false);
        }

        // Config sex spinner
        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sex, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinnerSex.setAdapter(adapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        APP_SCRIPT_URL = getResources().getString(R.string.app_script_url);
        CSV_FILE_NAME = getResources().getString(R.string.csv_file_name);
        CSV_FILE_PATH = getActivity().getFilesDir() + "/" + CSV_FILE_NAME;
        Log.i("CSV_FILE_PATH", CSV_FILE_PATH);

        notifyAffect = new NotifyAffect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ble_device_name = getArguments().getString("ble_device_name");
        ble_device_address = getArguments().getString("ble_device_address");
        Log.d(TAG, "Device name: " + ble_device_name);
        Log.d(TAG, "Device address: " + ble_device_address);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_sample, container, false);
        initComponent(view);

        // TODO: Override functions by programmer
        // TakeSample signal to GlucoseDevice to get Wavelength values over BLE connection
        btnTakeSample.setOnClickListener(new View.OnClickListener() {
            int check = 1;
            int restApp = 0;
            @Override
            public void onClick(View view) {
//                String cmd = "0x01C0";
//                byte[] cmd = {0xc0};
                Log.d(TAG, "Taking data");
                if (!isDeviceConnected)
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyAffect.makeFailed("No Device GlucoMetric Connected");
                        }
                    },2000);
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 1000,intent,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC,System.currentTimeMillis() + 5000, pendingIntent);
                }
                else
                {
                    if (uit_glucose_characteristic_cmd != null) {
                        uit_glucose_characteristic_cmd.setValue(new byte[]{(byte) 0xc0});
                        check = bleGattService.writeCharacteristic(uit_glucose_characteristic_cmd);

                        if (check == 0)
                        {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    notifyAffect.makeFailed("Device: "+ ble_device_name + " turn off bluetooth");
                                    restApp = 1;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            btnTakeSample.performClick();
                                        }
                                    },1000);

                                }
                            },3000);

                            if (restApp == 1)
                            {
                                restApp = 0 ;
                                System.exit(0);
                            }
                        }
                    }
                    Handler mhandler = new Handler();
                    mhandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Read characteristic");
                            bleGattService.readCharacteristic(uit_glucose_characteristic_data);
                        }
                    }, 7000);
                }

            }
        });
        // Save spectra wavelengths to local (CSV) and cloud (GoogleSheet)
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("btnSave", "Save");
                boolean validGlucoseValue = editTextGlucoseValue.length() > 0;
                //boolean validNote = editTextNote.length() > 0 ? true : false;
                if (!isDeviceConnected)
                {
                    notifyAffect.makeFailed("No Device GlucoMetric Connected");
                }
                if (validGlucoseValue) {
                    ArrayList<String> data4Saving = processData4Saving();
                    appendData2CSVFile(CSV_FILE_PATH, data4Saving);
                    appendData2Database(data4Saving);
                    // Hide softKeyBoard
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else {
                    notifyAffect.makeFailed("Glucose value is empty");
                }
            }
        });
        // Simulate data (for only debugging mode)
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("btnRandom", "Random");
                simulateDataRandom();
            }
        });

        // Handle button disconnected device
        buttonDisconnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openDialogDisconnectDevice();
            }
        });
        // View start visualize affectively
        simulateDataRandom();

        return view;
    }

    private void openDialogDisconnectDevice()
    {
        final Dialog dialogDisconnectDevice = new Dialog(getActivity());
        dialogDisconnectDevice.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDisconnectDevice.setContentView(R.layout.custom_layout_dialog_disconnect_bluetooth);
        Window window = dialogDisconnectDevice.getWindow();
        if (window == null)
        {
            Toast.makeText(getActivity(), "No window",Toast.LENGTH_SHORT).show();
            return;
        }
        dialogDisconnectDevice.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDisconnectDevice.setCancelable(false);

        Button btnYes = dialogDisconnectDevice.findViewById(R.id.buttonYes);
        Button btnNo  = dialogDisconnectDevice.findViewById(R.id.buttonNo);

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Keep connected bluetooth",Toast.LENGTH_SHORT).show();
                dialogDisconnectDevice.dismiss();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Disconnected bluetooth device:"+ ble_device_name,Toast.LENGTH_SHORT).show();
                bleGattService.close();
                textViewConnectionStatus.setText("No Device Connected");
                buttonDisconnectDevice.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 1000,intent,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC,System.currentTimeMillis() + 5000, pendingIntent);
                        Toast.makeText(getActivity(), "Successfully disconnect bluetooth", Toast.LENGTH_LONG).show();
                        System.exit(0);
                    }
                },2000);
            }
        });
        dialogDisconnectDevice.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent gattServiceIntent = new Intent(getActivity(), BLEGATTService.class);
        getActivity().bindService(gattServiceIntent, bleGattConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(bleGattReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(bleGattReceiver, BLEGATTService.intentFilter());
    }

    @SuppressLint("DefaultLocale")
    private void simulateDataRandom() {
        // Reset barchart
        resetBarChart();
        editTextWavelengthValues.setText(null);
        String textStringValues = "";
        arrayList.clear();

        for (int i = 0; i < 18; i++) {
            double rand = Math.random() * 10000 + 0;
            arrayList.add(String.valueOf(rand));
            textStringValues += String.format("<b>%s:</b>%.0f   ", listWavelengthLabels.get(i), rand);
            barEntriesList.add(new BarEntry(i, (float) rand));
        }

        editTextWavelengthValues.setText(Html.fromHtml(textStringValues, Html.FROM_HTML_MODE_COMPACT));

        handleBarChar(barEntriesList, "Wavelength");
    }

    @SuppressLint("DefaultLocale")
    private void updateBarChart(String[] arrayData) {
        // Reset barchart
        resetBarChart();
        editTextWavelengthValues.setText(null);
        String textStringValues = "";
        int i = 0;
        for (String s : arrayData) {
            Log.d(TAG, s);
            double rand = Double.parseDouble(s);
            textStringValues += String.format("<b>%s:</b>%s   ", listWavelengthLabels.get(i), s);
            barEntriesList.add(new BarEntry(i, (float) rand));
            i++;
        }

        editTextWavelengthValues.setText(Html.fromHtml(textStringValues, Html.FROM_HTML_MODE_COMPACT));

        handleBarChar(barEntriesList, "Wavelength");
    }

    private void resetBarChart() {
        barEntriesList = new ArrayList<>();
        barchart.clear();
    }

    private void handleBarChar(ArrayList entriesList, String label) {
        barDataSet = new BarDataSet(entriesList, label);
        int[] colors = new int[] {Color.rgb(58,40,113), Color.rgb(125,38,205)
                                    ,Color.rgb(40,55,105), Color.rgb(24,71,133)
                                    ,Color.rgb(32,90,167), Color.rgb(0,127,84)
                                    ,Color.rgb(54,117,23), Color.rgb(91,189,43)
                                    ,Color.rgb(220, 216,0), Color.rgb(249,244,0)
                                    ,Color.rgb(241,175,0), Color.rgb(240,156,66)
                                    ,Color.rgb(236,135,14) , Color.rgb(235,113,83)
                                    ,Color.rgb(223,53,57),Color.rgb(223,0,41)
                                    ,Color.rgb(182,41,43), Color.rgb(139,0,22)};
        barDataSet.setColors(colors);
        barDataSet.setValueTextColor(Color.BLACK);
        barData = new BarData(barDataSet);
        barchart.animateY(500);
        barchart.setData(barData);
    }

    private void setEnableComponent(boolean b) {
        btnTakeSample.setEnabled(b);
        btnSave.setEnabled(b);
        btnRandom.setEnabled(b);
        editTextWavelengthValues.setEnabled(b);
    }

    public ArrayList<String> processData4Saving() {
        // Format: wavelengths[18],age,height,weight,sex,note
        // Total field: 24
        ArrayList<String> dataList = new ArrayList<>(arrayList);
        dataList.add(editTextTemp.getText().toString());
        dataList.add(editTextAge.getText().toString());
        dataList.add(editTextHeight.getText().toString());
        dataList.add(editTextWeight.getText().toString());
        dataList.add(spinnerSex.getSelectedItem().toString());
        Log.i("processData4Saving", Arrays.toString(arrayList.toArray()));
        return dataList;
    }

    private void volleyHTTPRequest(int requestMethod, String query) {
        lottieDialog.show();
        //progressBarSavaData.setVisibility(View.VISIBLE);
        setEnableComponent(false);

        String url = APP_SCRIPT_URL + query;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(requestMethod, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("onResponse", response);

                notifyAffect.makeSuccess("Update data to Database successfully");
                lottieDialog.dismiss();
                //progressBarSavaData.setVisibility(View.GONE);
                setEnableComponent(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("onResponse", error.toString());

                notifyAffect.makeFailed(error.toString());
                lottieDialog.dismiss();
                //progressBarSavaData.setVisibility(View.GONE);
                notifyAffect.makeSuccess("Update data to Database fail");
                notifyAffect.makeFailed("No internet Connection");
                setEnableComponent(true);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(stringRequest);
    }

    private void appendData2Database(ArrayList<String> arrayData) {
        /*
         * -> Format data as string: value1|value2|...|value18|glucoseValue|note
         * <- At AppScript: decode by using String.split(',')
         */
        String dataAsString = "";
        for (String x : arrayData) {
            dataAsString += x + "|";
        }
        dataAsString += editTextGlucoseValue.getText().toString();
        dataAsString += "|" + editTextNote.getText().toString();

        Log.i("dataAsString", dataAsString);
        volleyHTTPRequest(Request.Method.GET, "itemName=" + dataAsString);
    }

    public boolean appendData2CSVFile(String outputPath, ArrayList<String> arrayList) {
        // Source base on: https://www.geeksforgeeks.org/writing-a-csv-file-in-java-using-opencsv/
        File file = new File(outputPath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outfile = new FileWriter(file, true);
            CSVWriter writer = new CSVWriter(outfile);

            // Convert ArrayList to String[]
            String[] dataArray = arrayList.toArray(new String[arrayList.size()]);
            writer.writeNext(dataArray);

            Log.i(TAG, "Success: " + Arrays.toString(dataArray));

            // closing writer connection
            writer.close();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Failed: Data didn't write to CSV file", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
        Toast.makeText(getActivity(), "Success: Data wrote to CSV file", Toast.LENGTH_SHORT).show();
        return true;
    }
}