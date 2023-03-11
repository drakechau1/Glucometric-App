package com.example.glucometric1.takesample;

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

import com.example.glucometric1.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

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

    Button buttonTakeSample;
    Button buttonSave;
    Button buttonRandom;
    EditText editTextValues;
    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList barEntriesList;

    ArrayList<Double> arrayList = new ArrayList<Double>();
    ArrayList<String> listWavelengthLabels =
            new ArrayList<String>(List.of("410", "435","460", "485",
                    "510", "535", "560", "585",
                    "610", "645", "680","705", "730", "760","810", "860", "900", "940"));

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


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Debug", "Save data");
            }
        });

        buttonTakeSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Debug", "Take sample");
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
                    arrayList.add(rand);
                }
                Log.i("Debug", String.format("ArrayList size: %s", arrayList.size()));

                barEntriesList = new ArrayList<>();
                barChart.clear();
                editTextValues.setText(null);
                String textStringValues = "";
                for (int i = 0; i < 18; i++) {
                    textStringValues += String.format("<b>%s:</b>%.2f - ", listWavelengthLabels.get(i),arrayList.get(i));
                    barEntriesList.add(new BarEntry(i, arrayList.get(i).floatValue()));
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
}