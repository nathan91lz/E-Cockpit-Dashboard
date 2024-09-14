"""
PROGRAM USE TO FAKE OBD2 CONNECTION 
-> install 'socat' to create virtual serial port

1) Create a virtual serial port  :
% socat -d -d pty,raw,echo=0 pty,raw,echo=0

return :
>% 2024/09/13 13:47:04 socat[41719] N PTY is /dev/ttys007  # virtual serial port created
>% 2024/09/13 13:47:04 socat[41719] N PTY is /dev/ttys008

3) If needed, to stop all 'socat process, use :
% pkill socat

"""


import serial
import time


port = '/dev/ttys008'   # virtual port number
baudrate = 9600

def simulate_obd_device():
    ser = serial.Serial(port, baudrate=baudrate, timeout=1)
    print(f"Simulating OBD-II device on {port}")

    while True:
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
    simulate_obd_device()
