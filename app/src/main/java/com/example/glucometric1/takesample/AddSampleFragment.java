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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
    public static final ArrayList<String> listWavelengthLabels = new ArrayList<>(
            List.of("410", "435", "460", "485", "510", "535", "560", "585", "610", "645", "680", "705", "730", "760", "810", "860", "900", "940"));
    // TODO: Variables are defined by user
    private static String CSV_FILE_PATH;
    private static String CSV_FILE_NAME;
    private static String APP_SCRIPT_URL;
    private static final ArrayList<String> arrayList = new ArrayList<>();
    private static Button btnTakeSample, btnSave, btnRandom;
    private static EditText editTextWavelengthValues, editTextGlucoseValue, editTextNote;
    private static EditText editTextHeight, editTextWeight, editTextAge;
    private static BarData barData;
    private static BarDataSet barDataSet;
    private static BarChart barchart;
    private static ProgressBar progressBarSavaData;
    private static ArrayList<BarEntry> barEntriesList;
    private static InputMethodManager imm;
    private static Spinner spinnerSex;
    private NotifyAffect notifyAffect;

    // TODO: Rename and change types and number of parameters
    public static AddSampleFragment newInstance(String param1, String param2) {
        AddSampleFragment fragment = new AddSampleFragment();
        return fragment;
    }

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
            barchart = view.findViewById(R.id.barchart);
            progressBarSavaData = view.findViewById(R.id.progressBarSavaData);
            spinnerSex = view.findViewById(R.id.spinnerSex);

            imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
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
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.sex, android.R.layout.simple_spinner_item);
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_sample, container, false);

        initComponent(view);

        // TODO: Override functions by programmer
        // TakeSample signal to GlucoseDevice to get Wavelength values over BLE connection
        btnTakeSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // Save spectra wavelengths to local (CSV) and cloud (GoogleSheet)
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("btnSave", "Save");
                boolean validGlucoseValue = editTextGlucoseValue.length() > 0;
                //boolean validNote = editTextNote.length() > 0 ? true : false;
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

        // View start visualize affectively
        simulateDataRandom();

        return view;
    }

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

    private void resetBarChart() {
        barEntriesList = new ArrayList<>();
        barchart.clear();
    }

    private void handleBarChar(ArrayList entriesList, String label) {
        barDataSet = new BarDataSet(entriesList, label);
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
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
        dataList.add(editTextAge.getText().toString());
        dataList.add(editTextHeight.getText().toString());
        dataList.add(editTextWeight.getText().toString());
        dataList.add(spinnerSex.getSelectedItem().toString());
        Log.i("processData4Saving", Arrays.toString(arrayList.toArray()));
        return dataList;
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

                notifyAffect.makeSuccess("Data was written to DB");
                progressBarSavaData.setVisibility(View.GONE);
                setEnableComponent(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("onResponse", error.toString());

                notifyAffect.makeFailed(error.toString());
                progressBarSavaData.setVisibility(View.GONE);
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