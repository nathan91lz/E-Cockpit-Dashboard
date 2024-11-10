#!/bin/bash
sleep 10  
# Step 1: Start Bluetooth service (if not already running)
echo "Starting Bluetooth service..."
sudo systemctl start bluetooth

# Step 2: Enable Bluetooth discovery mode
echo "Enabling Bluetooth discovery mode..."
bluetoothctl <<EOF
power on
agent on
discoverable on
pairable on
scan on
EOF

# Step 3: Create an RFCOMM serial port listener
echo "Creating RFCOMM serial port listener..."
sudo rfcomm bind /dev/rfcomm0 B8:27:EB:A8:AB:EF 1

# Step 4: Launch Python script in a new terminal window
echo "Launching Python script to emulate OBD-II device..."
sudo python3 /home/lazarowicz/Projects/ECockpitDashboard_Android/fake_OBD2.py

# End
echo "Bluetooth discovery mode and serial port listener started!"

