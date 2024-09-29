package upsay.decouverteandroid.e_cockpit_dashboard;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Bluetooth {
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    public static String macAddress = getConnectedDeviceMac();
    public static String deviceName = getConnectedDeviceName();

    // UUID for the serial port service on Bluetooth devices
    private static final UUID UUID_SERIAL_PORT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String TAG = "ECockpitDashboard";




    // DEBUG MODE ! MAC ADDRESS FAKED
    // function to get the MAC address of the currently connected Bluetooth device
    public static String getConnectedDeviceMac() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // check if Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return "Bluetooth not enabled or not supported";
        }

        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // iterate over paired devices to find connected device
        for (BluetoothDevice device : pairedDevices) {
            macAddress = device.getAddress();
            return macAddress;
        }

        return "No connected Bluetooth device found";
    }

    // Used for BLE
    public static String getConnectedDeviceMacFromMacAddress(String macAddress) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check if Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return "Bluetooth not enabled or not supported";
        }

        // Retrieve the device based on the provided MAC address
        @SuppressLint("MissingPermission")
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        if (device != null) {
            return macAddress;  // Return the provided MAC address
        }

        return "No connected Bluetooth device found";
    }


    // DEBUG MODE ! DEVICE NAME FAKED
    // function get device name
    @SuppressLint("MissingPermission")
    public static String getConnectedDeviceName() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Ensure Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return "Bluetooth not enabled or not supported";
        }


        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // iterate over paired devices to find connected device
        for (BluetoothDevice device : pairedDevices) {
            // Return the name of the first paired device
            deviceName = device.getName();

            return deviceName;
        }
        return "No connected Bluetooth device found";
    }


    // used for BLE
    @SuppressLint("MissingPermission")
    public static String getConnectedDeviceNameFromMacAddress(String macAddress) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Ensure Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return "Bluetooth not enabled or not supported";
        }

        // Retrieve the device by its MAC address
        @SuppressLint("MissingPermission")
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        if (device != null) {
            return device.getName();  // Return the name of the device
        }

        return "No connected Bluetooth device found";
    }


    // connection to the Bluetooth device using MAC address
    @SuppressLint("MissingPermission")
    public void connect(String macAddress) throws IOException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        socket = device.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT);
        bluetoothAdapter.cancelDiscovery(); // Stop discovery, improves connection time
        socket.connect();

        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    
    // send AT command to initialize the OBD-II connection
    public void initializeConnection() throws IOException {
        sendATCommand("ATZ\r");
        sendATCommand("ATE0\r");
        sendATCommand("ATL0\r");
    }


    // send AT command to request RPM
    public void requestRPM() throws IOException {
        sendATCommand("010C\r");
    }


    // send an AT command to the device
    public void sendATCommand(String command) throws IOException {
        outputStream.write(command.getBytes());
    }


    // read the response from the OBD-II device
//    public String readResponse() throws IOException {
//        byte[] buffer = new byte[32]; // HERE PROBLEM CRASH test
//        int bytes = inputStream.read(buffer);
//        return new String(buffer, 0, bytes);
//    }
    public String readResponse() throws IOException {
        byte[] buffer = new byte[32];  // adjust buffer size if necessary
        int bytes = inputStream.read(buffer);
        String response = new String(buffer, 0, bytes);
        Log.i(TAG, "Response :" + response);

        // Remove unwanted characters like carriage returns (^M) and trailing '>'
        response = response.replace("\r", "").replace("\n", "").replace(">", "").trim();

        // Find and extract the part containing '41 0C' followed by two bytes
        int index = response.indexOf("41 0C");
        if (index != -1 && response.length() >= index + 11) { // '41 0C' + 2 bytes is 11 characters (including spaces)
            return response.substring(index, index + 11);  // Return only '41 0C XX XX'
        } else {
            return "Invalid response"; // Return a message or handle error case
        }
    }


    // process the response to extract RPM
    public int processRPMResponse(String response) {
        if (response.contains("41 0C")) {
            try {
                // extract hex values after '41 0C'
                String hexA = response.substring(6, 8);
                String hexB = response.substring(9, 11);

                // convert hex to integers
                int A = Integer.parseInt(hexA, 16);
                int B = Integer.parseInt(hexB, 16);

                // Calculate RPM
                return ((A * 256) + B) / 4;
            } catch (Exception e) {
                e.printStackTrace();
                return -1; // Return -1 on error
            }
        }
        return -2; // Return -1 if '41 0C' not found
    }


    // close the Bluetooth connection
    public void disconnect() throws IOException {
        // check if inputStream is not null before closing it
        if (inputStream != null) {
            inputStream.close();
            inputStream = null; // reset to null after closing
        }
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }


    // get own mac address
    public static String getOwnMacAddres() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";


//        WifiManager wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        String wifiMacAddress = wimanager.getConnectionInfo().getMacAddress();
//        if (wifiMacAddress == null) {
//            wifiMacAddress = "Device don't have mac address or wi-fi is disabled";
//        }
//
//        return wifiMacAddress;

//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        String ownMacAddress = bluetoothAdapter != null ? bluetoothAdapter.getAddress() : "Unavailable";
//        return ownMacAddress;
    }

}
