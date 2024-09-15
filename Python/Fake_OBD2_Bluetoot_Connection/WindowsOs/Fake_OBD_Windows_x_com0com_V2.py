"""
1) Windows parameters : 
windows > bluetooth > parametre bluetooth avancé > autoriser périphérique BT à détecter 
windows > bluetooth > parametre bluetooth avancé > check port COM

"""

import serial
import serial.tools.list_ports
import time

# function to list available COM ports
def list_com_ports():
    ports = list(serial.tools.list_ports.comports())
    for p in ports:
        print(f"Found port: {p.device}")

# function to simulate the OBD-II device
def simulate_obd_device(port, baudrate=9600):
    try:
        # open the serial port
        ser = serial.Serial(port, baudrate=baudrate, timeout=5)
        print(f"Simulating OBD-II device on {port}")

        while True:
            # check if data is received
            if ser.in_waiting > 0:
                command = ser.readline().decode('utf-8').strip()
                print(f"Received command: {command}")

                # handle OBD-II commands
                if command == '010C':
                    # simulate RPM response
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

    except serial.SerialException as e:
        print(f"Error opening serial port: {e}")

if __name__ == '__main__':
    print("Listing available COM ports:")
    list_com_ports()

    # replace 'COMX' with the actual port you want to use
    port = 'COM7'  # update to the actual COM port
    simulate_obd_device(port)
