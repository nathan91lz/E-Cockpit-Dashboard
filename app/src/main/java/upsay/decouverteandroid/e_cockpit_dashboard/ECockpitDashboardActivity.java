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


public class ECockpitDashboardActivity extends AppCompatActivity {
    private Bluetooth bluetooth;
    private Handler handler;
    private boolean isRequestingRPM = false;
    private Button bpGotoMain;
    private TextView txtRPM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_cockpit_dashboard);

        bpGotoMain = findViewById(R.id.bpGotoMain);
        txtRPM = findViewById(R.id.txtRPM);

        bluetooth = new Bluetooth();
        handler = new Handler(Looper.getMainLooper());

        String macAddress = Bluetooth.getConnectedDeviceMac();
        String deviceName = Bluetooth.getConnectedDeviceName();

        // try to connect to the OBDII device using the MAC address
        if (deviceName.equals("OBDII")) {
            if (!macAddress.equals("No connected Bluetooth device found") && !macAddress.equals("Bluetooth not enabled or not supported")) {
                try {
                    bluetooth.connect(macAddress);
                    bluetooth.initializeConnection();  // send initialization commands to the OBD device
                    Toast.makeText(this, "Connected to OBDII device", Toast.LENGTH_SHORT).show();

                    // start requesting RPM data in a loop
                    startRPMRequestLoop();

                } catch (IOException e) {
                    e.printStackTrace();
                    txtRPM.setText("Error connecting to device");
                }
            } else {
                txtRPM.setText("Error MAC Address : " + macAddress);  // Display error if no device found or Bluetooth issue
            }
        } else {
            txtRPM.setText("Connected device is not OBDII");  // Display message if the device is not "OBDII"
        }

        // stop RPM requests when bpGotoMain button is pressed and finish the activity
        bpGotoMain.setOnClickListener(v -> {
            stopRPMRequestLoop();
            try {
                bluetooth.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
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
            bluetooth.requestRPM();  // Send the RPM request command
            String response = bluetooth.readResponse();  // Read the response from the OBD device

            if (response == null || response.isEmpty()) {
                txtRPM.setText("ERROR");
                return;
            }

            int rpm = bluetooth.processRPMResponse(response);  // Process the response to get RPM
            if (rpm == -1) {
                txtRPM.setText("ERROR");
            } else {
                txtRPM.setText(String.valueOf(rpm));  // Display the RPM value
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtRPM.setText("ERROR");
        }
    }



}

