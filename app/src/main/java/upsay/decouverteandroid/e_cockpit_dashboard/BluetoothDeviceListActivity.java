package upsay.decouverteandroid.e_cockpit_dashboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import android.util.Log;

public class BluetoothDeviceListActivity extends AppCompatActivity {
    private Bluetooth bluetooth = new Bluetooth();

    private ListView deviceListView;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> adapter;

    private static final int REQUEST_ENABLE_BT = 1;

    private Button gotoMainActivity;

    private static final String TAG = "BluetoothDeviceListActi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_list);

        gotoMainActivity = findViewById(R.id.bpBackToMainActivity);

        deviceListView = findViewById(R.id.device_list);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<>();

        if (isEmulator()) {
            createFakeDeviceList();
            setupDeviceList();
        } else {
            // proceed with normal Bluetooth logic
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                setupDeviceList();
            }
        }


        gotoMainActivity.setOnClickListener(v -> {
            Intent intent = new Intent(BluetoothDeviceListActivity.this, MainActivity.class);
            startActivity(intent);
        });


    }


    private boolean isEmulator() {
        // basic check to identify if the app is running on an emulator (e.g., check for known emulator properties)
        String product = Build.PRODUCT;
        return product.contains("sdk") || product.contains("Genymotion");
    }

    // handle the result of the Bluetooth enable request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                setupDeviceList();
            } else {
                Toast.makeText(this, "Bluetooth is required to use this feature", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void setupDeviceList() {
        Log.i(TAG, "Setting up device list..."); // Debug log
        deviceList.clear();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);

        Log.i(TAG, "Device list size: " + deviceList.size());

        // check if in emulator mode, handle click without crashing due to missing Bluetooth
        if (isEmulator()) {
            createFakeDeviceList();

            deviceListView.setOnItemClickListener((parent, view, position, id) -> {
                Log.d(TAG, "Item clicked at position: " + position);
                String deviceInfo = adapter.getItem(position);
                Log.d(TAG, "Item clicked: " + deviceInfo);
                setBluetoothDevice(deviceInfo); // simulate Bluetooth device connection
            });
        } else {
            getPairedDevices();

            deviceListView.setOnItemClickListener((parent, view, position, id) -> {
                Log.d(TAG, "Item clicked at position: " + position);
                String deviceInfo = adapter.getItem(position);
                Log.d(TAG, "Item clicked: " + deviceInfo);
                setBluetoothDevice(deviceInfo); // set the Bluetooth device details
            });
        }
    }


    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                deviceList.add(deviceName + "\n" + deviceAddress);
            }
        } else {
            createFakeDeviceList();
        }
    }


    private void createFakeDeviceList() {
        deviceList.add("Fake Device A\n00:11:22:33:44:55");
        deviceList.add("Fake Device B\n66:77:88:99:AA:BB");
        deviceList.add("Fake Device C\nCC:DD:EE:FF:00:11");
        deviceList.add("Fake Device D\n22:33:44:55:66:77");
        deviceList.add("Fake Device E\n88:99:AA:BB:CC:DD");

        // use the adapter to set the fake devices
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);
    }


    // method to set Bluetooth device details in the Bluetooth class
    private void setBluetoothDevice(String deviceInfo) {
        if (deviceInfo != null && deviceInfo.length() > 17) {
            // The expected format is "Device Name\nMAC_ADDRESS"
            String[] parts = deviceInfo.split("\n");
            if (parts.length == 2) {
                String deviceName = parts[0].trim();
                String macAddress = parts[1].trim();

                bluetooth.setMacAddress(macAddress);
                bluetooth.setDeviceName(deviceName);

                // Show a toast with the connected device details
                Toast.makeText(this, "Connected to " + bluetooth.deviceName + " device", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid device information", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid device information", Toast.LENGTH_SHORT).show();
        }
    }




}
