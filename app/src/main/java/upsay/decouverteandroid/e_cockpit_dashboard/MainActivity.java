package upsay.decouverteandroid.e_cockpit_dashboard;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


// used for wainting second
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {
    private Button gotoECockpitFragment;
    public String macAddress = Bluetooth.macAddress;
    public String deviceName = Bluetooth.deviceName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imgBtIndicator = findViewById(R.id.imgBtIndicator);
        Button refreshButton = findViewById(R.id.bpRefresh);
        TextView txtEtatBt = findViewById(R.id.txtEtatBt);
        TextView txtAddressMac = findViewById(R.id.txtAddressMac);
        TextView txtAddressMacOwn = findViewById(R.id.txtAddressMacOwn);
        gotoECockpitFragment = findViewById(R.id.bpGotoCockpit);


        // BLUETOOTH EMULATOR :
        boolean isEmulator = Build.FINGERPRINT.contains("generic");

        if (isEmulator) {
            // Simulate a Bluetooth connection and data
            Log.d("Bluetooth", "Running on an emulator, simulating Bluetooth connection");
            String mockMacAddress = "00:11:22:33:44:55";
            String mockDeviceName = "MockedOBDII";

            txtEtatBt.setText("Connected (Emulator)");
            txtAddressMac.setText("MAC Address: " + mockMacAddress + "\nDevice Name: " + mockDeviceName);

            imgBtIndicator.setImageResource(R.drawable.green_circle);
            gotoECockpitFragment.setVisibility(View.VISIBLE);

            // used to bypass main fragment
            Intent intent = new Intent(MainActivity.this, ECockpitDashboardActivity.class);
            startActivity(intent);

        } else {
            // If not on an emulator, check Bluetooth state
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // Check BT device and get Mac address
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshButton.setText("Searching ...");

                    // Delay the MAC address check and UI update by 1 second
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            String macAddress = Bluetooth.getConnectedDeviceMac();
//                            String deviceName = Bluetooth.getConnectedDeviceName();
                            // USED WITH ARDUINO BT FAKE
                            //String macAddress = Bluetooth.getConnectedDeviceMacFromMacAddress(Bluetooth.macAddressFake);
                            //String deviceName = Bluetooth.getConnectedDeviceNameFromMacAddress(Bluetooth.macAddressFake);


                            if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
                                // Optional: You could reset the button text back to default here
                                refreshButton.setText("Not found");
                                imgBtIndicator.setImageResource(R.drawable.red_circle);
                                txtEtatBt.setText("Not paired");
                                txtAddressMac.setText("MAC Address: ");
                                refreshButton.setText("Refresh");

                                // Hide the button if no device is connected
                                gotoECockpitFragment.setVisibility(View.GONE);
                            } else {
                                imgBtIndicator.setImageResource(R.drawable.green_circle);
                                txtEtatBt.setText("Paired");
                                txtAddressMac.setText("MAC Address: " + macAddress + "\nDevice Name: " + deviceName);

                                refreshButton.setText("Found");

                                // Show the button if a device is connected
                                gotoECockpitFragment.setVisibility(View.VISIBLE);
                            }

                            //String ownMacAddress = Bluetooth.getOwnMacAddres();
                            //txtAddressMacOwn.setText("Your Bluetooth MAC Address: " + ownMacAddress);

                            refreshButton.setText("Refresh");
                        }
                    }, 200);  // delay of 1 second
                }
            });
        }



        // Setup fragment page: e-cockpit
        gotoECockpitFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ECockpitDashboardActivity.class);
                startActivity(intent);
            }
        });

        // Initial check to set the visibility of the button on activity start
        checkDeviceConnection();
    }

    private void checkDeviceConnection() {
        String macAddress = Bluetooth.getConnectedDeviceMac();
        if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
            gotoECockpitFragment.setVisibility(View.GONE);
        } else {
            gotoECockpitFragment.setVisibility(View.VISIBLE);
        }
    }







}