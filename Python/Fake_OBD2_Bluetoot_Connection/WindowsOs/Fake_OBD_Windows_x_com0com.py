"""
MAC Address of computer : 50-84-92-C3-7F-89
Computer Name : DESKTOP-PLOQD37

1) Use com0com application to emulate serial com port
- create virtual COM port COM7 <-> COM8

2) Run pycode
"""


import serial
import time
import bluetooth

# Path to the virtual serial port (use the virtual COM port created)
port = 'COM7'   # Change this to your virtual COM port number
baudrate = 9600

# Function to check if a Bluetooth device is connected
def check_bluetooth_connection():
    try:
        # Discover nearby Bluetooth devices
        devices = bluetooth.discover_devices(duration=8, lookup_names=True)
        if devices:
            for addr, name in devices:
                print(f"Found Bluetooth device: {name} [{addr}]")
            return True
        return False
    except bluetooth.BluetoothError as e:
        print(f"Bluetooth error: {e}")
        return False

# Simulate the OBD-II device
def simulate_obd_device():
    ser = serial.Serial(port, baudrate=baudrate, timeout=1)
    print(f"Simulating OBD-II device on {port}")

    while True:
        if check_bluetooth_connection():
            print("Device connected to the virtual serial port")
        else:
            print("Waiting for Bluetooth device connection...")

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
    # Simulate OBD-II device over virtual serial port
    simulate_obd_device()
