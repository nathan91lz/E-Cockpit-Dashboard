package upsay.decouverteandroid.e_cockpit_dashboard;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;


public class OBDII {
    private Bluetooth bluetooth;
    private ECockpitDashboardActivity dashboard;

    private String rmpExpectedResponse = "41 0C";
    private String fuelLevelExpectedResponse = "41 2F";
    private String ambientAirTempExpectedResponse = "41 46";
    private String engineOilTempExpectedResponse = "41 5C";
    private String coolantTempExpectedResponse = "41 05";
    private String intakeAirTempExpectedResponse = "41 0F";

    private static final String atzCommand = "ATZ\n";
    private static final String ate0Command = "ATE0\n";
    private static final String atl0Command = "ATL0\n";
    private static final String rpmCommand = "010C\n";
    private static final String fuelLevelCommand = "012F\n";
    private static final String ambientAirTempCommand = "0146\n";
    private static final String engineOilTempCommand = "015C\n";
    private static final String coolantTempCommand = "0105\n";
    private static final String intakeAirTempCommand = "010F\n";

    private String response;

    private static final String TAG = "OBDII";

    public OBDII(Bluetooth bluetooth, ECockpitDashboardActivity dashboard) {
        this.bluetooth = bluetooth;
        this.dashboard = dashboard;
    }


    // send AT command to initialize the OBD-II connection
    public void initializeConnection() throws IOException, InterruptedException {
        Log.i(TAG, "Inititialization start");
        Bluetooth.sendATCommand(atzCommand);
        Bluetooth.waitForPrompt(); // Wait until '>' or appropriate response

        Bluetooth.sendATCommand(ate0Command);
        Bluetooth.waitForPrompt();

        Bluetooth.sendATCommand(atl0Command);
        Bluetooth.waitForPrompt();
        Log.i(TAG, "Done");
    }


    // send the RPM request to the OBD device and update the UI
    public String rpmRequest(){
        try {
            if (bluetooth != null) {
                Log.i(TAG, "HERE");
                Bluetooth.sendATCommand(rpmCommand);  // send the RPM request command
                response = Bluetooth.waitForPrompt();  // wait for the response until prompt '>' is received
                Log.d(TAG, "Response: " + response);

                if (response == null || response.isEmpty()) {
                    //txtRPM.setText("ERROR, empty response");
                    return "Response null";
                }
                processRPMResponse(response);  // process the response to get RPM
                //txtRPM.setText("RPM: " + rpmValue);  // display the RPM value
            } else {
                Log.e(TAG, "ERROR Response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //txtRPM.setText("ERROR");
        }
        return response;
    }


    public String requestCoolantTempData() {
        try {
            if (bluetooth != null) {
                Bluetooth.sendATCommand(coolantTempCommand);  // send the Coolant Temp request command
                response = bluetooth.waitForPrompt();

                if (response == null || response.isEmpty()) {
                    return "Response null";
                }

                processCoolantTempResponse(response);

            } else {
                Log.e(TAG, "ERROR Response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    public String requestIntakeAirTempData() {
        try {
            if (bluetooth != null) {
                Bluetooth.sendATCommand(intakeAirTempCommand);  // Send the Intake Air Temp request command
                response = Bluetooth.waitForPrompt();  // Read the response from the OBD device

                if (response == null || response.isEmpty()) {
                    return "Response null";
                }

                Log.i(TAG, "Response gotten: " + response);
                processIntakeAirTempResponse(response);  // Process the response
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
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
                Log.i(TAG, "RPM value: " + String.valueOf(rpm));

                if (rpm >= 0 && rpm <= 10000) {
                    dashboard.setRPM(rpm);

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

                Log.i(TAG, "Coolant Temperature value: " + coolantTemp);

                if (coolantTemp >= -40 && coolantTemp <= 215) {
                    dashboard.setCoolantTemp(coolantTemp);

                    return String.valueOf(coolantTemp);
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
        int intakeAirTemp;

        Log.i(TAG, "Response in function: " + response);

        // check if the response contains '41 0F'
        if (response.contains("41 0F")) {
            try {
                // extract the hex value after '41 0F'
                String hexTemp = response.substring(6, 8); // get the hex value after '41 0F'

                Log.i(TAG, "Intake Air Temperature hex: " + hexTemp);

                // convert hex to integer
                intakeAirTemp = Integer.parseInt(hexTemp, 16); // Convert to int

                // calculate the intake air temperature in degrees
                intakeAirTemp -= 40;

                Log.i(TAG, "Intake Air Temperature value: " + intakeAirTemp + "°C");

                if (intakeAirTemp >= -40 && intakeAirTemp <= 215) {
                    dashboard.setIntakeAirTemp(intakeAirTemp); 
                    return String.valueOf(intakeAirTemp);
                } else {
                    return "Invalid Intake Air Temp value: Out of range";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error -1"; // Return -1 on error
            }
        }
        return "Error response value: '41 0F' not found"; // Return error if '41 0F' is not found
    }




}
