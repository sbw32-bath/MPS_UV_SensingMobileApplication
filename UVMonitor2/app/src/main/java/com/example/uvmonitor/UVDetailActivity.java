package com.example.uvmonitor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.uvmonitor.database.UVDataEntity;
import com.example.uvmonitor.database.UVDatabase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UVDetailActivity extends AppCompatActivity {

    private LineChart uvDetailChart;
    private String selectedDate;
    private UVDatabase database;
    private List<Entry> uvEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uv_detail);

        uvDetailChart = findViewById(R.id.uv_detail_chart);

        // Get the date from the intent
        selectedDate = getIntent().getStringExtra("date");

        database = UVDatabase.getInstance(this);

        setupChart();
        loadUVDataForDay(selectedDate);
    }

    private void setupChart() {
        LineDataSet dataSet = new LineDataSet(uvEntries, "UV Intensity");
        LineData lineData = new LineData(dataSet);
        uvDetailChart.setData(lineData);

        uvDetailChart.getDescription().setText("UV Intensity for " + selectedDate);
        uvDetailChart.invalidate();
    }

    private void loadUVDataForDay(String date) {
        database.uvDataDao().getUVDataByDate(date).observe(this, new Observer<List<UVDataEntity>>() {
            @Override
            public void onChanged(List<UVDataEntity> uvDataEntities) {
                uvEntries.clear();
                int index = 0;

                for (UVDataEntity data : uvDataEntities) {
                    uvEntries.add(new Entry(index++, data.uvValue));
                }

                uvDetailChart.getData().notifyDataChanged();
                uvDetailChart.notifyDataSetChanged();
                uvDetailChart.invalidate();
            }
        });
    }
}
