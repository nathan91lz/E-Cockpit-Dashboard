package upsay.decouverteandroid.e_cockpit_dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import android.util.Log;


public class ECockpitDashboardActivity extends AppCompatActivity {
    private Bluetooth bluetooth;
    private Handler handler;
    private boolean isRequestingRPM = false;
    private Button bpGotoMain;
    private TextView txtRPM;
    public String macAddress = Bluetooth.macAddress;
    public String deviceName = Bluetooth.deviceName;

    private static final String TAG = "ECockpitDashboard";

    @Override

//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_e_cockpit_dashboard);
//
//        setContentView(R.layout.activity_e_cockpit_dashboard);
//
//        Button cancelButton = findViewById(R.id.bpGotoMain);
//        cancelButton.setOnClickListener(v -> finish());
//
//    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_cockpit_dashboard);

        bpGotoMain = findViewById(R.id.bpGotoMain);
        txtRPM = findViewById(R.id.txtRPM);

        bluetooth = new Bluetooth();
        handler = new Handler(Looper.getMainLooper());

        Log.i(TAG, "MAC address is :" + macAddress);
        Log.i(TAG, "Device name is :" + deviceName);


        // try to connect to the OBDII device using the MAC address
        //if (deviceName.equals("OBDII")) {
        if (deviceName != null && deviceName.equals("MockedOBDII")) { // UESED WITHOUT ANDROID TAB
            if (!macAddress.equals("No connected Bluetooth device found") && !macAddress.equals("Bluetooth not enabled or not supported")) {
                try {
                    bluetooth.connect(macAddress);
                    bluetooth.initializeConnection();  // send initialization commands to the OBD device
                    Toast.makeText(this, "Connected to OBDII device", Toast.LENGTH_SHORT).show();

                    // start requesting RPM data in a loop
                    //startRPMRequestLoop();

                    bluetooth.sendATCommand("010C\r");
                    //bluetooth.readResponse();

                } catch (IOException e) {
                    e.printStackTrace();
                    txtRPM.setText("Error connecting to device");
                }
            } else {
                txtRPM.setText("Error MAC Address : " + macAddress);  // Display error if no device found or Bluetooth issue
            }
        } else {
            txtRPM.setText("Connected device is not OBDII");  // display message if the device is not "OBDII"
        }

        // stop RPM requests when bpGotoMain button is pressed and finish the activity
        bpGotoMain.setOnClickListener(v -> {
            stopRPMRequestLoop();
            if (bluetooth != null) {
                try {
                    bluetooth.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            finish();  // close the activity
        });
    }

    // start a loop to request RPM data every second
    private void startRPMRequestLoop() {
        isRequestingRPM = true;
        Runnable rpmRequestTask = new Runnable() {
            @Override
            public void run() {
                if (isRequestingRPM) {
                    requestRPMData();  // Request and display RPM
                    handler.postDelayed(this, 1000);  // repeat every 1 second
                }
            }
        };
        handler.post(rpmRequestTask);  // start the task
    }

    // stop the loop for requesting RPM data
    private void stopRPMRequestLoop() {
        isRequestingRPM = false;
        handler.removeCallbacksAndMessages(null);  // Stop all callbacks
    }

    // send the RPM request to the OBD device and update the UI
    private void requestRPMData() {
        try {
            if (bluetooth != null) {
                bluetooth.requestRPM();  // send the RPM request command
                String response = bluetooth.readResponse();  // read the response from the OBD device

                if (response == null || response.isEmpty()) {
                    txtRPM.setText("ERROR");
                    return;
                }

                int rpm = bluetooth.processRPMResponse(response);  // process the response to get RPM
                if (rpm == -1) {
                    txtRPM.setText("ERROR");
                } else {
                    txtRPM.setText(String.valueOf(rpm));  // display the RPM value
                }
            } else {
                txtRPM.setText("Bluetooth object is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtRPM.setText("ERROR");
        }
    }





}

