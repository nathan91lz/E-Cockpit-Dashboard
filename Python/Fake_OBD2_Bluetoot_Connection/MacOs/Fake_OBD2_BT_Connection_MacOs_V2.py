import serial
import time
import ctypes
import os
#import subprocess

# Path to virtual serial port
port = '/dev/ttys001'   # Virtual port number
baudrate = 9600

# Load the IOBluetooth framework from the system library (macOS specific)
iobluetooth = ctypes.cdll.LoadLibrary("/System/Library/Frameworks/IOBluetooth.framework/IOBluetooth")

# Declaration of the functions in the macOS Bluetooth API
IOBluetoothPreferenceSetDiscoverableState = iobluetooth.IOBluetoothPreferenceSetDiscoverableState
IOBluetoothPreferenceSetControllerPowerState = iobluetooth.IOBluetoothPreferenceSetControllerPowerState

# Set discoverable state (1 = discoverable, 0 = not discoverable)
def set_bluetooth_discoverable(state: int):
    IOBluetoothPreferenceSetDiscoverableState(state)

# Set Bluetooth power state (1 = powered on, 0 = powered off)
def set_bluetooth_power_state(state: int):
    IOBluetoothPreferenceSetControllerPowerState(state)

# Function to check if a Bluetooth device is connected
def check_bluetooth_connection():
    # Using 'lsof' to check for open connections on the serial port
    command = f"lsof | grep {port}"
    result = os.popen(command).read()
    return port in result

# Simulate the OBD-II device
def simulate_obd_device():
    ser = serial.Serial(port, baudrate=baudrate, timeout=1)
    print(f"Simulating OBD-II device on {port}")

    while True:
        if check_bluetooth_connection():
            print("Device connected to the virtual serial port")
        else:
            print("Waiting for device connection...")

        if ser.in_waiting > 0:
            command = ser.readline().decode('utf-8').strip()
            print(f"Received command: {command}")

            if command == '010C':
                # Simulate RPM response
                response = '41 0C 1A F8\r'
                ser.write(response.encode('utf-8'))
            elif command == 'ATZ':
                ser.write(b'OK\r')
            elif command == 'ATE0':
                ser.write(b'OK\r')
            elif command == 'ATL0':
                ser.write(b'OK\r')
            else:
                ser.write(b'ERROR\r')

        time.sleep(1)

if __name__ == '__main__':
    # Start 'socat' to create virtual serial ports
    #print("Creating virtual serial ports using socat...")
    #subprocess.Popen(['socat', '-d', '-d', 'pty,raw,echo=0', 'pty,raw,echo=0'])

    # Turn on Bluetooth and make it discoverable
    print("Turning Bluetooth power on and making it discoverable...")
    set_bluetooth_power_state(1)  # Power on Bluetooth
    set_bluetooth_discoverable(1) # Make Bluetooth discoverable

    # Simulate OBD-II device over virtual serial port
    simulate_obd_device()
