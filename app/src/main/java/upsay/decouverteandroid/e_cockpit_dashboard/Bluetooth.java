package upsay.decouverteandroid.e_cockpit_dashboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class Bluetooth {
    // function to get the MAC address of the currently connected Bluetooth device
    public static String getConnectedDeviceMac() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // check if Bluetooth is supported and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return "Bluetooth not enabled or not supported";
        }

        // Iterate over paired devices to find connected device
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            // Add your logic here to determine if the device is connected
            // In some cases, this can be done using Bluetooth profiles
            // Example just returns the first paired device's MAC address
            return device.getAddress();  // Return the MAC address
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

        // Iterate over paired devices to find connected device
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            // Return the name of the first paired device
            return device.getName();
        }

        return "No connected Bluetooth device found";
    }

}
