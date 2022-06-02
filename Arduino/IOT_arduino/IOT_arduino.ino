#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <IRremote.h>


int count = 0;
boolean isTurnOn = false;
boolean isAuto = false;
int speeds = 0;
int state = LOW;
int val = 0;

int motorPin = 12;
int PIRSensorIn = 16;
int PIRSensorOut = 5;

// WiFi
const char *ssid = "mmslab_smallRoom"; // Enter your WiFi name
const char *password = "mmslab406";  // Enter WiFi password
const char *client_id = "client-test";
   
// MQTT Broker
const char *mqtt_broker = "broker.emqx.io";
const char *topic = "mqttTest";
const char *mqtt_username = "";
const char *mqtt_password = "";
const int mqtt_port = 1883;
   
WiFiClient espClient;
PubSubClient client(espClient);

DynamicJsonDocument doc(1024);
   
void setup() {
    
    Serial.begin(9600);
    pinMode(Led, OUTPUT);
    pinMode(PIRSensor, INPUT);
//    irrecv.enableIRIn(); // Start the receiver
//    Serial.println("IR Receiver Ready...");
//    Serial.println(F("Enabling IRin"));
//    IrReceiver.begin(IR_RECEIVE_PIN,ENABLE_LED_FEEDBACK);
//    Serial.print(F("Ready to receive IR signals at pin "));
//    Serial.println(IR_RECEIVE_PIN);
    
    // connecting to a WiFi network
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.println("Connecting to WiFi..");
    }
    Serial.println("Connected to the WiFi network");
    //connecting to a mqtt broker
    client.setServer(mqtt_broker, mqtt_port);
    client.setCallback(callback);
    while (!client.connected()) {
//        char client_id = "esp8266-client-";
//        client_id += String(WiFi.macAddress());
        Serial.println("Connecting to public emqx mqtt broker.....");
        if (client.connect(client_id, mqtt_username, mqtt_password)) {
            Serial.println("Public emqx mqtt broker connected");
        } else {
            Serial.print("failed with state ");
            Serial.print(client.state());
            delay(2000);
        }
    }
    // publish and subscribe
    doc["isTurnOn"] = false;
    doc["speed"] = 0;
    doc["count"] = 0;
    char json[256];
    serializeJson(doc, json);
    client.publish(topic, json);
    client.subscribe(topic);
}
   
void callback(char *topic, byte *payload, unsigned int length) {
    Serial.println("-----------------------");
    Serial.print("Message arrived in topic: ");
    Serial.println(topic);
    Serial.print("Message:");
    String message;
    for (int i = 0; i < length; i++) {
        message = message + (char) payload[i];  // convert *byte to string
    }
    Serial.println(message);

    deserializeJson(doc, message);
    isTurnOn = doc["isTurnOn"].as<boolean>();
    speeds = doc["speed"].as<int>();
    count = doc["count"].as<int>();
    
    Serial.println("-----------------------");
}
   
void loop() {
    client.loop();

    //風扇控制
    if (isAuto) {
      if (count <= 2) {
      analogWrite(motorPin, 0);
      } else if (2 < count <= 4) {
        analogWrite(motorPin, 127);
        isTurnOn = true;
      } else if (count <= 5) {
        analogWrite(motorPin, 255);
        isTurnOn = true;
      }
    } else {
      if (isTurnOn){
        analogWrite(Led, speeds);
      } else {
        analogWrite(Led, 0);
      }
    }
    
    //進門
    enter = digitalRead(PIRSensorIn); //讀取Sensor是否有偵測到物體移動
    if(enter == HIGH){ //如果有物體移動
      delay(500);

      if (state == LOW){
        count ++;
        Serial.print("偵測進門");

        doc["isTurnOn"] = isTurnOn;
        doc["speed"] = speeds;
        doc["count"] = count;
        char json[256];
        serializeJson(doc, json);
        client.publish(topic, json);
        state = HIGH;
      }
    } else {
      delay(500);

      if (state == HIGH) {
        Serial.println("-------");
        state = LOW;
      }
    }

    //出門
    enter = digitalRead(PIRSensorOut); //讀取Sensor是否有偵測到物體移動
    if(enter == HIGH){ //如果有物體移動
      delay(500);

      if (state == LOW){
        count ++;
        Serial.print("偵測出門");

        doc["isTurnOn"] = isTurnOn;
        doc["speed"] = speeds;
        doc["count"] = count;
        char json[256];
        serializeJson(doc, json);
        client.publish(topic, json);
        state = HIGH;
      }
    } else {
      delay(500);

      if (state == HIGH) {
        Serial.println("=======");
        state = LOW;
      }
    }
}
