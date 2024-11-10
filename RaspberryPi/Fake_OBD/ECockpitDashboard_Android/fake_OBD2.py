import bluetooth
import time

# Simulation parameters
rpm = 0
max_rpm = 8000
rpm_increment = 100

coolant_temp = 50
max_coolant_temp = 120
coolant_increment = 1

air_temp = 0
max_air_temp = 40
air_increment = 1

led_state = False
is_connected = False
previous_millis = time.time() * 1000
interval = 200  # ms > 500 default || 200 ok
led_pin = 2  # Placeholder for an LED pin simulation

def pad_hex(value):
    return "{:02X}".format(value)

# Simulate LED blinking for data reception
def blink_once():
    global led_state
    led_state = True
    time.sleep(0.05)
    led_state = False

# Blink LED every 500ms when disconnected
def blink_led(current_millis):
    global led_state
    if not is_connected and (current_millis - previous_millis) >= 500:
        led_state = not led_state
        print("LED ON" if led_state else "LED OFF")

# Setup Bluetooth server
server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
server_sock.bind(("", bluetooth.PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]
bluetooth.advertise_service(server_sock, "OBD2_Fake",
                            service_id="00001101-0000-1000-8000-00805F9B34FB",
                            service_classes=["00001101-0000-1000-8000-00805F9B34FB", bluetooth.SERIAL_PORT_CLASS],
                            profiles=[bluetooth.SERIAL_PORT_PROFILE])

print("Waiting for Bluetooth connection on RFCOMM channel", port)

try:
    client_sock, client_info = server_sock.accept()
    print("Accepted connection from", client_info)
    is_connected = True
except Exception as e:
    print("Could not accept connection:", e)
    exit(1)

# Main loop
try:
    last_command_check = time.time() * 1000  # Last time commands were checked
    while True:
        current_millis = time.time() * 1000

        # Update simulated sensor values
        if current_millis - previous_millis >= interval:
            previous_millis = current_millis
            rpm = (rpm + rpm_increment) % (max_rpm + rpm_increment)
            coolant_temp = (coolant_temp + coolant_increment) % (max_coolant_temp + coolant_increment)
            air_temp = (air_temp + air_increment) % (max_air_temp + air_increment)

        # Check for incoming Bluetooth commands every 100ms
        if current_millis - last_command_check >= 100:
            last_command_check = current_millis
            try:
                data = client_sock.recv(1024).decode().strip()
                if len(data) == 0:
                    continue
                
                print("Received command:", data)
                blink_once()

                # Respond to specific OBD-II commands
                if data == "010C":  # RPM request
                    scaled_rpm = rpm * 4
                    A = scaled_rpm // 256
                    B = scaled_rpm % 256
                    response = f"41 0C {pad_hex(A)} {pad_hex(B)} >"
                    client_sock.send(response + "\n")
                    print(f"Sent RPM: {rpm}")

                elif data == "0105":  # Coolant temperature request
                    response = f"41 05 {pad_hex(coolant_temp)} >"
                    client_sock.send(response + "\n")
                    print(f"Sent Coolant Temperature: {coolant_temp}")

                elif data == "010F":  # Air temperature request
                    response = f"41 0F {pad_hex(air_temp)} >"
                    client_sock.send(response + "\n")
                    print(f"Sent Air Temperature: {air_temp}")

                elif data in ["ATZ", "ATE0", "ATL0"]:  # AT commands
                    client_sock.send("OK >\n")

                else:
                    client_sock.send("ERROR >\n")

            except bluetooth.btcommon.BluetoothError as e:
                print("Bluetooth error:", e)
                is_connected = False
                break

        # Blink LED when disconnected
        blink_led(current_millis)

except KeyboardInterrupt:
    print("Shutting down.")

finally:
    client_sock.close()
    server_sock.close()
    print("Bluetooth connection closed.")

