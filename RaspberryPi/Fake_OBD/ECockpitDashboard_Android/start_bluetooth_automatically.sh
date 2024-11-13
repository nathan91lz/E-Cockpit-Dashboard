#!/bin/bash

# Put Bluetooth into discovery mode
bluetoothctl <<EOF
power on
discoverable on
pairable on
agent NoInputNoOutput
default-agent
EOF

# Create a Bluetooth RFCOMM serial port
sudo rfcomm bind /dev/rfcomm0 B8:27:EB:A8:AB:EF 1

# Launch the Python script (replace with the correct path)
# Ensure it runs in the foreground so the systemd service remains active
/usr/bin/python3 /home/lazarowicz/Projects/ECockpitDashboard_Android/fake_OBD2.py

