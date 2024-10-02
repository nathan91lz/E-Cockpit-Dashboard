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
    public void initializeConnection() throws IOException, InterruptedException {
        sendATCommand("ATZ\r");
        Thread.sleep(200);

        sendATCommand("ATE0\r");
        Thread.sleep(200);

        sendATCommand("ATL0\r");
        Thread.sleep(200);
    }


    // send AT command to request RPM
    public void requestRPM() throws IOException {
        sendATCommand("010C\r");
    }


    // send AT command to request Fuel level
    public void requestFuelLevel() throws IOException {
        sendATCommand("012F\r");
    }


    // send AT command to request Fuel level
    public void requestAmbientAirTemp() throws IOException {
        sendATCommand("0146\r");
    }


    // send AT command to request Fuel level
    public void requestEngineOilTemp() throws IOException {
        sendATCommand("015C\r"); // check
    }


    // send AT command to request Fuel level
    public void requestCoolantTemp() throws IOException {
        sendATCommand("0105\r");
    }


    // send AT command to request Fuel level
    public void requestIntakeAirTemp() throws IOException {
        sendATCommand("010F\r");
    }


    // send an AT command to the device
    public void sendATCommand(String command) throws IOException {
        outputStream.write(command.getBytes());
    }


    public String readResponse(String expectedResponseCode) throws IOException {
        byte[] buffer = new byte[32];  // Adjust buffer size if necessary
        int bytes = inputStream.read(buffer);
        String response = new String(buffer, 0, bytes);
        Log.i(TAG, "Response: " + response);

        // clean up the response by removing unwanted characters like carriage returns and '>'
        response = response.replace("\r", "").replace("\n", "").replace(">", "").trim();

        // Find and extract the part containing the expected response code followed by two bytes
        int index = response.indexOf(expectedResponseCode);
        if (index != -1 && response.length() >= index + expectedResponseCode.length() + 6) {
            // Expected response code + 2 bytes = expectedResponseCode length + 6 characters (including spaces)
            return response.substring(index, index + expectedResponseCode.length() + 6);  // Return the full response
        } else {
            return "Invalid response";  // Return a message or handle the error case
        }
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
