#include <SoftwareSerial.h>
#include <ArduinoJson.h>


const int LED = 3;
bool isLedOn = false;
SoftwareSerial Bluetooth(10, 11);  // rx tx


void setup() {
  Serial.begin(9600);
  Bluetooth.begin(9600);
  delay(1000);
  pinMode(LED, OUTPUT);
}

void loop() {
  String reqStr = readSerial();

  if (reqStr.length() == 0) {
    return;
  }

  handleRequest(reqStr);
}


void handleRequest(const String& reqStr) {
  Serial.println(reqStr);
  const JsonObject& request = deserialize(reqStr);
  const int action = request["action"];
  const JsonObject& params = request["params"];
  const int brightness = params["brightness"];

  Serial.println(action);
  Serial.println(brightness);

  switch (action) {
    case 1:  // LED off
      ledOff();
      isLedOn = false;
      Serial.print("LED OFF");
      break;

    case 2:  // LED on
      ledOn();
      isLedOn = true;
      Serial.println("LED ON");
      break;

    /*
    case 3:  // Adjust LED brightness
      if (brightness >= 0 && brightness <= 255) {
        if (isLedOn) {
          analogWrite(LED, brightness);
          Serial.println(brightness);
          delay(10);
        }
      }
      break;
      */

    default:
      Serial.println("Wrong bluetooth event");
  }
}


String readSerial() {
  String s = "";
  bool isEnd = false;
  long int time = millis();
  const int timeout = 500;

  while ((time + timeout) > millis())
  {
    while (Bluetooth.available())
    {

      // The esp has data so display its output to the serial window
      char c = Bluetooth.read(); // read the next character.
      s += c;
    }
  }

  return s;
}

JsonObject& deserialize(const String& str) {
  // Reserve memory space
  StaticJsonBuffer<256> jsonBuffer;

  // Deserialize the JSON string
  JsonObject& result = jsonBuffer.parseObject(str);

  if (!result.success())
  {
    Serial.println("parseObject() failed");
    return jsonBuffer.createObject();
  }

  return result;
}

void ledOn() {
  analogWrite(LED, 255);
  delay(10);
}

void ledOff() {
  analogWrite(LED, 0);
  delay(10);
}
