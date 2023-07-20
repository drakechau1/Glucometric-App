package com.example.glucometric1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.airbnb.lottie.LottieDrawable;
import com.amrdeveloper.lottiedialog.LottieDialog;
import com.example.glucometric1.custom_textview.PoppinsMediumTextView;
import com.example.glucometric1.takesample.NotifyAffect;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class DatasetActivity extends AppCompatActivity {
    private NotifyAffect notifyAffect;
    LineChart lineChart;
    LottieDialog lottieDialog;
    private ListView DataHistoryListView;
    private ArrayList<DataHistory> dataHistoryArrayList = new ArrayList<>();
    private DataHistoryAdapter dataHistoryAdapter;
    private PoppinsMediumTextView Greeting;
    private LineDataSet lineDataSet = new LineDataSet(null,null);
    private ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    private LineData lineData;
    private float Data_Glucose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset);

        Log.i(Thread.currentThread().getStackTrace()[2].getClassName().toString(), "Opened");
        notifyAffect = new NotifyAffect(DatasetActivity.this);
        lineChart = findViewById(R.id.line_chart);
        Greeting = (PoppinsMediumTextView)findViewById(R.id.Greeting_textview);
        DataHistoryListView = (ListView)findViewById(R.id.list_view);
        lottieDialog = new LottieDialog(this)
                .setAnimation(R.raw.medical_shield)
                .setAnimationViewHeight(2000)
                .setAnimationViewHeight(2000)
                .setAnimationRepeatCount(LottieDrawable.INFINITE)
                .setAutoPlayAnimation(true)
                .setMessage("Histories of Data is loading...")
                .setMessageTextSize(18)
                .setDialogBackground(Color.WHITE)
                .setDialogHeight(1000)
                .setDialogWidth(1000)
                .setCancelable(false);

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

        GetDataFromDB();
    }


    private void AddDataHistory(String patient_name, String patient_gender,
                                String patient_timestamps, String patient_age, String patient_glucose_value)
    {
        dataHistoryArrayList.add(new DataHistory(patient_name,patient_gender,patient_timestamps,patient_age,patient_glucose_value ));
        dataHistoryAdapter = new DataHistoryAdapter(this,R.layout.listitem_data_history,dataHistoryArrayList);
        DataHistoryListView.setAdapter(dataHistoryAdapter);
    }
    public void GetDataFromDB() {
        lottieDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");
        Query checkUser = databaseReference.orderByKey();
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Entry> dataValue = new ArrayList<Entry>();
                if (snapshot.hasChildren()) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String nameFromDB = dataSnapshot.child("name").getValue().toString();
                    String ageFromDB = dataSnapshot.child("age").getValue().toString();
                    String timestampFromDB = dataSnapshot.child("timestamp").getValue().toString();
                    String genderFromDB = dataSnapshot.child("gender").getValue().toString();
                    String glucoseFromDB = dataSnapshot.child("glucose").getValue().toString();
                    String idFromDB     = dataSnapshot.child("id").getValue().toString();
                    AddDataHistory(nameFromDB, genderFromDB, timestampFromDB, ageFromDB, glucoseFromDB);
                    Data_Glucose = Float.parseFloat(glucoseFromDB);
                    float id = Float.parseFloat(idFromDB);
                    Log.i("DEBUG_LINE", "Data: " + Data_Glucose);
                    Log.i("ID", "ID: " + id);
                    dataValue.add(new Entry(id, Data_Glucose));
                    lottieDialog.dismiss();
                }
                    showLineChart(dataValue);
                }else
                {
                    notifyAffect.makeFailed("No data");
                    lineChart.clear();
                    lineChart.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showLineChart(ArrayList<Entry> dataValue) {
        lineDataSet.setValues(dataValue);
        lineDataSet.setLabel("Glucose Value");
        lineDataSet.setDrawFilled(true);
        lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        lineDataSet.setLineWidth(4);
        lineDataSet.setValueTextSize(13f);
        iLineDataSets.clear();
        iLineDataSets.add(lineDataSet);
        lineData = new LineData(iLineDataSets);
        lineChart.setVisibleXRangeMaximum(10f);
        lineChart.moveViewToX(10);
        lineChart.clear();
        lineChart.setDescription(null);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}