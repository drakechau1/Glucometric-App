package com.example.glucometric1.takesample;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddSampleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddSampleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    // TODO: Variables are defined by user
    private static final String CSV_FILE_PATH = "result.csv";
    Button buttonTakeSample;
    Button buttonSave;
    Button buttonRandom;
    EditText editTextValues;
    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList barEntriesList;
    ProgressBar progressBar;

    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayList<String> listWavelengthLabels =
            new ArrayList<String>(List.of("410", "435", "460", "485",
                    "510", "535", "560", "585",
                    "610", "645", "680", "705", "730", "760", "810", "860", "900", "940"));

    public AddSampleFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddSampleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddSampleFragment newInstance(String param1, String param2) {
        AddSampleFragment fragment = new AddSampleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean addDataToCSV(String outputPath, ArrayList<String> arrayList) {
        // Source base on: https://www.geeksforgeeks.org/writing-a-csv-file-in-java-using-opencsv/

        // first create file object for file placed at location specified by filepath
        File file = new File(outputPath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outfile = new FileWriter(file, true);
            CSVWriter writer = new CSVWriter(outfile);

            String dataArray[] = arrayList.toArray(new String[arrayList.size()]);
            writer.writeNext(dataArray);

            Log.i("WriteData2CSV", "Wrote: " + Arrays.toString(dataArray));

            // closing writer connection
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_sample, container, false);

        buttonSave = (Button) view.findViewById(R.id.btnSave);
        buttonTakeSample = (Button) view.findViewById(R.id.btnTakeSample);
        buttonRandom = (Button) view.findViewById(R.id.btnRandom);
        editTextValues = (EditText) view.findViewById(R.id.editTextWavelengthValues);
        barChart = (BarChart) view.findViewById(R.id.barchart);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarSavaData);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Debug", "Save data");
                String path = getActivity().getFilesDir() + "/" + CSV_FILE_PATH;
                Log.i("FilePath", path);
                if (addDataToCSV(path, arrayList)) {
                    Toast.makeText(getActivity(), "Success: Data wrote to CSV file", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getActivity(), "Failed: Data didn't write to CSV file", Toast.LENGTH_SHORT).show();

            }
        });

        buttonTakeSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dataStr = "";
                for (String x :
                        arrayList) {
                    dataStr += x + "|";
                }
                Log.i("String", dataStr);
                postDataUsingVolley(dataStr);

            }
        });

        buttonRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Debug", "Random");

                arrayList.clear();
                Log.i("Debug", String.format("ArrayList size: %s", arrayList.size()));
                for (int i = 0; i < 18; i++) {
                    double rand = Math.random() * 10000 + 0;
                    arrayList.add(String.valueOf(rand));
                }
                Log.i("Debug", String.format("ArrayList size: %s", arrayList.size()));

                // Bar Chart processing
                barEntriesList = new ArrayList<>();
                barChart.clear();
                editTextValues.setText(null);
                String textStringValues = "";
                for (int i = 0; i < 18; i++) {
                    float fValue = Float.parseFloat(arrayList.get(i));
                    textStringValues += String.format("<b>%s:</b>%.0f   ", listWavelengthLabels.get(i), fValue);
                    barEntriesList.add(new BarEntry(i, fValue));
                }
                editTextValues.setText(Html.fromHtml(textStringValues, Html.FROM_HTML_MODE_COMPACT));

                barDataSet = new BarDataSet(barEntriesList, "Wavelength");
                barData = new BarData(barDataSet);
                barChart.setData(barData);
                barChart.fitScreen();
                barChart.setFitBars(true);
                barChart.animateY(500);
                barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(listWavelengthLabels));
                barChart.getXAxis().setLabelCount(barEntriesList.size(), false);
                barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChart.getXAxis().setLabelRotationAngle(-60);
                barChart.getDescription().setEnabled(false);
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
            }
        });
        return view;
    }

    private void setEnableComponent(boolean b)
    {
        buttonTakeSample.setEnabled(b);
        buttonSave.setEnabled(b);
        buttonRandom.setEnabled(b);
        editTextValues.setEnabled(b);
    }

    private void postDataUsingVolley(String query) {
// Instantiate the RequestQueue.

        progressBar.setVisibility(View.VISIBLE);
        setEnableComponent(false);

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://script.google.com/macros/s/AKfycbz-rAqupPrxGvLU77kRXdGvv9THJOGMJ1bn-sjKCgIClMPUBABSLLigcpqs6E_cENw/exec?itemName=" + query;

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        progressBar.setVisibility(View.GONE);
                        setEnableComponent(true);

                        Log.i("onResponse", response);
                        Toast.makeText(getActivity(), "Data was written to DB", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                setEnableComponent(true);

                Log.i("onResponse", error.toString());
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}