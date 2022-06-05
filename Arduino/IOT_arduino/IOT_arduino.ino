#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>


int count = 0;
boolean isTurnOn = false;
boolean isAuto = true;
int speeds = 0;
int state = LOW;
int state2 = LOW;
int val = 0;

int motorPin = 12;
int PIRSensorIn = 16;
int PIRSensorOut = 5;

// WiFi
const char *ssid = "1234"; // Enter your WiFi name
const char *password = "jk871124";  // Enter WiFi password
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
    pinMode(motorPin, OUTPUT);
    pinMode(PIRSensorIn, INPUT);
    
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
    isAuto = doc["isAuto"].as<boolean>();
    isTurnOn = doc["isTurnOn"].as<boolean>();
    speeds = doc["speed"].as<int>();
    count = doc["count"].as<int>();
    
    Serial.println("-----------------------");
}

void controlFan(){
  //風扇控制
  if (isAuto) {
    if (count <= 2) {
      speeds = 0;
      isTurnOn = false;
    } else if (count > 2 && count <= 4) {
      speeds = 150;
      isTurnOn = true;
    } else if (count > 4 && count <= 6){
      speeds = 200;
      isTurnOn = true;
    } else {
      speeds = 255;
      isTurnOn = true;
    }
    analogWrite(motorPin, speeds);
  } else {
    if (isTurnOn){
      analogWrite(motorPin, speeds);
    } else {
      analogWrite(motorPin, 0);
    }
  }
}
   
void loop() {
    client.loop();

    controlFan();
    
    //進門
    int enter = digitalRead(PIRSensorIn); //讀取Sensor是否有偵測到物體移動
    if(enter == HIGH){ //如果有物體移動
      if (state == LOW){
        count ++;
        Serial.println("偵測進門");

        controlFan();

        doc["isAuto"] = isAuto;
        doc["isTurnOn"] = isTurnOn;
        doc["speed"] = speeds;
        doc["count"] = count;
        char json[256];
        serializeJson(doc, json);
        client.publish(topic, json);
        state = HIGH;
      }
    } else {
      if (state == HIGH) {
        Serial.println("________________");
        state = LOW;
      }
    }

    //出門
    int out = digitalRead(PIRSensorOut); //讀取Sensor是否有偵測到物體移動
    if(out == HIGH){ //如果有物體移動
      if (state2 == LOW){
        if (count > 0) {
          count --;
        }
        
        Serial.print("偵測出門");

        controlFan();

        doc["isAuto"] = isAuto;
        doc["isTurnOn"] = isTurnOn;
        doc["speed"] = speeds;
        doc["count"] = count;
        char json[256];
        serializeJson(doc, json);
        client.publish(topic, json);
        state2 = HIGH;
      }
    } else {
      if (state2 == HIGH) {
        Serial.println("===================");
        state2 = LOW;
      }
    }
}
