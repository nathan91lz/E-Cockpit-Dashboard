package upsay.decouverteandroid.e_cockpit_dashboard;

import android.graphics.Color;
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
import androidx.core.content.ContextCompat;

import java.io.IOException;

import android.util.Log;

import de.nitri.gauge.Gauge;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class ECockpitDashboardActivity extends AppCompatActivity {
    private Bluetooth bluetooth;
    private Handler handler;
    private boolean isRequesting = false;

    private Button bpGotoMain;
    private TextView txtRPM;
    private TextView txtFuelLevel;
    private TextView txtAmbientAirTemp;
    private TextView txtEngineOilTemp;
    private TextView txtCoolantTemp;
    private TextView txtIntakeAirTemp;
    private TextView txtTemperature;
    private TextView txtCoolantTemperature;
    public String macAddress = Bluetooth.macAddress;
    public String deviceName = Bluetooth.deviceName;

    private boolean debugView = false; // true to debug

    private int rpmRequestCount = 0;
    private String response;
    private String rpmValue;

    private Gauge gauge;

    private ProgressBar gaugeProgressBar;

    private String rmpExpectedResponse = "41 0C";
    private String fuelLevelExpectedResponse = "41 2F";
    private String ambientAirTempExpectedResponse = "41 46";
    private String engineOilTempExpectedResponse = "41 5C";
    private String coolantTempExpectedResponse = "41 05";
    private String intakeAirTempExpectedResponse = "41 0F";

    private MapView mapView;
    private GoogleMap googleMap;

    private static final String TAG = "ECockpitDashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_cockpit_dashboard);

        Log.i(TAG, "MAC address is :" + macAddress);
        Log.i(TAG, "Device name is :" + deviceName);


        bpGotoMain = findViewById(R.id.bpGotoMain);
        txtRPM = findViewById(R.id.txtRPM);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtAmbientAirTemp = findViewById(R.id.txtAmbientAirTemp);
        txtEngineOilTemp = findViewById(R.id.txtEngineOilTemp);
        txtCoolantTemp = findViewById(R.id.txtCoolantTemp);
        txtIntakeAirTemp = findViewById(R.id.txtIntakeAirTemp);
        txtTemperature = findViewById(R.id.txtTemperature);
        txtCoolantTemperature = findViewById(R.id.txtCoolantTemperature);
        gaugeProgressBar = findViewById(R.id.progressBar);

        bluetooth = new Bluetooth();
        handler = new Handler(Looper.getMainLooper());

        gauge = findViewById((R.id.gauge));
        int faceColor = ContextCompat.getColor(this, R.color.face);
        gauge.setDrawingCacheBackgroundColor(faceColor);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            // set initial location
            LatLng initialLocation = new LatLng(48.8317, 2.3879);
            googleMap.addMarker(new MarkerOptions().position(initialLocation).title("Initial Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10));
        });

        launchTest();


        // DEBBUGIN VEHICLE DATA ;
        setDebugView(false);

        // >>>> debug gauge > incrementation
        //testRPMGauge();
        //testLinearGauge();

        // try to connect to the OBDII device using the MAC address
        // DEGUG UNCOMMENT LINE :
        if (!MainActivity.emulatorMod) {
            if (!macAddress.equals("No connected Bluetooth device found") && !macAddress.equals("Bluetooth not enabled or not supported")) {
                try {
                    bluetooth.connect(macAddress);
                    Toast.makeText(this, "Connected to OBDII device", Toast.LENGTH_SHORT).show(); // message on screen

                    try {
                        bluetooth.initializeConnection();  // send initialization commands to the OBD device
                        Log.i(TAG, "Initialization done");
                    } catch (InterruptedException e) {
                        Toast.makeText(this, "Error initialization", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error initialization");
                        throw new RuntimeException(e);
                    }

                    //requestRPMData();
                    //bluetooth.sendATCommand("010C\r");

                    Log.i(TAG, "AT command loop start");

                    startRequestLoop();

                    //bluetooth.readResponse();

                } catch (IOException e) {
                    e.printStackTrace();
                    txtRPM.setText("Error connecting to device");
                }
            } else {
                txtRPM.setText("Error MAC Address : " + macAddress);  // display error if no device found or Bluetooth issue
            }
        } else {
            Log.d(TAG, "Skipping Bluetooth connection logic as emulatorMod is true");
        }


        // stop RPM requests when bpGotoMain button is pressed and finish the activity
        bpGotoMain.setOnClickListener(v -> {
            stopRequestLoop();
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


    // setup serial comm
    private void setupCommunication() {

        isRequesting = true;

        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                if (isRequesting) {
                    try {

                        if (bluetooth != null) {
                            bluetooth.initializeConnection();  // send the RPM request command

                        } else {
                            txtRPM.setText("Bluetooth object is null");
                        }

                        Log.i(TAG, "Request ongoing...");

                        // schedule the next execution
                        handler.postDelayed(this, 50);
                    } catch (Exception e) {
                        // log the error and display a message on the UI
                        Log.e(TAG, "Error during data request", e);
                        runOnUiThread(() -> txtRPM.setText("Error retrieving data"));

                        // optionally, stop the loop on failure
                        stopRequestLoop();
                    }
                }
            }
        };

        // start the task for the first time
        handler.post(requestTask);
    }

    private void setDebugView(boolean debugView){
        if(debugView){
            txtFuelLevel.setVisibility(View.VISIBLE);
            txtAmbientAirTemp.setVisibility(View.VISIBLE);
            txtEngineOilTemp.setVisibility(View.VISIBLE);
            txtCoolantTemp.setVisibility(View.VISIBLE);
            txtIntakeAirTemp.setVisibility(View.VISIBLE);
            txtRPM.setVisibility(View.VISIBLE);
            bpGotoMain.setVisibility(View.VISIBLE);
        }
        else{
            txtFuelLevel.setVisibility(View.GONE);
            txtAmbientAirTemp.setVisibility(View.GONE);
            txtEngineOilTemp.setVisibility(View.GONE);
            txtCoolantTemp.setVisibility(View.GONE);
            txtIntakeAirTemp.setVisibility(View.GONE);
            txtRPM.setVisibility(View.GONE);
            bpGotoMain.setVisibility(View.GONE);
        }
    }



    // start a loop to request data every 200ms
    private void startRequestLoop() {
        isRequesting = true;

        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                if (isRequesting) {
                    try {
                        if (rpmRequestCount < 4) {
                            // request and process RPM data
                            requestRPMData();
                            Log.i(TAG, "Request ongoing... (RPM)");
                        } else {
                            // call alternate data requests after 4 RPM requests
                            switch (rpmRequestCount) {
                                case 4:
                                    requestCoolantTempData();
                                    Log.i(TAG, "Request ongoing... (Coolant Temperature)");
                                    break;
                                case 5:
                                    requestIntakeAirTempData();
                                    Log.i(TAG, "Request ongoing... (Intake Air Temperature)");
                                    break;
                            }
                        }

                        // increment or reset the RPM counter
                        rpmRequestCount = (rpmRequestCount + 1) % 6; // 4 RPM requests + 2 other data requests

                        // Request and process RPM data
                        //requestRPMData(); //OK
                        //requestCoolantTempData(); //OK
                        //requestIntakeAirTempData(); //OK

                        //Log.i(TAG, "Request ongoing...");

                        // add a delay before the next request
                        handler.postDelayed(this, 300); // between 200 to 300ms
                    } catch (Exception e) {
                        // log the error and display a message on the UI
                        Log.e(TAG, "Error during data request", e);
                        runOnUiThread(() -> txtRPM.setText("Error retrieving data"));

                        // optionally, stop the loop on failure
                        stopRequestLoop();
                    }
                }
            }
        };

        // Start the task for the first time
        handler.post(requestTask);
    }


    // stop the loop for requesting RPM data
    private void stopRequestLoop() {
        isRequesting = false;
        handler.removeCallbacksAndMessages(null);  // Stop all callbacks
    }


    // send the RPM request to the OBD device and update the UI
    private void requestRPMData() {
        try {
            if (bluetooth != null) {
                bluetooth.requestRPM();  // send the RPM request command
                response = bluetooth.waitForPrompt();  // wait for the response until prompt '>' is received

                if (response == null || response.isEmpty()) {
                    txtRPM.setText("ERROR, empty response");
                    return;
                }

                Log.i(TAG, "Response gotten: " + response);
                rpmValue = processRPMResponse(response);  // process the response to get RPM
                txtRPM.setText("RPM: " + rpmValue);  // display the RPM value
            } else {
                txtRPM.setText("Bluetooth object is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtRPM.setText("ERROR");
        }
    }


    private void requestFuelLevelData() {
        try {
            if (bluetooth != null) {
                bluetooth.requestFuelLevel();  // send the Fuel Level request command
                response = bluetooth.readResponse(fuelLevelExpectedResponse);  // read the response from the OBD device

                try {
                    Thread.sleep(200); // adjust the delay as necessary
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Response: " + response);

                if (response == null || response.isEmpty()) {
                    txtFuelLevel.setText("ERROR, empty response");
                    return;
                }

                String fuelLevel = processFuelLevelResponse(response);  // process the response
                txtFuelLevel.setText("Fuel Level: " + fuelLevel);  // display the fuel level

            } else {
                txtFuelLevel.setText("Bluetooth object is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtFuelLevel.setText("ERROR");
        }
    }


    private void requestAmbientAirTempData() {
        try {
            if (bluetooth != null) {
                bluetooth.requestAmbientAirTemp();  // send the Ambient Air Temp request command
                response = bluetooth.readResponse(ambientAirTempExpectedResponse);  // read the response from the OBD device

                try {
                    Thread.sleep(200); // adjust the delay as necessary
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Response: " + response);

                if (response == null || response.isEmpty()) {
                    txtAmbientAirTemp.setText("ERROR, empty response");
                    return;
                }

                String airTemp = processAmbientAirTempResponse(response);  // process the response
                txtAmbientAirTemp.setText("Ambient Air Temp: " + airTemp);  // display the air temperature

            } else {
                txtAmbientAirTemp.setText("Bluetooth object is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtAmbientAirTemp.setText("ERROR");
        }
    }


    private void requestEngineOilTempData() {
        try {
            if (bluetooth != null) {
                bluetooth.requestEngineOilTemp();  // send the Engine Oil Temp request command
                response = bluetooth.readResponse(engineOilTempExpectedResponse);  // read the response from the OBD device

                try {
                    Thread.sleep(200); // adjust the delay as necessary
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Response: " + response);

                if (response == null || response.isEmpty()) {
                    txtEngineOilTemp.setText("ERROR, empty response");
                    return;
                }

                String oilTemp = processEngineOilTempResponse(response);  // process the response
                txtEngineOilTemp.setText("Engine Oil Temp: " + oilTemp);  // display the engine oil temperature

            } else {
                txtEngineOilTemp.setText("Bluetooth object is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtEngineOilTemp.setText("ERROR");
        }
    }


    private void requestCoolantTempData() {
        try {
            if (bluetooth != null) {
                bluetooth.requestCoolantTemp();  // send the Coolant Temp request command
                response = bluetooth.waitForPrompt();

                if (response == null || response.isEmpty()) {
                    txtCoolantTemp.setText("ERROR, empty response");
                    return;
                }

                String coolantTemp = processCoolantTempResponse(response);  // process the response
                txtCoolantTemp.setText(coolantTemp);  // display the coolant temperature

            } else {
                txtCoolantTemp.setText("Bluetooth object is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtCoolantTemp.setText("ERROR");
        }
    }


    private void requestIntakeAirTempData() {
        try {
            if (bluetooth != null) {
                bluetooth.requestIntakeAirTemp();  // Send the Intake Air Temp request command
                response = bluetooth.waitForPrompt();  // Read the response from the OBD device

                if (response == null || response.isEmpty()) {
                    txtIntakeAirTemp.setText("ERROR, empty response");
                    return;
                }

                Log.i(TAG, "Response gotten: " + response);
                String intakeAirTemp = processIntakeAirTempResponse(response);  // Process the response
                txtIntakeAirTemp.setText(intakeAirTemp);  // Display the intake air temperature
            } else {
                txtIntakeAirTemp.setText("Bluetooth object is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            txtIntakeAirTemp.setText("ERROR");
        }
    }


    // process the response to extract RPM
    public String processRPMResponse(String response) {
        int rpm;
        if (response.contains("41 0C")) { // try with only 0C
            try {
                // extract hex values after '41 0C'
                String hexA = response.substring(6, 8);
                String hexB = response.substring(9, 11);

                // convert hex to integers
                int A = Integer.parseInt(hexA, 16);
                int B = Integer.parseInt(hexB, 16);

                // calculate RPM
                rpm = ((A * 256) + B) / 4;
                Log.w(TAG, "RPM value: " + String.valueOf(rpm));

                if (rpm >= 0 && rpm <= 10000) {
                    gauge.moveToValue(rpm);  // smooth
                    //gauge.setValue(Float.parseFloat(rpm/100));
                    gauge.setLowerText(String.valueOf(rpm/100));

                    return String.valueOf(rpm);
                } else {
                    return "Invalid RPM value : Out of range";
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error -1"; // -1 on error
            }
        }
        return "Error response value : '41 0C' not found";
    }



    // NOT SUPPORTED : C1
    public String processFuelLevelResponse(String response) {
        if (response.contains("41 2F")) {
            try {
                // extract hex value after '41 2F'
                String hexA = response.substring(6, 8);

                // convert hex to integer
                int A = Integer.parseInt(hexA, 16);

                // calculate fuel level percentage
                int fuelLevel = (A * 100) / 255;

                Log.w(TAG, "Fuel Level: " + fuelLevel + "%");
                return String.valueOf(fuelLevel) + "%";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error -1"; // return -1 on error
            }
        }
        return "Error response value: '41 2F' not found";
    }


    // NOT SUPPORTED : C1
    public String processAmbientAirTempResponse(String response) {
        if (response.contains("41 46")) {
            try {
                // extract hex value after '41 46'
                String hexA = response.substring(6, 8);

                // convert hex to integer
                int A = Integer.parseInt(hexA, 16);

                // calculate ambient air temperature in Celsius
                int temperature = A - 40;

                Log.w(TAG, "Ambient Air Temperature: " + temperature + "°C");
                return String.valueOf(temperature) + "°C";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error -1"; // return -1 on error
            }
        }
        return "Error response value: '41 46' not found";
    }


    // NOT SUPPORTED : C1
    public String processEngineOilTempResponse(String response) {
        if (response.contains("41 5C")) {
            try {
                // extract hex value after '41 5C'
                String hexA = response.substring(6, 8);

                // convert hex to integer
                int A = Integer.parseInt(hexA, 16);

                // calculate engine oil temperature in Celsius
                int temperature = A - 40;

                Log.w(TAG, "Engine Oil Temperature: " + temperature + "°C");
                return String.valueOf(temperature) + "°C";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error -1"; // return -1 on error
            }
        }
        return "Error response value: '41 5C' not found";
    }


    public String processCoolantTempResponse(String response) {
        int coolantTemp;

        Log.i(TAG, "Response in function " + response);

        // Check if the response contains '41 05'
        if (response.contains("41 05")) {
            try {
                // Extract the hex value after '41 05'
                String hexTemp = response.substring(6, 8); // Get the hex value

                Log.i(TAG, "Coolant Temperature hex: " + hexTemp);

                // Convert hex to integer
                coolantTemp = Integer.parseInt(hexTemp, 16); // Convert to int

                Log.w(TAG, "Coolant Temperature value: " + coolantTemp);

                if (coolantTemp >= -40 && coolantTemp <= 215) {
                    txtCoolantTemperature.setText(coolantTemp + " °C");
                    gaugeProgressBar.setProgress(coolantTemp);
                    return "Coolant Temperature: " + coolantTemp + " °C";
                } else {
                    return "Invalid Coolant Temp value: Out of range";
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error -1"; // Return -1 on error
            }
        }
        return "Error response value: '41 05' not found"; // Return error if '41 05' is not found
    }



    public String processIntakeAirTempResponse(String response) {
        // Normalize the response by trimming and removing unnecessary characters
        response = response.trim().replaceAll(">", "").replaceAll("<", "");

        // Check if the response contains the expected prefix for intake air temperature
        if (response.contains("41 0F")) {
            try {
                // Split the response by spaces
                String[] parts = response.split("\\s+");
                int index = -1;

                // Search for the "41 0F" prefix in the response
                for (int i = 0; i < parts.length; i++) {
                    if (i + 1 < parts.length && "41".equals(parts[i]) && "0F".equals(parts[i + 1])) {
                        index = i + 2; // The hex value should be after "41 0F"
                        break;
                    }
                }

                // Ensure index is valid and within the bounds of the response
                if (index != -1 && index < parts.length) {
                    String hexValue = parts[index];

                    // Convert hex to an integer
                    int temperatureHex = Integer.parseInt(hexValue, 16);

                    // Calculate temperature
                    int temperature = temperatureHex - 40;
                    Log.w(TAG, "Intake Air Temperature: " + temperature + "°C");

                    // Display the temperature correctly
                    if (temperature >= -40 && temperature <= 215) {
                        txtTemperature.setText(temperature + " °C");
                        return "Intake Air Temperature: " + temperature + " °C";
                    } else {
                        return "Invalid Intake Air Temp value: Out of range";
                    }
                } else {
                    return "Error response: '41 0F' found, but no temperature data";
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid hex value in response: " + response, e);
                return "Error - Invalid hex value";
            } catch (Exception e) {
                Log.e(TAG, "An unexpected error occurred: " + e.getMessage(), e);
                return "Error -1"; // -1 on general error
            }
        }
        return "Error response: '41 0F' not found";
    }


    private void testLinearGauge() {
        // create a new thread for simulating temperature changes
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int temp = 50; temp <= 120; temp += 10) {
                    // Capture the temp value for use inside the inner Runnable
                    final int finalTemp = temp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update your progress bar with the current temperature value
                            Log.w(TAG, "Setting progress to " + finalTemp);
                            txtCoolantTemperature.setText(finalTemp + " °C");  // Update text

                            gaugeProgressBar.setProgress(finalTemp);
                        }
                    });

                    // Sleep for 500 milliseconds to simulate a delay
                    try {
                        Thread.sleep(500); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private void testRPMGauge() {
        // create a new thread for simulating temperature changes
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int rpm = 0; rpm <= 8000; rpm += 500) {
                    // Capture the temp value for use inside the inner Runnable
                    final int finalRPM = rpm;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update your progress bar with the current temperature value
                            Log.w(TAG, "Setting progress to " + finalRPM);

                            gauge.moveToValue(finalRPM);  // smooth
                            //gauge.setValue(Float.parseFloat(rpm));
                            gauge.setLowerText(String.valueOf(finalRPM));
                        }
                    });

                    // Sleep for 500 milliseconds to simulate a delay
                    try {
                        Thread.sleep(500); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }



    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void launchTest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int rpm = 0; rpm <= 8000; rpm += 250) {
                    // capture the temp value for use inside the inner Runnable
                    final int finalRPM = rpm;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.w(TAG, "Setting progress to " + finalRPM);

                            gauge.moveToValue(finalRPM/100);  // smooth
                            //gauge.setValue(Float.parseFloat(rpm));
                            gauge.setLowerText(String.valueOf(finalRPM));
                        }
                    });

                    // sleep to simulate a delay
                    try {
                        Thread.sleep(150); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int temp = 50; temp <= 130; temp += 2) {
                    // Capture the temp value for use inside the inner Runnable
                    final int finalTemp = temp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update your progress bar with the current temperature value
                            Log.w(TAG, "Setting progress to " + finalTemp);
                            txtCoolantTemperature.setText(finalTemp + " °C");  // Update text

                            gaugeProgressBar.setProgress(finalTemp);
                        }
                    });

                    // Sleep for 500 milliseconds to simulate a delay
                    try {
                        Thread.sleep(100); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();



    }




}

