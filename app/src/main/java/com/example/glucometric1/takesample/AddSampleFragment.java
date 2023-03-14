package com.example.glucometric1.takesample;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.glucometric1.R;
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
    public static final ArrayList<String> listWavelengthLabels = new ArrayList<>(List.of("410", "435", "460", "485", "510", "535", "560", "585", "610", "645", "680", "705", "730", "760", "810", "860", "900", "940"));
    // TODO: Variables are defined by user
    private static String CSV_FILE_PATH;
    private static String CSV_FILE_NAME;
    private static String APP_SCRIPT_URL;
    Button btnTakeSample, btnSave, btnRandom;
    EditText editTextWavelengthValues, editTextGlucoseValue, editTextNote;
    BarData barData;
    BarDataSet barDataSet;
    BarChart barchart;
    ProgressBar progressBarSavaData;
    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayList<BarEntry> barEntriesList;

    public AddSampleFragment() {

    }

    // TODO: Rename and change types and number of parameters
    public static AddSampleFragment newInstance(String param1, String param2) {
        AddSampleFragment fragment = new AddSampleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        APP_SCRIPT_URL = getResources().getString(R.string.app_script_url);
        CSV_FILE_NAME = getResources().getString(R.string.csv_file_name);
        CSV_FILE_PATH = getActivity().getFilesDir() + "/" + CSV_FILE_NAME;
        Log.i("CSV_FILE_PATH", CSV_FILE_PATH);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_sample, container, false);

        {
            // Bind component to layout xml
            btnSave = (Button) view.findViewById(R.id.btnSave);
            btnTakeSample = (Button) view.findViewById(R.id.btnTakeSample);
            btnRandom = (Button) view.findViewById(R.id.btnRandom);
            editTextWavelengthValues = (EditText) view.findViewById(R.id.editTextWavelengthValues);
            editTextNote = (EditText) view.findViewById(R.id.editTextNote);
            editTextGlucoseValue = (EditText) view.findViewById(R.id.editTextGlucoseValue);
            barchart = (BarChart) view.findViewById(R.id.barchart);
            progressBarSavaData = (ProgressBar) view.findViewById(R.id.progressBarSavaData);
        }

        {
            barchart.fitScreen();
            barchart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(listWavelengthLabels));
            barchart.getXAxis().setLabelCount(listWavelengthLabels.size(), false);
            barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barchart.getXAxis().setLabelRotationAngle(-60);
            barchart.getDescription().setEnabled(false);
        }

        // TODO: Override functions by programmer
        /*
         * TakeSample signal to GlucoseDevice to get Wavelength values over BLE connection
         * */
        btnTakeSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validGlucoseValue = editTextGlucoseValue.length() > 0;
//                boolean validNote = editTextNote.length() > 0 ? true : false;
                if (validGlucoseValue) {
                    appendData2CSVFile(CSV_FILE_PATH, arrayList);
                    appendData2Database();

                    editTextGlucoseValue.setHint(editTextGlucoseValue.getText());
                    editTextGlucoseValue.setText("");

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else {
                    Toast.makeText(getActivity(), "Glucose value is empty", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Debug", "Random");

                // Bar Chart processing
                barEntriesList = new ArrayList<>();
                barchart.clear();
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

                barDataSet = new BarDataSet(barEntriesList, "Wavelength");
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
                barData = new BarData(barDataSet);
                barchart.animateY(500);
                barchart.setData(barData);
            }
        });

        return view;
    }

    private void setEnableComponent(boolean b) {
        btnTakeSample.setEnabled(b);
        btnSave.setEnabled(b);
        btnRandom.setEnabled(b);
        editTextWavelengthValues.setEnabled(b);
    }

    private void volleyHTTPRequest(int requestMethod, String query) {
        progressBarSavaData.setVisibility(View.VISIBLE);
        setEnableComponent(false);

        String url = APP_SCRIPT_URL + query;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(requestMethod, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("onResponse", response);
                Toast.makeText(getActivity(), "Data was written to DB", Toast.LENGTH_SHORT).show();

                progressBarSavaData.setVisibility(View.GONE);
                setEnableComponent(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("onResponse", error.toString());
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();

                progressBarSavaData.setVisibility(View.GONE);
                setEnableComponent(true);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(stringRequest);
    }

    private void appendData2Database() {
        /*
         * -> Format data as string: value1|value2|...|value18|glucoseValue|note
         * <- At AppScript: decode by using String.split(',')
         */
        String dataAsString = "";
        for (String x : arrayList) {
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