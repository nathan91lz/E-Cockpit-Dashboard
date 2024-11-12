package upsay.decouverteandroid.e_cockpit_dashboard;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;


public class OBDII {
    private Bluetooth bluetooth;
    private Handler handler;
    private boolean isRequesting = false;

    private String rmpExpectedResponse = "41 0C";
    private String fuelLevelExpectedResponse = "41 2F";
    private String ambientAirTempExpectedResponse = "41 46";
    private String engineOilTempExpectedResponse = "41 5C";
    private String coolantTempExpectedResponse = "41 05";
    private String intakeAirTempExpectedResponse = "41 0F";

    private String atzCommand = "ATZ\n";
    private String ate0Command = "ATE0\n";
    private String atl0Command = "ATL0\n";
    private String rpmCommand = "010C\n";
    private String fuelLevelCommand = "012F\n";
    private String ambientAirTempCommand = "0146\n";
    private String engineOilTempCommand = "015C\n";
    private String coolantTempCommand = "0105\n";
    private String intakeAirTempCommand = "010F\n";

    private String response;

    private static final String TAG = "OBDII";




    // send AT command to initialize the OBD-II connection
    public void initializeConnection() throws IOException, InterruptedException {
        Bluetooth.sendATCommand(atzCommand);
        Bluetooth.waitForPrompt(); // Wait until '>' or appropriate response

        Bluetooth.sendATCommand(ate0Command);
        Bluetooth.waitForPrompt();

        Bluetooth.sendATCommand(atl0Command);
        Bluetooth.waitForPrompt();
    }



    public static void sendATCommand(String command) throws IOException {
        Bluetooth.outputStream.write(command.getBytes());
    }

    // read response : waiting for the response to the command until the prompt '>' is received and get it
    public static String waitForPrompt() throws IOException {
        byte[] buffer = new byte[64];
        StringBuilder responseBuilder = new StringBuilder();

        while (true) {
            int bytes = Bluetooth.inputStream.read(buffer);  // read from the input stream
            if (bytes != -1) {
                String part = new String(buffer, 0, bytes).trim();
                responseBuilder.append(part);

                // exit the loop when the prompt '>' is found, meaning OBD-II is ready
                if (responseBuilder.toString().endsWith(">")) {
                    break;
                }
            }
        }
        Log.i(TAG, "Brut response received: '" + responseBuilder.toString() + "'");
        String cleanedResponse = responseBuilder.toString()
                .replace(">", "")       // Remove the prompt '>'
                .replace("\r", "")      // Remove carriage return
                .replace("\n", "")      // Remove line feed
                .trim();                // Trim any leading/trailing spaces
        Log.i(TAG, "Cleaned response received: '" + cleanedResponse + "'");
        return cleanedResponse;
    }



    // setup serial comm
//    private void setupCommunication() {
//        isRequesting = true;
//        Runnable requestTask = new Runnable() {
//            @Override
//            public void run() {
//                if (isRequesting) {
//                    try {
//
//                        if (bluetooth != null) {
//                            bluetooth.initializeConnection();  // send the RPM request command
//
//                        } else {
//                            //txtRPM.setText("Bluetooth object is null");
//                        }
//
//                        Log.i(TAG, "Request ongoing...");
//
//                        // schedule the next execution
//                        handler.postDelayed(this, 50);
//                    } catch (Exception e) {
//                        // log the error and display a message on the UI
//                        Log.e(TAG, "Error during data request", e);
//                        //runOnUiThread(() -> txtRPM.setText("Error retrieving data"));
//
//                        // optionally, stop the loop on failure
//                        //stopRequestLoop();
//                    }
//                }
//            }
//        };
//
//        // start the task for the first time
//        handler.post(requestTask);
//    }


    // send the RPM request to the OBD device and update the UI
//    private void requestRPMData() {
//        try {
//            if (bluetooth != null) {
//                sendATCommand(rpmCommand);  // send the RPM request command
//                response = waitForPrompt();  // wait for the response until prompt '>' is received
//
//                if (response == null || response.isEmpty()) {
//                    txtRPM.setText("ERROR, empty response");
//                    return;
//                }
//
//                Log.i(TAG, "Response gotten: " + response);
//                rpmValue = processRPMResponse(response);  // process the response to get RPM
//                txtRPM.setText("RPM: " + rpmValue);  // display the RPM value
//            } else {
//                txtRPM.setText("Bluetooth object is null");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            txtRPM.setText("ERROR");
//        }
//    }

}
