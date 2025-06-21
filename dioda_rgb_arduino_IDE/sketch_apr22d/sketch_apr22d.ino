#include <WiFi.h>

// 🔒 Dane Twojego hotspotu (np. z telefonu)
const char* ssid = "ESP_TEST";       // <-- zmień na swoją nazwę sieci
const char* password = "12345678";   // <-- zmień na swoje hasło

const int redPin = 14;
const int greenPin = 13;
const int bluePin = 12;

WiFiServer server(80);

void setup() {
  Serial.begin(115200);

  // 🔍 Skanowanie dostępnych sieci
  Serial.println("Skanuję dostępne sieci WiFi...");
  int n = WiFi.scanNetworks();
  if (n == 0) {
    Serial.println("Nie znaleziono żadnych sieci.");
  } else {
    Serial.println("Znalezione sieci:");
    for (int i = 0; i < n; ++i) {
      Serial.println(WiFi.SSID(i));
    }
  }

  // 🟢 Ustawienie pinów jako wyjścia
  pinMode(redPin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(bluePin, OUTPUT);

  // 🌐 Próba połączenia z WiFi
  WiFi.begin(ssid, password);
  Serial.print("Łączenie z WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nPołączono z WiFi. IP:");
  Serial.println(WiFi.localIP());

  server.begin();
}

void setColor(bool r, bool g, bool b) {
  digitalWrite(redPin, r ? HIGH : LOW);
  digitalWrite(greenPin, g ? HIGH : LOW);
  digitalWrite(bluePin, b ? HIGH : LOW);
}

void loop() {
  WiFiClient client = server.available();
  if (client) {
    Serial.println("Nowy klient.");
    String req = "";

    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        req += c;
        if (c == '\n') break;
      }
    }

    if (req.indexOf("/red") > 0) setColor(true, false, false);
    else if (req.indexOf("/green") > 0) setColor(false, true, false);
    else if (req.indexOf("/blue") > 0) setColor(false, false, true);
    else if (req.indexOf("/white") > 0) setColor(true, true, true);
    else if (req.indexOf("/off") > 0) setColor(false, false, false);

    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println();
    client.println("<h1>Sterowanie diodą RGB</h1>");
    client.println("<p><a href=\"/red\">🔴 Czerwony</a></p>");
    client.println("<p><a href=\"/green\">🟢 Zielony</a></p>");
    client.println("<p><a href=\"/blue\">🔵 Niebieski</a></p>");
    client.println("<p><a href=\"/white\">⚪ Biały</a></p>");
    client.println("<p><a href=\"/off\">⚫ Zgaś</a></p>");
    
    client.stop();
    Serial.println("Połączenie zakończone.");
  }
}
