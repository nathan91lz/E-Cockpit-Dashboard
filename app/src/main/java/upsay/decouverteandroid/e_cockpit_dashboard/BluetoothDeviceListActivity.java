package upsay.decouverteandroid.e_cockpit_dashboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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

public class BluetoothDeviceListActivity extends AppCompatActivity {

    private ListView deviceListView;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> adapter;

    private Button gotoMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_list);

        gotoMainActivity = findViewById(R.id.bpBackToMainActivity);

        deviceListView = findViewById(R.id.device_list);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<>();

        // get paired devices
        getPairedDevices();

        // set up the adapter for the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);

        // set click listener for the ListView items
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                String deviceInfo = adapter.getItem(position);
                String macAddress = deviceInfo.substring(deviceInfo.length() - 17);
                Toast.makeText(BluetoothDeviceListActivity.this, "Selected Device: " + deviceInfo + "\nMAC Address: " + macAddress, Toast.LENGTH_SHORT).show();
            }
        });


        gotoMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothDeviceListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    private void getPairedDevices() {
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    }
}
