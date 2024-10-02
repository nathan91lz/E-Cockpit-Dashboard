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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import android.util.Log;


import de.nitri.gauge.Gauge;


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
    public String macAddress = Bluetooth.macAddress;
    public String deviceName = Bluetooth.deviceName;

    private int rpmRequestCount = 0;
    private String response;
    private String rpmValue;

    private Gauge gauge;

    private String rmpExpectedResponse = "41 0C";
    private String fuelLevelExpectedResponse = "41 2F";
    private String ambientAirTempExpectedResponse = "41 46";
    private String engineOilTempExpectedResponse = "41 5C";
    private String coolantTempExpectedResponse = "41 05";
    private String intakeAirTempExpectedResponse = "41 0F";



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

        bluetooth = new Bluetooth();
        handler = new Handler(Looper.getMainLooper());



        gauge = findViewById((R.id.gauge));
        int faceColor = ContextCompat.getColor(this, R.color.face);
        gauge.setDrawingCacheBackgroundColor(faceColor);


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

                startRequestLoop();


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





    // start a loop to request data every 200ms
    private void startRequestLoop() {
        isRequesting = true;

        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                if (isRequesting) {
                    try {
                        /*
                        if (rpmRequestCount < 4) {
                            // request and process RPM data
                            requestRPMData();
                            Log.i(TAG, "Request ongoing... (RPM)");
                        } else {
                            // call alternate data requests after 4 RPM requests
                            switch (rpmRequestCount) {
                                case 4:
                                    requestFuelLevelData();
                                    Log.i(TAG, "Request ongoing... (Fuel Level)");
                                    break;
                                case 5:
                                    requestAmbientAirTempData();
                                    Log.i(TAG, "Request ongoing... (Ambient Air Temp)");
                                    break;
                                case 6:
                                    requestEngineOilTempData();
                                    Log.i(TAG, "Request ongoing... (Engine Oil Temp)");
                                    break;
                                case 7:
                                    requestCoolantTempData();
                                    Log.i(TAG, "Request ongoing... (Coolant Temp)");
                                    break;
                                case 8:
                                    requestIntakeAirTempData();
                                    Log.i(TAG, "Request ongoing... (Coolant Temp)");
                                    break;
                            }
                        }

                        // increment or reset the RPM counter
                        rpmRequestCount = (rpmRequestCount + 1) % 9; // 4 RPM requests + 4 other data requests
                        */

                        // Request and process RPM data
                        requestRPMData();
                        //requestCoolantTempData();
                        //requestIntakeAirTempData();
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
                response = bluetooth.readResponse(rmpExpectedResponse);  // read the response from the OBD device

                try {
                    Thread.sleep(200); // adjust the delay as necessary : 200 ok
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //txtRPMDebug.setText(response);
                Log.i(TAG, "Response : " + response);

                if (response == null || response.isEmpty()) {
                    txtRPM.setText("ERROR, empty reponse");
                    return;
                }

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
                response = bluetooth.readResponse(coolantTempExpectedResponse);  // read the response from the OBD device

                try {
                    Thread.sleep(200); // adjust the delay as necessary
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Response: " + response);

                if (response == null || response.isEmpty()) {
                    txtCoolantTemp.setText("ERROR, empty response");
                    return;
                }

                String coolantTemp = processCoolantTempResponse(response);  // process the response
                txtCoolantTemp.setText("Coolant Temp: " + coolantTemp);  // display the coolant temperature

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
                String response = bluetooth.readResponse(intakeAirTempExpectedResponse);  // Read the response from the OBD device

                try {
                    Thread.sleep(200); // Adjust the delay as necessary
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Response: " + response);

                if (response == null || response.isEmpty()) {
                    txtIntakeAirTemp.setText("ERROR, empty response");
                    return;
                }

                String intakeAirTemp = processIntakeAirTempResponse(response);  // Process the response
                txtIntakeAirTemp.setText("Intake Air Temp: " + intakeAirTemp);  // Display the intake air temperature

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
        if (response.contains("41 0C")) {
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
                    //gauge.setValue(Float.parseFloat(rpm));
                    gauge.setLowerText(String.valueOf(rpm));

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
        int temperature;
        if (response.contains("41 05")) {
            try {
                // split the response by spaces to handle variable length formatting
                String[] parts = response.split(" ");
                int index = -1;

                // Search for the "41 05" prefix in the response
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("41") && i + 1 < parts.length && parts[i + 1].equals("05")) {
                        index = i + 2; // The hex value should be after "41 05"
                        break;
                    }
                }

                // Ensure index is valid and within the bounds of the response
                if (index != -1 && index < parts.length) {
                    String hexA = parts[index];

                    // Convert hex to an integer
                    int A = Integer.parseInt(hexA, 16);

                    // Calculate temperature
                    temperature = A - 40;

                    Log.w(TAG, "Coolant Temperature: " + temperature + "°C");

                    if (temperature >= -40 && temperature <= 215) {
                        return temperature + " °C";
                    } else {
                        return "Invalid Intake Air Temp value: Out of range";
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error -1"; // -1 on error
            }
        }
        return "Error response value : '41 05' not found";
    }




    // process the response to extract Intake Air Temperature
    public String processIntakeAirTempResponse(String response) {
        if (response.contains("41 0F")) {  // PID 0F is for Intake Air Temperature
            try {
                // extract hex value after '41 0F'
                String hexA = response.substring(response.indexOf("41 0F") + 5, response.indexOf("41 0F") + 7).trim();

                // convert hex to integer
                int A = Integer.parseInt(hexA, 16);

                // calculate temperature
                int temperature = A - 40;
                Log.w(TAG, "Intake Air Temperature: " + temperature + " °C");

                // check if temperature is within a reasonable range
                if (temperature >= -40 && temperature <= 215) {
                    return temperature + " °C";
                } else {
                    return "Invalid Intake Air Temp value: Out of range";
                }

            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Response parsing error: " + e.getMessage());
                return "Error - Invalid response format"; // more specific error message
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing hex value: " + e.getMessage());
                return "Error - Unable to convert hex to number"; // more specific error message
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error: " + e.getMessage());
                return "Error -1"; // general error
            }
        }
        return "Error response: '41 0F' not found";
    }




}

