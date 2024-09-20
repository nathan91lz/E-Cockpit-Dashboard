#include "BluetoothSerial.h"  // Import Bluetooth Serial library for ESP32

BluetoothSerial SerialBT;

int rpm = 0;                  // Start at 0 RPM
int rpmIncrement = 100;        // Increment RPM by 100
unsigned long previousMillis = 0;  // To track time intervals
const long interval = 500;     // 500 ms interval for RPM update

// Function to pad the hex string to always be 2 characters long
String padHex(int value) {
  String hexString = String(value, HEX);
  if (hexString.length() < 2) {
    hexString = "0" + hexString;  // Add leading zero if necessary
  }
  return hexString;
}

void setup() {
  Serial.begin(115200);        // Start Serial Monitor for debugging
  SerialBT.begin("OBD2_Fake"); // Start Bluetooth and set name to ESP32_OBD
  Serial.println("ESP32 OBD-II Emulator started");
}

void loop() {
  unsigned long currentMillis = millis();  // Get current time
  
  // RPM incrementation loop every 500ms
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;  // Update previous time
    rpm += rpmIncrement;             // Increment RPM by 100
    
    if (rpm > 9000) {                // Reset to 0 if RPM exceeds 9000
      rpm = 0;
    }
  }
  
  if (SerialBT.hasClient()) {
    // Check for incoming data from the Android app
    if (SerialBT.available()) {
      String command = SerialBT.readStringUntil('\n');  // Read command from Bluetooth
      command.trim();  // Remove any leading/trailing whitespace
      Serial.println("Received command: " + command);

      // Respond to the specific OBD-II command
      if (command == "010C") {  // Request for RPM

        int scaledRPM = rpm * 4;  // Multiply the RPM to get the right OBD-II scaling
        int A = scaledRPM / 256;  // First byte of the RPM response
        int B = scaledRPM % 256;  // Second byte of the RPM response
        
        // Ensure that the hex output is always two digits by padding with zeros
        String response = "41 0C " + padHex(A) + " " + padHex(B);  
        
        SerialBT.println(response);  // Send simulated RPM response
        Serial.println("Sent RPM: " + String(rpm));  // Debug print
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

  delay(100);  // Small delay to prevent CPU overload
}
