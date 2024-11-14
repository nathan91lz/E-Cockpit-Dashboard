package upsay.decouverteandroid.e_cockpit_dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
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
    public static OutputStream outputStream;
    public static InputStream inputStream;
    public static String macAddress = getConnectedDeviceMac();
    public static String deviceName = getConnectedDeviceName();


    // UUID for the serial port service on Bluetooth devices
    private static final UUID UUID_SERIAL_PORT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String TAG = "Bluetooth";



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


    // send an AT command to the device
    public static void sendATCommand(String command) throws IOException {
        outputStream.write(command.getBytes());
        Log.i(TAG, command + "send");
    }


    // waiting for the response to the command until the prompt '>' is received and get it
    public static String waitForPrompt() throws IOException {
        byte[] buffer = new byte[64];
        StringBuilder responseBuilder = new StringBuilder();

        while (true) {
            int bytes = inputStream.read(buffer);  // read from the input stream
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


    // close the Bluetooth connection
    public void disconnect() throws IOException {
        // check if inputStream is not null before closing it
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
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
                    res1.append(java.lang.String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";

    }



}
