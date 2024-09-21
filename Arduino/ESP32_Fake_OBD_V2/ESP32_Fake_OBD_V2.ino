#include "BluetoothSerial.h"  

BluetoothSerial SerialBT;

int rpm = 0;                  
int rpmIncrement = 500;        
unsigned long previousMillis = 0;  
const long interval = 500;     // RPM update

const int ledPin = 2;          
bool isConnected = false;     
bool dataReceived = false;    
unsigned long blinkStartMillis = 0;  

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
  unsigned long currentMillis = millis();  // Get current time

  // RPM simulation update every 500ms
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;  
    rpm += rpmIncrement;             
    
    if (rpm > 9000) {                
      rpm = 0;
    }
  }
  
  // check if a Bluetooth client is connected
  if (SerialBT.hasClient()) {
    if (!isConnected) {
      isConnected = true;  // Update connection status
      digitalWrite(ledPin, LOW);  // Turn off LED when connected
    }

    // check for incoming data from the Android app
    if (SerialBT.available()) {
      String command = SerialBT.readStringUntil('\n');  // Read command from Bluetooth
      command.trim();  // Remove any leading/trailing whitespace
      Serial.println("Received command: " + command);

      // indicate data reception by blinking the LED once
      blinkOnce();

      // respond to the specific OBD-II command
      if (command == "010C") {  
        int scaledRPM = rpm * 4;  // multiply the RPM to get the right OBD-II scaling
        int A = scaledRPM / 256;  // first byte of the RPM response
        int B = scaledRPM % 256;  // second byte of the RPM response
        
        String response = "41 0C " + padHex(A) + " " + padHex(B);  
        
        SerialBT.println(response);  // Send simulated RPM response
        Serial.println("Sent RPM: " + String(rpm)); 
      } 
      else if (command == "ATZ") {  // ATZ command
        SerialBT.println("OK");    
      } 
      else if (command == "ATE0") {  // ATE0 command (Echo off)
        SerialBT.println("OK");    
      } 
      else if (command == "ATL0") {  // ATL0 command (Linefeeds off)
        SerialBT.println("OK");    
      } 
      else {  // for any other unrecognized command
        SerialBT.println("ERROR"); 
      }
    }
  } else {
    // handle Bluetooth disconnected state
    if (isConnected) {
      isConnected = false;  
      Serial.println("Bluetooth disconnected.");
    }

    // blink the LED when disconnected
    blinkLed(currentMillis);
  }

  delay(100);  
}

// blink the LED when disconnected (blink every 500ms)
void blinkLed(unsigned long currentMillis) {
  static unsigned long previousBlinkMillis = 0;
  const long blinkInterval = 500;  

  if (currentMillis - previousBlinkMillis >= blinkInterval) {
    previousBlinkMillis = currentMillis;
    digitalWrite(ledPin, !digitalRead(ledPin));
  }
}

// blink the LED once when data is received
void blinkOnce() {
  digitalWrite(ledPin, HIGH); 
  delay(50);                 
  digitalWrite(ledPin, LOW);   
}
