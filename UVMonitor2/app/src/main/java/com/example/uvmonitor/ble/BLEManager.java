package com.example.uvmonitor.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEManager {
    private static final String TAG = "BLEManager";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;
    private MutableLiveData<List<BluetoothDevice>> foundDevices = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private MutableLiveData<String> uvData = new MutableLiveData<>("N/A");

    private static final UUID UV_SERVICE_UUID = UUID.fromString("ed69bccc-61bc-433c-b670-dc8bf3eaafa6");
    private static final UUID UV_CHARACTERISTIC_UUID = UUID.fromString("673262db-119e-49ca-aae8-f517404b0f4f");

    public BLEManager(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void startScan() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bleScanner != null) {
                foundDevices.getValue().clear();
                bleScanner.startScan(scanCallback);
                Log.d(TAG, "Started BLE Scan");
            }
        }
    }

    public void stopScan() {
        if (bleScanner != null) {
            bleScanner.stopScan(scanCallback);
            Log.d(TAG, "Stopped BLE Scan");
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null && device.getName() != null && device.getName().toLowerCase().contains("esp32")) {
                List<BluetoothDevice> currentList = foundDevices.getValue();
                if (!currentList.contains(device)) {
                    currentList.add(device);
                    foundDevices.postValue(currentList);
                    Log.d(TAG, "Device Found: " + device.getName());
                }
            }
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            isConnected.postValue(false);
            Log.d(TAG, "Disconnected from GATT server");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server");
                isConnected.postValue(true);
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server");
                isConnected.postValue(false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService uvService = gatt.getService(UV_SERVICE_UUID);
                if (uvService != null) {
                    BluetoothGattCharacteristic uvCharacteristic = uvService.getCharacteristic(UV_CHARACTERISTIC_UUID);
                    if (uvCharacteristic != null) {
                        gatt.readCharacteristic(uvCharacteristic);
                        gatt.setCharacteristicNotification(uvCharacteristic, true);
                        Log.d(TAG, "UV service and characteristic found, reading...");
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic.getUuid().equals(UV_CHARACTERISTIC_UUID)) {
                int uvValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                uvData.postValue(String.valueOf(uvValue));
                Log.d(TAG, "UV Value Read: " + uvValue);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(UV_CHARACTERISTIC_UUID)) {
                int uvValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                uvData.postValue(String.valueOf(uvValue));
                Log.d(TAG, "UV Value Updated: " + uvValue);
            }
        }
    };

    public MutableLiveData<List<BluetoothDevice>> getFoundDevices() {
        return foundDevices;
    }

    public MutableLiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public MutableLiveData<String> getUVData() {
        return uvData;
    }
}
