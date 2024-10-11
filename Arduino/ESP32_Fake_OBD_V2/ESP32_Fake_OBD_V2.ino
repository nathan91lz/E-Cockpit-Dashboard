#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

int rpm = 0;
int maxRPM = 8000;
int rpmIncrement = 500;

unsigned long previousMillis = 0;
const long interval = 500;  // update

const int ledPin = 2;
bool isConnected = false;

// coolant temperature simulation
int coolantTemp = 0;
const int maxCoolantTemp = 110;
const int coolantIncrement = 1;
// air temperature simulation
int airTemp = 0;
const int maxAirTemp = 40;
const int airIncrement = 1;

String padHex(int value) {
  String hexString = String(value, HEX);
  if (hexString.length() < 2) {
    hexString = "0" + hexString;  // add zero padding
  }
  return hexString;
}

void setup() {
  Serial.begin(115200);
  SerialBT.begin("OBD2_Fake");
  Serial.println("ESP32 OBD-II Emulator started");
  pinMode(ledPin, OUTPUT);
}

void loop() {
  static unsigned long lastCommandCheck = 0;
  unsigned long currentMillis = millis();  // Get current time

  // Update simulation every 500ms
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    rpm += rpmIncrement;
    coolantTemp += coolantIncrement;
    airTemp += airIncrement;

    // Reset values if they exceed their max
    if (rpm > maxRPM) {
      rpm = 0;
    }
    if (coolantTemp > maxCoolantTemp) {
      coolantTemp = 0;
    }
    if (airTemp > maxAirTemp) {
      airTemp = 0;
    }
  }

  // Check if a Bluetooth client is connected
  if (SerialBT.hasClient()) {
    if (!isConnected) {
      isConnected = true;  // Update connection status
      digitalWrite(ledPin, LOW);  // Turn off LED when connected
      Serial.println("Bluetooth connected.");
    }

    // Check for incoming data from the Android app
    if (currentMillis - lastCommandCheck >= 100) { // Check for commands every 100 ms
      lastCommandCheck = currentMillis;
      if (SerialBT.available()) {
        String command = SerialBT.readStringUntil('\n');  // Read command from Bluetooth
        command.trim();  // Remove any leading/trailing whitespace
        Serial.println("Received command: " + command);

        // Indicate data reception by blinking the LED once
        blinkOnce();

        // Respond to specific OBD-II commands
        if (command == "010C") {
          int scaledRPM = rpm * 4;  // OBD-II scaling
          int A = scaledRPM / 256;   // first byte of the RPM response
          int B = scaledRPM % 256;   // second byte of the RPM response
          String response = "41 0C " + padHex(A) + " " + padHex(B) + " >";
          SerialBT.println(response);  // Send simulated RPM response
          Serial.println("Sent RPM: " + String(rpm));
        } 
        else if (command == "0105") {  // Coolant temperature request
          String response = "41 05 " + padHex(coolantTemp) + " >";
          SerialBT.println(response);  // Send simulated coolant temp response
          Serial.println("Sent Coolant Temperature: " + String(coolantTemp));
        }
        else if (command == "010F") {  // Air temperature request
          String response = "41 0F " + padHex(airTemp) + " >";
          SerialBT.println(response);  // Send simulated air temp response
          Serial.println("Sent Air Temperature: " + String(airTemp));
        }
        else if (command == "ATZ" || command == "ATE0" || command == "ATL0") {  // Handle AT commands
          SerialBT.println("OK >");
        } 
        else {  // For any other unrecognized command
          SerialBT.println("ERROR >");
        }
      }
    }
  } else {
    // Handle Bluetooth disconnected state
    if (isConnected) {
      isConnected = false;
      Serial.println("Bluetooth disconnected.");
      digitalWrite(ledPin, HIGH);  // Turn on LED when disconnected
    }
  }

  // Blink LED when disconnected
  blinkLed(currentMillis);
}

// Blink the LED once when data is received
void blinkOnce() {
  digitalWrite(ledPin, HIGH);
  delay(50);
  digitalWrite(ledPin, LOW);
}

// Blink the LED when disconnected (blink every 500ms)
void blinkLed(unsigned long currentMillis) {
  static unsigned long previousBlinkMillis = 0;
  const long blinkInterval = 500;

  if (currentMillis - previousBlinkMillis >= blinkInterval) {
    previousBlinkMillis = currentMillis;
    digitalWrite(ledPin, !digitalRead(ledPin));
  }
}
