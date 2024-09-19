#include "BluetoothSerial.h"  // Import Bluetooth Serial library for ESP32

BluetoothSerial SerialBT;

void setup() {
  Serial.begin(115200);        // Start Serial Monitor for debugging
  SerialBT.begin("OBDII_Fake"); // Start Bluetooth and set name to ESP32_OBD
  Serial.println("ESP32 OBD-II Emulator started");
}

void loop() {
  if (SerialBT.hasClient()) {
    // Check for incoming data from the Android app
    if (SerialBT.available()) {
      String command = SerialBT.readStringUntil('\n');  // Read command from Bluetooth
      command.trim();  // Remove any leading/trailing whitespace
      Serial.println("Received command: " + command);

      // Respond to the specific OBD-II command
      if (command == "010C") {  // Request for RPM
        SerialBT.println("41 0C 1A F8");  // Simulated RPM response (example: 6800 RPM)
      } 
      else if (command == "ATZ") {  // ATZ command
        SerialBT.println("OK");     // Respond with OK
      } 
      else if (command == "ATE0") {  // ATE0 command (Echo off)
        SerialBT.println("OK");     // Respond with OK
      } 
      else if (command == "ATL0") {  // ATL0 command (Linefeeds off)
        SerialBT.println("OK");     // Respond with OK
      } 
      else {  // Any other unrecognized command
        SerialBT.println("ERROR");  // Respond with ERROR
      }
    }
  } else {
    Serial.println("Waiting for Bluetooth connection...");
  }

  delay(1000);  // Wait 1 second before checking again
}
