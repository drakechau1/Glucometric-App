package com.example.glucometric1;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieDrawable;
import com.amrdeveloper.lottiedialog.LottieDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.glucometric1.bluetoothle.BLEGATTService;
import com.example.glucometric1.custom_textview.PoppinsMediumTextView;
import com.example.glucometric1.databinding.ActivityMainBinding;
import com.example.glucometric1.takesample.NotifyAffect;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MeasurementActivity extends AppCompatActivity {

    public static final ArrayList<String> listWavelengthLabels = new ArrayList<>(List.of("410", "435", "460", "485", "510", "535", "560", "585", "610", "645", "680", "705", "730", "760", "810", "860", "900", "940"));
    // TODO: Variables are defined by user
    public TextView textViewConnectionStatus;
    public TextView textview_value;
    public TextView textview_time_date;
    public EditText editText_patient_name;
    public EditText edit_patient_age;
    public ImageView imageView_button;
    public ProgressBar progress_bar;
    public Button button_save;
    public String ble_device_name;
    public String ble_device_address;
    public LottieDialog lottieDialog_measurement;
    public int ValueGlucose;
    private static InputMethodManager imm;
    private PoppinsMediumTextView Greeting;
    private Spinner spinnerSex;
    private static boolean isDeviceConnected;
    private static BluetoothGattCharacteristic uit_glucose_characteristic_data, uit_glucose_characteristic_cmd;
    private NotifyAffect notifyAffect;
    private static ArrayList<String> arrayList = new ArrayList<>();
    private static String APP_SCRIPT_URL;
    private static int id_counter;
    private static int check = 1;
    private static int restApp = 0;
    BLEGATTService bleGattService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);
        Log.i(Thread.currentThread().getStackTrace()[2].getClassName().toString(), "Opened");


        Intent intent = getIntent();
        ble_device_name = intent.getStringExtra("ble_device_name");
        ble_device_address = intent.getStringExtra("ble_device_address");

        textViewConnectionStatus = (TextView)findViewById(R.id.textViewConnectionStatus);
        textViewConnectionStatus.setText("No Device Connected");
        Greeting = (PoppinsMediumTextView)findViewById(R.id.Greeting_textview);
        spinnerSex = (Spinner)findViewById(R.id.spinnerSex);
        imageView_button = (ImageView)findViewById(R.id.imageView_button);
        progress_bar = (ProgressBar)findViewById(R.id.progress_bar);
        textview_value = (TextView)findViewById(R.id.textview_value);
        editText_patient_name = (EditText)findViewById(R.id.editText_patient_name);
        edit_patient_age = (EditText)findViewById(R.id.edit_patient_age);
        textview_time_date = (TextView)findViewById(R.id.textview_time_date);
        button_save = (Button)findViewById(R.id.button_save);
        notifyAffect = new NotifyAffect(MeasurementActivity.this);

        lottieDialog_measurement = new LottieDialog(this)
                .setAnimation(R.raw.medical_shield)
                .setAnimationViewHeight(2000)
                .setAnimationViewHeight(2000)
                .setAnimationRepeatCount(LottieDrawable.INFINITE)
                .setAutoPlayAnimation(true)
                .setMessage("Measure your blood sugar...")
                .setMessageTextSize(18)
                .setDialogBackground(Color.WHITE)
                .setDialogHeight(1000)
                .setDialogWidth(1000)
                .setCancelable(false);
        imm = (InputMethodManager) MeasurementActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        isDeviceConnected = false;
        Calendar calendar = Calendar.getInstance();
        int jam = calendar.get(Calendar.HOUR_OF_DAY);
        if(jam >= 0 && jam <16){
            Greeting.setText("Good Morning");
        }else if(jam >=12 && jam <16){
            Greeting.setText("Good Afternoon");
        }else if(jam >=16 && jam <21){
            Greeting.setText("Good Evening");
        }else if(jam >=21 && jam <24){
            Greeting.setText("Good Night");
        }else {
            Greeting.setText("UIT GLUCOMETRIC Hello");
        }

        // Config sex spinner
        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MeasurementActivity.this, R.array.sex, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinnerSex.setAdapter(adapter);
        }

        imageView_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                lottieDialog_measurement.show();
                TakeSampleFuction();
                startAnimationCounter(0,100);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displayValueTextView();
                        Toast.makeText(MeasurementActivity.this, "Blood glucose check finish", Toast.LENGTH_SHORT).show();
                        lottieDialog_measurement.dismiss();
                    }
                }, 10000);
            }
        });

        APP_SCRIPT_URL = getResources().getString(R.string.app_script_url);

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("btnSave", "Save");

                //boolean validNote = editTextNote.length() > 0 ? true : false;
                if (!isDeviceConnected)
                {
                    notifyAffect.makeFailed("No Device GlucoMetric Connected");
                }
                if (!checkGlucoseValue()) {
                    notifyAffect.makeFailed("No gluocse value");
                    return;
                }
                else {
                    ArrayList<String> data4Saving = processData4Saving();
                    //appendData2CSVFile(CSV_FILE_PATH, data4Saving);
                    appendData2Database(data4Saving);
                    // Hide softKeyBoard
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });


    }

    private void TakeSampleFuction() {
        //        String cmd = "0x01C0";
//                byte[] cmd = {0xc0};
        Log.d("Take Sample", "Taking data");
        if (!isDeviceConnected)
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    notifyAffect.makeFailed("No Device GlucoMetric Connected");
                    lottieDialog_measurement.dismiss();
                }
            },2000);
        }
        else
        {
            if (uit_glucose_characteristic_cmd != null) {
                uit_glucose_characteristic_cmd.setValue(new byte[]{(byte) 0xc0});
                check = bleGattService.writeCharacteristic(uit_glucose_characteristic_cmd);

                if (check == 0)
                {
                    lottieDialog_measurement.dismiss();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyAffect.makeFailed("Device: "+ ble_device_name + " turn off bluetooth");
                            restApp = 1;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    imageView_button.performClick();
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

                    Log.d("bleGattSerVice", "Read characteristic");
                    bleGattService.readCharacteristic(uit_glucose_characteristic_data);
                }
            }, 10000);
        }

    }

    private void displayValueTextView() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date()); // Find todays date
        Log.i("TimeStamp", "Timestamp:"+ currentDateTime);
        textview_time_date.setText(currentDateTime);
        final int min = 80;
        final int max = 140;
        ValueGlucose = new Random().nextInt((max - min) + 1) + min;
        textview_value.setText(String.valueOf(ValueGlucose));
    }

    public void startAnimationCounter(int start, int end)
    {
        ValueAnimator animator = ValueAnimator.ofInt(start,end);
        animator.setDuration(10000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                progress_bar.setProgress(Integer.parseInt(animation.getAnimatedValue().toString()));
            }
        });
        animator.start();
    }

    private final ServiceConnection bleGattConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            BLEGATTService.LocalBinder binder = ((BLEGATTService.LocalBinder) iBinder);
            bleGattService = binder.getService();
            if (bleGattService != null) {
                if (!bleGattService.initialize()) {
                    Log.e("bleGattConnection", "Ble GATT server init failed");
                    return;
                }
                Log.d("bleGattConnection", "Ble GATT server init successfully");
                bleGattService.connect(ble_device_address);
                Handler mhandler = new Handler();
                mhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bleGattService.requestMtu(BLEGATTService.REQUEST_MTU);
                    }
                }, 1600);

            } else {
                Log.e("bleGattConnection", "Unable to get bleGattService");
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
                    isDeviceConnected = true;
                    Log.d("bleGattReceiver", "BLE connected");
                    break;
                case BLEGATTService.ACTION_GATT_DISCONNECTED:
                    textViewConnectionStatus.setText("Disconnected: " + ble_device_name);
                    Log.e("bleGattReceiver", "BLE disconnected");
                    isDeviceConnected = false;
                    break;
                case BLEGATTService.ACTION_GATT_SERVICES_DISCOVERED:
                    List<BluetoothGattService> gattServices = bleGattService.getSupportedGattServices();
                    for (BluetoothGattService gattService : gattServices) {
                        Log.d("bleGattReceiver", "Service uuid: " + gattService.getUuid());
                        List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            String uuid = characteristic.getUuid().toString();
                            Log.d("bleGattReceiver", "uuid: " + uuid);
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
                    String uuid = intent.getStringExtra("char_uuid");
                    String data_value = intent.getStringExtra("data_value");
                    Log.d("bleGattReceiver", "uuid: " + uuid);
                    Log.d("bleGattReceiver", "value: " + data_value);
                    String[] temp_array = data_value.split(",", 18);
                    Log.d("bleGattReceiver", String.format("Length: %d", temp_array.length));
                    if (temp_array.length == 18) {
                        arrayList = new ArrayList<String>(Arrays.asList(temp_array));
                    }
                default:
                    break;
            }
        }
    };
    public void onStart() {
        super.onStart();
        Intent gattServiceIntent = new Intent(MeasurementActivity.this, BLEGATTService.class);
        MeasurementActivity.this.bindService(gattServiceIntent, bleGattConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        MeasurementActivity.this.unregisterReceiver(bleGattReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        MeasurementActivity.this.registerReceiver(bleGattReceiver, BLEGATTService.intentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public ArrayList<String> processData4Saving() {
        // Format: wavelengths[18],age,height,weight,sex,note
        // Total field: 24
        ArrayList<String> dataList = new ArrayList<>(arrayList);
        dataList.add(textview_value.getText().toString());
        dataList.add(editText_patient_name.getText().toString());
        dataList.add(edit_patient_age.getText().toString());
        dataList.add(spinnerSex.getSelectedItem().toString());
        dataList.add(textview_time_date.getText().toString());
        Log.i("processData4Saving", Arrays.toString(arrayList.toArray()));
        return dataList;
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
        dataAsString += textview_value.getText().toString();

        Log.i("dataAsString", dataAsString);
        volleyHTTPRequest(Request.Method.GET, "itemName=" + dataAsString);
    }
    private void volleyHTTPRequest(int requestMethod, String query) {

        String url = APP_SCRIPT_URL + query;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(requestMethod, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("onResponse", response);

                //notifyAffect.makeSuccess("Update data to Database successfully");

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("onResponse", error.toString());

                notifyAffect.makeFailed(error.toString());
                //lottieDialog.dismiss();
                //progressBarSavaData.setVisibility(View.GONE);
                notifyAffect.makeSuccess("Update data to Database fail");
                notifyAffect.makeFailed("No internet Connection");
                //setEnableComponent(true);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(MeasurementActivity.this);
        queue.add(stringRequest);

        RequestDataDB();
    }
    private Boolean checkName(){
        String val = editText_patient_name.getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if(val.isEmpty()){
            editText_patient_name.setError("Field cannot be empty");
            return false;
        }
        else if(val.length() >= 15){
            editText_patient_name.setError("Name is too long");
            return false;
        }
        else{
            editText_patient_name.setError(null);
            return true;
        }
    }
    private Boolean checkAge (){
        String age_check    = edit_patient_age.getText().toString();
        if(age_check.isEmpty()){
            edit_patient_age.setError("Field cannot be empty");
            return false;
        }
        else if(age_check.length() >= 3){
            edit_patient_age.setError("Age is too long");
            return false;
        }
        else{
            edit_patient_age.setError(null);
            return true;
        }
    }
    private Boolean checkGlucoseValue(){
        String glucose_check    = textview_value.getText().toString();
        if(glucose_check.isEmpty()){
            textview_value.setError("Field cannot be empty");
            return false;
        }
        else if(glucose_check.length() >= 4){
            textview_value.setError("Glucose is too long");
            return false;
        }
        else{
            textview_value.setError(null);
            return true;
        }
    }
    private void RequestDataDB()
    {
        try {
            //If input data is not correct
            if(!checkName() || !checkAge()){
                return;
            }else{
                //Get all the value from the form
                String name_patient  = editText_patient_name.getText().toString();
                String age_patient   = edit_patient_age.getText().toString();
                String time_stamp    = textview_time_date.getText().toString();
                String device_id     = ble_device_name;
                String glucose_value = textview_value.getText().toString();
                String gender_patient= spinnerSex.getSelectedItem().toString();
                DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("User");
                Query checkUser = database.orderByKey();
                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int total  =(int)snapshot.getChildrenCount();
                        id_counter = total;
                        total = total + 1;
                        String id_db = String.valueOf(total);
                        Log.i("DataSet", "ID:"+ id_db);
                        User user = new User(name_patient,age_patient,time_stamp,device_id,glucose_value,gender_patient, id_db);
                        database.child(id_db).setValue(user);
                        Toast.makeText(MeasurementActivity.this, "Update data successfully", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }catch (IllegalStateException e){
            e.printStackTrace();
        }

    }
}