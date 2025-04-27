package com.example.uvmonitor.utils;

import android.bluetooth.BluetoothAdapter;

public class BluetoothUtils {
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }
}
