package upsay.decouverteandroid.e_cockpit_dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import android.util.Log;

import de.nitri.gauge.Gauge;


public class ECockpitDashboardActivity extends AppCompatActivity {
    private Bluetooth bluetooth;
    private Handler handler;
    private boolean isRequestingRPM = false;
    private Button bpGotoMain;
    private TextView txtRPM;
    private TextView txtRPMDebug;
    public String macAddress = Bluetooth.macAddress;
    public String deviceName = Bluetooth.deviceName;

    private ProgressBar rpmGauge;

    final Gauge gauge = (Gauge) findViewById(R.id.gauge);


    private static final String TAG = "ECockpitDashboard";

    @Override



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_cockpit_dashboard);

        Log.i(TAG, "MAC address is :" + macAddress);
        Log.i(TAG, "Device name is :" + deviceName);

        gauge.moveToValue(800);

        HandlerThread thread = new HandlerThread("GaugeDemoThread");
        thread.start();
        Handler handler = new Handler(thread.getLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gauge.moveToValue(300);
            }
        }, 2800);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gauge.moveToValue(550);
            }
        }, 5600);

        bpGotoMain = findViewById(R.id.bpGotoMain);
        txtRPM = findViewById(R.id.txtRPM);

        bluetooth = new Bluetooth();
        handler = new Handler(Looper.getMainLooper());

        rpmGauge = findViewById(R.id.rpmLinearGauge);

        rpmGauge.setProgress(1000);



        // try to connect to the OBDII device using the MAC address
        // DEGUG UNCOMMENT LINE :
        if (!macAddress.equals("No connected Bluetooth device found") && !macAddress.equals("Bluetooth not enabled or not supported")) {
            try {
                bluetooth.connect(macAddress);
                //bluetooth.initializeConnection();  // send initialization commands to the OBD device
                Toast.makeText(this, "Connected to OBDII device", Toast.LENGTH_SHORT).show(); // message on screen

                //startRPMRequestLoop();

                //requestRPMData();
                //bluetooth.sendATCommand("010C\r");
                Log.i(TAG, "AT command sent");

                startRPMRequestLoop();


                //bluetooth.readResponse();

            } catch (IOException e) {
                e.printStackTrace();
                txtRPM.setText("Error connecting to device");
            }
        } else {
            txtRPM.setText("Error MAC Address : " + macAddress);  // display error if no device found or Bluetooth issue
        }


        // stop RPM requests when bpGotoMain button is pressed and finish the activity
        bpGotoMain.setOnClickListener(v -> {
            stopRPMRequestLoop();
            if (bluetooth != null) {
                try {
                    bluetooth.disconnect();
                    Toast.makeText(this, "OBDII device disconnected", Toast.LENGTH_SHORT).show();
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
                    try {
                        // Request and process RPM data
                        requestRPMData();
                        Log.i(TAG, "Request ongoing...");

                        // Schedule the next execution after 1 second
                        handler.postDelayed(this, 1000);
                    } catch (Exception e) {
                        // Log the error and display a message on the UI
                        Log.e(TAG, "Error during RPM request", e);
                        runOnUiThread(() -> txtRPM.setText("Error retrieving RPM"));

                        // Optionally, stop the loop on failure
                        stopRPMRequestLoop();
                    }
                }
            }
        };

        // Start the task for the first time
        handler.post(rpmRequestTask);
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

                try {
                    Thread.sleep(200); // adjust the delay as necessary (200ms here)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //txtRPMDebug.setText(response);
                Log.i(TAG, "Response : " + response);

                if (response == null || response.isEmpty()) {
                    txtRPM.setText("ERROR, empty reponse");
                    return;
                }

                int rpm = bluetooth.processRPMResponse(response);  // process the response to get RPM
                if (rpm == -1) {
                    txtRPM.setText("ERROR RPM value");
                    Log.w(TAG, "RPM value :" + String.valueOf(rpm));
                } else {
                    txtRPM.setText("RPM : " + String.valueOf(rpm));  // display the RPM value
                    rpmGauge.setProgress(rpm);
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

