package com.example.glucometric1.takesample;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.glucometric1.R;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
    // TODO: Variables are defined by user
    private static String CSV_FILE_PATH;
    private static String CSV_FILE_NAME;
    private static String APP_SCRIPT_URL;
    Button buttonReloadRecord;

    public RecordFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static RecordFragment newInstance(String param1, String param2) {
        RecordFragment fragment = new RecordFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        buttonReloadRecord = (Button) view.findViewById(R.id.buttonReloadRecord);

        buttonReloadRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FetchDataFromCSVFile(CSV_FILE_PATH);
            }
        });


        return view;
    }

    public void FetchDataFromCSVFile(String filename) {
        Log.i("Debug", "Log data from CSV");

        try {
            // Create an object of file reader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader(new File(filename));

            // create csvReader object and skip first Line
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(0)
                    .build();
            List<String[]> allData = csvReader.readAll();

            // print Data
            for (String[] row : allData) {
                Log.i("ReadAllData", Arrays.toString(row));
            }
            int numberOfSamples = allData.size();
            Log.i("NumberOfSamples", String.valueOf(numberOfSamples));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}