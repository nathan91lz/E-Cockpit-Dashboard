// Arduino nana > old bootloader

#include <SoftwareSerial.h>

// Define pins for SoftwareSerial communication
SoftwareSerial bluetooth(2, 3);  // RX, TX (Connect to Bluetooth Module's TX, RX)

// Variables to simulate OBD-II responses
String command = "";
String rpmResponse = "41 0C 1A F8\r";  // Example: 6900 RPM (in hexadecimal)

void setup() {
  // Start Serial communication for debugging
  Serial.begin(9600);
  
  // Start Bluetooth communication
  bluetooth.begin(9600);
  
  Serial.println("Fake OBD2 Bluetooth Device Initialized...");
}

void loop() {
  // Check if any data is available from Bluetooth module
  if (bluetooth.available()) {
    char incomingChar = (char)bluetooth.read();
    command += incomingChar;  // Store incoming characters
    
    // Check for the end of the command (OBD-II commands end with '\r')
    if (incomingChar == '\r') {
      Serial.println("Received OBD-II Command: " + command);

      // Handle specific OBD-II commands
      if (command == "010C\r") {  // RPM Request
        bluetooth.print(rpmResponse);  // Send RPM response
        Serial.println("Sent RPM Response: " + rpmResponse);
      }
      else if (command == "ATZ\r") {  // Reset Command
        bluetooth.print("OK\r");
        Serial.println("Sent: OK");
      }
      else if (command == "ATE0\r") {  // Echo Off
        bluetooth.print("OK\r");
        Serial.println("Echo Off");
      }
      else if (command == "ATL0\r") {  // Linefeed Off
        bluetooth.print("OK\r");
        Serial.println("Linefeed Off");
      }
      else {
        bluetooth.print("ERROR\r");
        Serial.println("Sent: ERROR");
      }
      command = "";  // Clear the command buffer for the next command
    }
  }
}

