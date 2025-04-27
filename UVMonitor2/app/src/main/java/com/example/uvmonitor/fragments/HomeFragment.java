package com.example.uvmonitor.fragments;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.uvmonitor.R;
import com.example.uvmonitor.ble.BLEManager;
import com.example.uvmonitor.database.UVDataEntity;
import com.example.uvmonitor.database.UVDatabase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private BLEManager bleManager;
    private TextView uvValueText;
    private Button turnOnBluetoothButton, scanButton, disconnectButton;
    private LineChart uvChart;
    private List<Entry> uvEntries = new ArrayList<>();
    private int dataPointIndex = 0;
    private UVDatabase database;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Handle permissions
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        bleManager = new BLEManager(requireContext());
        database = UVDatabase.getInstance(requireContext());

        uvValueText = root.findViewById(R.id.uv_value);
        turnOnBluetoothButton = root.findViewById(R.id.turn_on_bluetooth_button);
        scanButton = root.findViewById(R.id.scan_button);
        disconnectButton = root.findViewById(R.id.disconnect_button);
        uvChart = root.findViewById(R.id.uv_chart);

        setupChart();

        if (!bleManager.isBluetoothEnabled()) {
            turnOnBluetoothButton.setVisibility(View.VISIBLE);
        } else {
            scanButton.setVisibility(View.VISIBLE);
        }

        turnOnBluetoothButton.setOnClickListener(v -> {
            Intent enableBtIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(enableBtIntent);
        });

        scanButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                });
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                });
            }
            bleManager.startScan();
            showDeviceDialog();
        });

        disconnectButton.setOnClickListener(v -> {
            bleManager.disconnect();
            scanButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.GONE);
        });

        bleManager.getIsConnected().observe(getViewLifecycleOwner(), isConnected -> {
            if (isConnected) {
                scanButton.setVisibility(View.GONE);
                disconnectButton.setVisibility(View.VISIBLE);
            } else {
                disconnectButton.setVisibility(View.GONE);
                scanButton.setVisibility(View.VISIBLE);
            }
        });

        bleManager.getUVData().observe(getViewLifecycleOwner(), uvData -> {
            uvValueText.setText("UV Data: " + uvData);

            int value = 0;
            try {
                value = Integer.parseInt(uvData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            addUVDataPoint(value);
            saveDataToDatabase(value);
        });

        return root;
    }

    private void showDeviceDialog() {
        bleManager.getFoundDevices().observe(getViewLifecycleOwner(), new Observer<List<BluetoothDevice>>() {
            @Override
            public void onChanged(List<BluetoothDevice> devices) {
                if (!devices.isEmpty()) {
                    // Pick first device for now automatically
                    bleManager.connectToDevice(devices.get(0));
                    bleManager.stopScan();
                }
            }
        });
    }

    private void setupChart() {
        uvEntries.clear();
        LineDataSet dataSet = new LineDataSet(uvEntries, "UV Intensity");
        LineData lineData = new LineData(dataSet);
        uvChart.setData(lineData);

        Description desc = new Description();
        desc.setText("Today's UV Data");
        uvChart.setDescription(desc);
        uvChart.invalidate();
    }

    private void addUVDataPoint(int value) {
        uvEntries.add(new Entry(dataPointIndex++, value));
        uvChart.getData().notifyDataChanged();
        uvChart.notifyDataSetChanged();
        uvChart.invalidate();
    }

    private void saveDataToDatabase(int uvValue) {
        new Thread(() -> {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            UVDataEntity entity = new UVDataEntity(currentDate, uvValue);
            database.uvDataDao().insert(entity);
        }).start();
    }
}
