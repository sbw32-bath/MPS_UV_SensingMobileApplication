#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <BLESecurity.h>
#include <Adafruit_NeoPixel.h>

// ========== Feather ESP32 V2 NeoPixel Setup ==========
#if defined(ADAFRUIT_FEATHER_ESP32_V2) || defined(ARDUINO_ADAFRUIT_ITSYBITSY_ESP32)
  #define PIN_NEOPIXEL 0
  #define NEOPIXEL_I2C_POWER 2
#endif

Adafruit_NeoPixel pixel(1, PIN_NEOPIXEL, NEO_GRB + NEO_KHZ800);

// ========== BLE UUIDs ==========
#define SERVICE_UUID        "ed69bccc-61bc-433c-b670-dc8bf3eaafa6"
#define CHAR_NOTIFY_UUID    "673262db-119e-49ca-aae8-f517404b0f4f"

BLEServer* pServer = nullptr;
BLECharacteristic* pNotifyChar = nullptr;

bool deviceConnected = false;
bool wasConnected = false;

const int UV_SENSOR_PIN = 26; //Connecting to A0 on board

const int MAX_SENSOR_VALUE = 0;  // 0 corresponds to the maximum UV index
const int MIN_SENSOR_VALUE = 4095;  // 4095 corresponds to the minimum UV index

const float MAX_UV_INDEX = 10.0;  // Maximum UV Index
const float MIN_UV_INDEX = 0.0;   // Minimum UV Index

// ========== Function to Convert Sensor Value to UV Index ==========
float getUVMeasurement(int analogValue){
    if(analogValue < MIN_SENSOR_VALUE) analogValue = MIN_SENSOR_VALUE;
    if(analogValue > MAX_SENSOR_VALUE) analogValue = MAX_SENSOR_VALUE;

    float uvIndex = MIN_UV_INDEX + (float)(analogValue - MIN_SENSOR_VALUE) / (MAX_SENSOR_VALUE - MIN_SENSOR_VALUE) * (MAX_UV_INDEX - MIN_UV_INDEX);
    
    return uvIndex;
}



// ========== NeoPixel Control ==========
void enableInternalPower(){
#if defined(NEOPIXEL_I2C_POWER)
  pinMode(NEOPIXEL_I2C_POWER, OUTPUT);
  digitalWrite(NEOPIXEL_I2C_POWER, HIGH);
#endif
}

void disableInternalPower(){
#if defined(NEOPIXEL_I2C_POWER)
  pinMode(NEOPIXEL_I2C_POWER, OUTPUT);
  digitalWrite(NEOPIXEL_I2C_POWER, LOW);
#endif
}

void setStatusLED(int r, int g, int b){
  pixel.setPixelColor(0, pixel.Color(r, g, b));
  pixel.show();
}

void fadeLED(int r, int g, int b, int steps = 100, int delayMs = 10){
  for(int i = 0; i <= steps; i++){
    pixel.setPixelColor(0, pixel.Color(r * i / steps, g * i / steps, b * i / steps));
    pixel.show();
    delay(delayMs);
  }
  for(int i = steps; i >= 0; i--){
    pixel.setPixelColor(0, pixel.Color(r * i / steps, g * i / steps, b * i / steps));
    pixel.show();
    delay(delayMs);
  }
}

void pulseLED(int r, int g, int b, int duration = 1000){
  int steps = 30;
  int delayMs = duration / (2 * steps);
  for(int i = 0; i <= steps; i++){
    pixel.setPixelColor(0, pixel.Color(r * i / steps, g * i / steps, b * i / steps));
    pixel.show();
    delay(delayMs);
  }
  for(int i = steps; i >= 0; i--){
    pixel.setPixelColor(0, pixel.Color(r * i / steps, g * i / steps, b * i / steps));
    pixel.show();
    delay(delayMs);
  }
}

void flashLED(int r, int g, int b, int duration = 100){
  setStatusLED(r, g, b);
  delay(duration);
  setStatusLED(0, 0, 0);
}




// ========== BLE Security ==========
class DeviceSecurity : public BLESecurityCallbacks{
  uint32_t onPassKeyRequest() override {
    Serial.println("🔐 Passkey requested...");
    return 123456;
  }

  void onPassKeyNotify(uint32_t pass_key) override{
    Serial.print("🔑 Passkey to display: ");
    Serial.println(pass_key);
  }

  bool onConfirmPIN(uint32_t pass_key) override{
    Serial.print("✅ Confirming passkey: ");
    Serial.println(pass_key);
    return true;
  }

  bool onSecurityRequest() override{
    return true;
  }

  void onAuthenticationComplete(esp_ble_auth_cmpl_t auth_cmpl) override{
    if (auth_cmpl.success) {
      Serial.println("🔓 Authentication succeeded...");
    } else {
      Serial.println("❌ Authentication failed...");
    }
  }
};




// ========== Server Callbacks ==========
class ServerCallbacks : public BLEServerCallbacks{
  void onConnect(BLEServer* pServer) override{
    deviceConnected = true;
    Serial.println("📱 Client connected...");
    setStatusLED(0, 255, 0); // Green
  }

  void onDisconnect(BLEServer* pServer) override{
    deviceConnected = false;
    Serial.println("📴 Client disconnected...");
    fadeLED(255, 0, 0, 100, 5); // Red fade
    BLEDevice::startAdvertising();
  }
};




// ========== Setup ==========
void setup(){
  Serial.begin(115200);
  delay(1000);
  Serial.println("🚀 Starting BLE with Status LED...");

  enableInternalPower();
  pixel.begin();
  pixel.setBrightness(50);

  fadeLED(0, 0, 255, 100, 5); // Blue fade animation

  BLEDevice::init("ESP32 Feather Status");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  BLEDevice::setSecurityCallbacks(new DeviceSecurity());
  BLESecurity* pSecurity = new BLESecurity();
  pSecurity->setCapability(ESP_IO_CAP_OUT);
  pSecurity->setAuthenticationMode(ESP_LE_AUTH_REQ_SC_MITM_BOND);
  pSecurity->setInitEncryptionKey(ESP_BLE_ENC_KEY_MASK | ESP_BLE_ID_KEY_MASK);

  BLEService* pService = pServer->createService(SERVICE_UUID);

  pNotifyChar = pService->createCharacteristic(
    CHAR_NOTIFY_UUID,
    BLECharacteristic::PROPERTY_NOTIFY
  );
  pNotifyChar->addDescriptor(new BLE2902());

  pService->start();

  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->start();

  Serial.println("📡 Advertising started...");
}




// ========== Loop ==========
void loop(){
  static int fakeSensorValue = 0;
  static unsigned long blinkTimer = 0;

  // Blink yellow while advertising (not connected)
  if(!deviceConnected) {
    if(millis() - blinkTimer > 1000){
      fadeLED(255, 255, 0, 100, 10); // Smooth yellow fade during advertising
      blinkTimer = millis();
    }
  }

  // Notify and flash cyan if connected
  if(deviceConnected){

    // --- 1. Read from UV Sensor ---
    int analogValue = analogRead(UV_SENSOR_PIN);
    Serial.print("Analog Value: ");
    Serial.println(analogValue);
    
    float uvIndex = getUVMeasurement(analogValue);

    char payload[10];
    sprintf(payload, "%.2f", uvIndex);


    pNotifyChar->setValue(payload);
    pNotifyChar->notify();

    Serial.print("📤 Sent: ");
    Serial.println(payload);

    flashLED(0, 255, 255, 100); // Cyan blink
    delay(900); // total loop delay ~1 sec
  } else {
    delay(100);
  }
}