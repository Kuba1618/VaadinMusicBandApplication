#include <WiFi.h>

// 🔒 Dane Twojego hotspotu (np. z telefonu)
const char* ssid = "ESP_TEST";       // <-- zmień na swoją nazwę sieci
const char* password = "12345678";   // <-- zmień na swoje hasło

const int redPin = 14;
const int greenPin = 13;
const int bluePin = 12;


// Zmienne do kontroli trybu
bool rainbowModeActive = false;
unsigned long lastColorChangeTime = 0;
int currentHue = 0; // Aktualna barwa dla efektu tęczy (0-360)
const int hueChangeSpeed = 5; // Jak szybko zmienia się barwa (im mniejsza wartość, tym wolniej)


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
// Funkcja konwertująca HSV na RGB
// H: 0-360, S: 0-1, V: 0-1
// R, G, B: 0-255
void hsvToRgb(float h, float s, float v, int &r, int &g, int &b) {
    int i;
    float f, p, q, t;
    if (s == 0) {
        // achromatic (grey)
        r = g = b = v * 255;
        return;
    }
    h /= 60;            // sector 0 to 5
    i = floor(h);
    f = h - i;          // factorial part of h
    p = v * (1 - s);
    q = v * (1 - s * f);
    t = v * (1 - s * (1 - f));
    switch (i) {
        case 0:
            r = v * 255;
            g = t * 255;
            b = p * 255;
            break;
        case 1:
            r = q * 255;
            g = v * 255;
            b = p * 255;
            break;
        case 2:
            r = p * 255;
            g = v * 255;
            b = t * 255;
            break;
        case 3:
            r = p * 255;
            g = q * 255;
            b = v * 255;
            break;
        case 4:
            r = t * 255;
            g = p * 255;
            b = v * 255;
            break;
        default:        // case 5
            r = v * 255;
            g = p * 255;
            b = q * 255;
            break;
    }
}

// Nowa funkcja do ustawiania koloru RGB z wartościami 0-255
void setRgbColor(int r, int g, int b) {
  analogWrite(redPin, r);
  analogWrite(greenPin, g);
  analogWrite(bluePin, b);
}

// Stara funkcja setColor, dostosowana do nowej setRgbColor
void setColor(bool r, bool g, bool b) {
  setRgbColor(r ? 255 : 0, g ? 255 : 0, b ? 255 : 0);
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

    // Zresetuj tryb tęczy, jeśli otrzymano inne polecenie
    if (req.indexOf("/rainbow") < 0) {
      rainbowModeActive = false;
    }

    if (req.indexOf("/red") > 0) setColor(true, false, false);
    else if (req.indexOf("/green") > 0) setColor(false, true, false);
    else if (req.indexOf("/blue") > 0) setColor(false, false, true);
    else if (req.indexOf("/white") > 0) setColor(true, true, true);
    else if (req.indexOf("/off") > 0) setColor(false, false, false);
    else if (req.indexOf("/rainbow") > 0) { // Nowy endpoint dla tęczy
      rainbowModeActive = true;
      Serial.println("Aktywowano tryb tęczy.");
    }

    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println();
    client.println("<h1>Sterowanie diodą RGB</h1>");
    client.println("<p><a href=\"/red\">🔴 Czerwony</a></p>");
    client.println("<p><a href=\"/green\">🟢 Zielony</a></p>");
    client.println("<p><a href=\"/blue\">🔵 Niebieski</a></p>");
    client.println("<p><a href=\"/white\">⚪ Biały</a></p>");
    client.println("<p><a href=\"/off\">⚫ Zgaś</a></p>");
    client.println("<p><a href=\"/rainbow\">🌈 Tęcza</a></p>"); // Nowy przycisk na stronie
    
    client.stop();
    Serial.println("Połączenie zakończone.");
  }

  // Obsługa trybu tęczy
  if (rainbowModeActive) {
    unsigned long currentTime = millis();
    if (currentTime - lastColorChangeTime >= hueChangeSpeed) { // Zmień kolor co X milisekund
      currentHue = (currentHue + 1) % 360; // Zwiększ barwę o 1, zawijając po 360
      int r, g, b;
      hsvToRgb(currentHue, 1.0, 1.0, r, g, b); // S=1.0, V=1.0 dla pełnej saturacji i jasności
      setRgbColor(r, g, b);
      lastColorChangeTime = currentTime;
    }
  }
}