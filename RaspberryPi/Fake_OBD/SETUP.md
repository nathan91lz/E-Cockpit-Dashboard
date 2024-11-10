- On raspberry pi :
sudo apt update
sudo apt install -y bluetooth bluez bluez-tools python3-serial

- Discovery mode :
sudo hciconfig hci0 piscan

- setup
which bluetoothd

sudo vim /lib/systemd/system/bluetooth.service

[Unit]
...
ExecStart=/usr/lib/bluetooth/bluetoothd --compat

> save and exit

---------------------------------------------------------------
LAUNCH :
- Enable Bluetooth discovery mode
sudo hciconfig hci0 piscan

- Create serial port listener :
sudo rfcomm listen hci0 1

- Pair your device 

- Open and run python
cd Project/ECockpitDashboard_Android 

pyhton3 fake_OBD2.py



--------------------------------------------------------------
-For auto launch :
sudo vim /etc/systemd/system/start_bluetooth.service

[Unit]
Description=Start Bluetooth OBD2 Script
After=bluetooth.target

[Service]
ExecStart=/bin/bash /path/to/your/start_bluetooth.sh
Restart=on-failure
User=pi

[Install]
WantedBy=multi-user.target

> save and exit 

- reload system to recognize this new service :
sudo systemctl daemon-reload

- Enable the service to start on boot :
sudo systemctl enable start_bluetooth.service

- Start the service manually :
sudo systemctl start start_bluetooth.service

- check status :
sudo systemctl status start_bluetooth.service

