package com.example.glucometric1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class DataHistoryAdapter extends BaseAdapter {

    Context mContext;
    int mInflater;
    private List<DataHistory> mDataHistory;

    public DataHistoryAdapter(Context mContext, int mInflater, List<DataHistory> mDataHistory) {
        this.mContext = mContext;
        this.mInflater = mInflater;
        this.mDataHistory = mDataHistory;
    }

    @Override
    public int getCount() {
        return mDataHistory.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(mInflater, null);
        TextView patientGlucoseValue = convertView.findViewById(R.id.textview_value);
        TextView patientName = convertView.findViewById(R.id.patient_name);
        TextView patientGender = convertView.findViewById(R.id.patient_gender);
        TextView patientAge = convertView.findViewById(R.id.patient_age);
        TextView patientTime = convertView.findViewById(R.id.time_date);
        LinearLayout item_layout_patient = convertView.findViewById(R.id.itemlistView_data_history);

        DataHistory data_history;
        try {
            data_history = mDataHistory.get(position);
            patientGlucoseValue.setText(data_history.getPatient_glucose_value());
            patientName.setText(data_history.getPatient_name());
            patientAge.setText(data_history.getPatient_age());
            patientGender.setText(data_history.getPatient_gender());
            patientTime.setText(data_history.getPatient_timestamps());
        }catch (IndexOutOfBoundsException e){
            Toast.makeText(mContext, "SOMETHING WRONG", Toast.LENGTH_SHORT).show();
        }
        return convertView;
    }



}
