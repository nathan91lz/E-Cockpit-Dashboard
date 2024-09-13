package upsay.decouverteandroid.e_cockpit_dashboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class Bluetooth {
    private static String bluetoothDeviceName;
    private static String bluetoothDeviceMacAddress;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    // UUID for the serial port service on Bluetooth devices
    private static final UUID UUID_SERIAL_PORT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // function to get the MAC address of the currently connected Bluetooth device
    public static String getConnectedDeviceMac() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // check if Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return "Bluetooth not enabled or not supported";
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // iterate over paired devices to find connected device
        for (BluetoothDevice device : pairedDevices) {
            bluetoothDeviceMacAddress = device.getAddress();
            return bluetoothDeviceMacAddress;
        }

        return "No connected Bluetooth device found";
    }

    // function get device name
    public static String getConnectedDeviceName() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Ensure Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return "Bluetooth not enabled or not supported";
        }


        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // iterate over paired devices to find connected device
        for (BluetoothDevice device : pairedDevices) {
            // Return the name of the first paired device
            bluetoothDeviceName = device.getName();
            return bluetoothDeviceName;
        }
        return "No connected Bluetooth device found";
    }


    // connection to the Bluetooth device using MAC address
    public void connect(String macAddress) throws IOException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        socket = device.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT);
        bluetoothAdapter.cancelDiscovery(); // Stop discovery, improves connection time
        socket.connect();

        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    // Send AT command to initialize the OBD-II connection
    public void initializeConnection() throws IOException {
        sendATCommand("ATZ\r");
        sendATCommand("ATE0\r");
        sendATCommand("ATL0\r");
    }

    // Send AT command to request RPM
    public void requestRPM() throws IOException {
        sendATCommand("010C\r");
    }

    // Send an AT command to the device
    private void sendATCommand(String command) throws IOException {
        outputStream.write(command.getBytes());
    }

    // Read the response from the OBD-II device
    public String readResponse() throws IOException {
        byte[] buffer = new byte[1024];
        int bytes = inputStream.read(buffer);
        return new String(buffer, 0, bytes);
    }

    // Process the response to extract RPM
    public int processRPMResponse(String response) {
        if (response.contains("41 0C")) {
            try {
                // Extract hex values after '41 0C'
                String hexA = response.substring(6, 8);
                String hexB = response.substring(9, 11);

                // Convert hex to integers
                int A = Integer.parseInt(hexA, 16);
                int B = Integer.parseInt(hexB, 16);

                // Calculate RPM
                return ((A * 256) + B) / 4;
            } catch (Exception e) {
                e.printStackTrace();
                return -1; // Return -1 on error
            }
        }
        return -1; // Return -1 if '41 0C' not found
    }

    // Close the Bluetooth connection
    public void disconnect() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }

}
