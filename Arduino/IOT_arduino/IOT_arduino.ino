#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#define LED 1 
   
// WiFi
const char *ssid = "mmslab_smallRoom"; // Enter your WiFi name
const char *password = "mmslab406";  // Enter WiFi password
const char *client_id = "client-test";
   
// MQTT Broker
const char *mqtt_broker = "broker.emqx.io";
const char *topic = "mqtt";
const char *mqtt_username = "";
const char *mqtt_password = "";
const int mqtt_port = 1883;
   
WiFiClient espClient;
PubSubClient client(espClient);
   
void setup() {
  
   pinMode(LED, OUTPUT);
  
    // Set software serial baud to 115200;
    Serial.begin(115200);
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
    client.publish(topic, "hello emqx");
    client.subscribe(topic);
}
   
void callback(char *topic, byte *payload, unsigned int length) {
    Serial.print("Message arrived in topic: ");
    Serial.println(topic);
    Serial.print("Message:");
    String message;
    for (int i = 0; i < length; i++) {
        message = message + (char) payload[i];  // convert *byte to string
    }
    Serial.print(message);
//    if (message == "on") { digitalWrite(LED, LOW); }   // LED on
//    if (message == "off") { digitalWrite(LED, HIGH); } // LED off
    Serial.println();
    Serial.println("-----------------------");
}
   
void loop() {
    client.loop();
}
