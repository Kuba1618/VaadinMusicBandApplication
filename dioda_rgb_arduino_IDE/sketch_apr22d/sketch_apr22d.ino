#include <WiFi.h>

// ?? Dane Twojego hotspotu (np. z telefonu)
const char* ssid = "ESP_TEST";       // <-- zmie里 na swoj? nazw? sieci
const char* password = "12345678";   // <-- zmie里 na swoje has?o

const int redPin = 14;
const int greenPin = 13;
const int bluePin = 12;

// Korekcja gamma dla percepcji jasno?ci
const float GAMMA_CORRECTION_FACTOR = 2.8; // Typowa warto?? dla LED車w (2.2 do 2.8)

// Zmienne do kontroli trybu t?czy
bool rainbowModeActive = false;
unsigned long lastColorChangeTime = 0;
int currentHue = 0; // Aktualna barwa dla efektu t?czy (0-360)
const int hueChangeSpeed = 5; // Jak szybko zmienia si? barwa (im mniejsza warto??, tym wolniej)

// Zmienne do kontroli trybu stroboskopu (bia?ego)
bool strobeModeActive = false;
unsigned long lastStrobeChangeTime = 0;
int strobeInterval = 100; // Czas w milisekundach pomi?dzy w??czeniem/wy??czeniem diody (50ms on, 50ms off dla 100ms interval)
bool strobeOn = false; // Czy dioda jest obecnie w??czona w trybie stroboskopu

// Zmienne do kontroli trybu Beziera
bool bezierModeActive = false;
unsigned long bezierStartTime = 0;
const unsigned long bezierDuration = 5000; // Czas trwania jednej pe?nej animacji Beziera w milisekach (np. 5 sekund)
// Punkty kontrolne dla Hue (barwy) dla krzywej Beziera trzeciego stopnia
float bezierP0 = 0.0;   // Start Hue (np. czerwony)
float bezierP1 = 90.0;  // Kontrolny punkt 1 (np. ?車?ty/zielony)
float bezierP2 = 180.0; // Kontrolny punkt 2 (np. zielony/cyjan)
float bezierP3 = 270.0; // End Hue (np. niebieski/purpurowy)

// NOWE ZMIENNE DLA KOLOROWEGO STROBOSKOPU
bool colorStrobeModeActive = false;
unsigned long lastColorStrobeChangeTime = 0;
int colorStrobeInterval = 150; // Domy?lny interwa? dla kolorowego stroboskopu (ms)
int currentColorStrobeIndex = 0; // Indeks aktualnego koloru w sekwencji
// Definiowanie kolor車w dla stroboskopu (RGB)
const int NUM_STROBE_COLORS = 3;
const int strobeColors[NUM_STROBE_COLORS][3] = {
  {255, 0, 0},   // Czerwony
  {0, 255, 0},   // Zielony
  {0, 0, 255}    // Niebieski
};


// Zmienne do pomiaru op車?nie里
const int MAX_DELAY_MEASUREMENTS = 20; // Ile ostatnich pomiar車w przechowujemy
long delayMeasurements[MAX_DELAY_MEASUREMENTS];
int measurementIndex = 0; // Aktualny indeks w buforze
bool measuringDelay = false; // Flaga, czy obecnie mierzymy op車?nienie
unsigned long commandReceiveTime = 0; // Czas odebrania komendy HTTP

WiFiServer server(80);

void setup() {
  Serial.begin(115200);

  // ?? Skanowanie dost?pnych sieci
  Serial.println("Skanuj? dost?pne sieci WiFi...");
  int n = WiFi.scanNetworks();
  if (n == 0) {
    Serial.println("Nie znaleziono ?adnych sieci.");
  } else {
    Serial.println("Znalezione sieci:");
    for (int i = 0; i < n; ++i) {
      Serial.println(WiFi.SSID(i));
    }
  }

  // ?? Ustawienie pin車w jako wyj?cia
  pinMode(redPin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(bluePin, OUTPUT);

  // ?? Pr車ba po??czenia z WiFi
  WiFi.begin(ssid, password);
  Serial.print("??czenie z WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nPo??czono z WiFi. IP:");
  Serial.println(WiFi.localIP());

  server.begin();

  // Inicjalizacja tablicy pomiar車w zerami
  for (int i = 0; i < MAX_DELAY_MEASUREMENTS; i++) {
    delayMeasurements[i] = 0;
  }
}

// Funkcja konwertuj?ca HSV na RGB
// H: 0-360, S: 0-1, V: 0-1
// R, G, B: 0-255
void hsvToRgb(float h, float s, float v, int &r, int &g, int &b) {
    int i;
    float f, p, q, t_hsv;
    if (s == 0) {
        r = g = b = v * 255;
        return;
    }
    h /= 60;
    i = floor(h);
    f = h - i;
    p = v * (1 - s);
    q = v * (1 - s * f);
    t_hsv = v * (1 - s * (1 - f));
    switch (i) {
        case 0: r = v * 255; g = t_hsv * 255; b = p * 255; break;
        case 1: r = q * 255; g = v * 255; b = p * 255; break;
        case 2: r = p * 255; g = v * 255; b = t_hsv * 255; break;
        case 3: r = p * 255; g = q * 255; b = v * 255; break;
        case 4: r = t_hsv * 255; g = p * 255; b = v * 255; break;
        default: r = v * 255; g = p * 255; b = q * 255; break;
    }
}

// NOWA FUNKCJA: Oblicza punkt na krzywej Beziera trzeciego stopnia
float bezierCurveCubic(float p0, float p1, float p2, float p3, float t) {
  float omt = 1.0 - t; // (1 - t)
  float omt2 = omt * omt; // (1 - t)^2
  float omt3 = omt2 * omt; // (1 - t)^3

  float t2 = t * t; // t^2
  float t3 = t2 * t; // t^3

  return omt3 * p0 + 3.0 * omt2 * t * p1 + 3.0 * omt * t2 * p2 + t3 * p3;
}

// NOWA FUNKCJA: Korekcja gamma
// Input value is 0-255. Returns gamma corrected value 0-255.
int applyGammaCorrection(int value) {
    // Przekszta?camy warto?? na zakres 0.0-1.0
    float normalizedValue = (float)value / 255.0;
    // Stosujemy pot?g? (korekcj? gamma)
    float correctedNormalizedValue = pow(normalizedValue, GAMMA_CORRECTION_FACTOR);
    // Przekszta?camy z powrotem na zakres 0-255
    return (int)(correctedNormalizedValue * 255.0 + 0.5); // Dodanie 0.5 i rzutowanie dla zaokr?glenia
}

// Funkcja do ustawiania koloru RGB z warto?ciami 0-255
void setRgbColor(int r, int g, int b) {
  // Zastosowanie korekcji gamma dla ka?dej sk?adowej koloru
  analogWrite(redPin, applyGammaCorrection(r));
  analogWrite(greenPin, applyGammaCorrection(g));
  analogWrite(bluePin, applyGammaCorrection(b));

  // Je?li trwa pomiar op車?nienia i w?a?nie zmienili?my kolor, zapisz op車?nienie
  if (measuringDelay) {
    long delayMs = millis() - commandReceiveTime;
    delayMeasurements[measurementIndex] = delayMs;
    measurementIndex = (measurementIndex + 1) % MAX_DELAY_MEASUREMENTS; // Przejd? do nast?pnego indeksu, zawijaj?c
    measuringDelay = false; // Zako里cz pomiar dla tego cyklu
    Serial.print("Zmierzono op車?nienie: ");
    Serial.println(delayMs);
  }
}

// Funkcja setColor u?ywa teraz setRgbColor z korekcj? gamma
void setColor(bool r, bool g, bool b) {
  setRgbColor(r ? 255 : 0, g ? 255 : 0, b ? 255 : 0);
}

void loop() {
  WiFiClient client = server.available();
  if (client) {
    Serial.println("Nowy klient.");
    String req = "";

    // *** Zapisz czas odebrania komendy ***
    commandReceiveTime = millis(); // Zapisz czas tu? po odebraniu klienta

    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        req += c;
        if (c == '\n') break;
      }
    }
    
    // Zresetuj flag? pomiaru op車?nie里 przed sprawdzeniem komend
    measuringDelay = false;

    // Wy??cz wszystkie tryby animacji, je?li odebrano komend? sta?ego koloru
    if (req.indexOf("/red") > 0 || req.indexOf("/green") > 0 || req.indexOf("/blue") > 0 ||
        req.indexOf("/white") > 0 || req.indexOf("/off") > 0) {
      rainbowModeActive = false;
      strobeModeActive = false;
      bezierModeActive = false;
      colorStrobeModeActive = false; // Wy??cz kolorowy stroboskop
      measuringDelay = true; // Aktywuj pomiar dla tej komendy sta?ego koloru
    }

    if (req.indexOf("/red") > 0) {
      setColor(true, false, false);
    }
    else if (req.indexOf("/green") > 0) {
      setColor(false, true, false);
    }
    else if (req.indexOf("/blue") > 0) {
      setColor(false, false, true);
    }
    else if (req.indexOf("/white") > 0) {
      setColor(true, true, true);
    }
    else if (req.indexOf("/off") > 0) {
      setColor(false, false, false);
    }
    else if (req.indexOf("/rainbow") > 0) {
      rainbowModeActive = true;
      strobeModeActive = false;
      bezierModeActive = false;
      colorStrobeModeActive = false; // Wy??cz kolorowy stroboskop
      Serial.println("Aktywowano tryb t?czy.");
    }
    else if (req.indexOf("/strobe") > 0) { // Bia?y stroboskop
      strobeModeActive = true;
      rainbowModeActive = false;
      bezierModeActive = false;
      colorStrobeModeActive = false; // Wy??cz kolorowy stroboskop
      strobeOn = false;
      Serial.println("Aktywowano tryb stroboskopu (bia?ego).");
    }
    else if (req.indexOf("/colorStrobe") > 0) { // NOWY ENDPOINT DLA KOLOROWEGO STROBOSKOPU
      colorStrobeModeActive = true;
      rainbowModeActive = false;
      strobeModeActive = false;
      bezierModeActive = false;
      currentColorStrobeIndex = 0; // Zawsze zaczynamy od pierwszego koloru
      Serial.println("Aktywowano tryb stroboskopu (kolorowego).");
    }
    else if (req.indexOf("/bezier") > 0) {
      bezierModeActive = true;
      rainbowModeActive = false;
      strobeModeActive = false;
      colorStrobeModeActive = false; // Wy??cz kolorowy stroboskop
      bezierStartTime = millis();
      Serial.println("Aktywowano tryb Beziera (3 stopnia).");
    }
    else if (req.indexOf("/setStrobeInterval/") > 0) {
      int intervalStartIndex = req.indexOf("/setStrobeInterval/") + strlen("/setStrobeInterval/");
      int intervalEndIndex = req.indexOf(" ", intervalStartIndex);
      String intervalStr = req.substring(intervalStartIndex, intervalEndIndex);
      int newInterval = intervalStr.toInt();

      if (newInterval >= 10 && newInterval <= 1000) {
        strobeInterval = newInterval;
        Serial.print("Ustawiono interwa? stroboskopu (bia?ego) na: ");
        Serial.println(strobeInterval);
      } else {
        Serial.println("Nieprawid?owy interwa? stroboskopu (bia?ego). Zakres: 10-1000.");
      }
    }
    // NOWA OBS?UGA ZMIANY INTERWA?U KOLOROWEGO STROBOSKOPU
    else if (req.indexOf("/setColorStrobeInterval/") > 0) {
      int intervalStartIndex = req.indexOf("/setColorStrobeInterval/") + strlen("/setColorStrobeInterval/");
      int intervalEndIndex = req.indexOf(" ", intervalStartIndex);
      String intervalStr = req.substring(intervalStartIndex, intervalEndIndex);
      int newInterval = intervalStr.toInt();

      if (newInterval >= 10 && newInterval <= 1000) {
        colorStrobeInterval = newInterval;
        Serial.print("Ustawiono interwa? stroboskopu (kolorowego) na: ");
        Serial.println(colorStrobeInterval);
      } else {
        Serial.println("Nieprawid?owy interwa? stroboskopu (kolorowego). Zakres: 10-1000.");
      }
    }
    // Endpoint do pobierania danych o op車?nieniach
    else if (req.indexOf("/getDelayData") > 0) {
      Serial.println("Wys?anie danych o op車?nieniach.");
      client.println("HTTP/1.1 200 OK");
      client.println("Content-Type: text/plain");
      client.println();

      for (int i = 0; i < MAX_DELAY_MEASUREMENTS; i++) {
        client.println(delayMeasurements[i]);
      }
      client.stop();
      Serial.println("Po??czenie zako里czone.");
      return;
    }


    // Domy?lna strona HTML dla innych ??da里
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println();
    client.println("<h1>Sterowanie diod? RGB</h1>");
    client.println("<p><a href=\"/red\">?? Czerwony</a></p>");
    client.println("<p><a href=\"/green\">?? Zielony</a></p>");
    client.println("<p><a href=\"/blue\">?? Niebieski</a></p>");
    client.println("<p><a href=\"/white\">? Bia?y</a></p>");
    client.println("<p><a href=\"/off\">? Zga?</a></p>");
    client.println("<p><a href=\"/rainbow\">?? T?cza</a></p>");
    client.println("<p><a href=\"/strobe\">? Stroboskop (bia?y)</a></p>");
    client.println("<p><a href=\"/colorStrobe\">? Stroboskop (kolorowy)</a></p>"); // NOWY PRZYCISK
    client.println("<p><a href=\"/bezier\">?? Krzywa Beziera (3 st.)</a></p>");
    client.println("<p><a href=\"/getDelayData\">?? Pobierz dane o op車?nieniach</a></p>");
    
    client.stop();
    Serial.println("Po??czenie zako里czone.");
  }

  // Obs?uga tryb車w animacji
  if (rainbowModeActive) {
    unsigned long currentTime = millis();
    if (currentTime - lastColorChangeTime >= hueChangeSpeed) {
      currentHue = (currentHue + 1) % 360;
      int r, g, b;
      hsvToRgb(currentHue, 1.0, 1.0, r, g, b);
      setRgbColor(r, g, b);
      lastColorChangeTime = currentTime;
    }
  }

  if (strobeModeActive) { // Bia?y stroboskop
    unsigned long currentTime = millis();
    if (currentTime - lastStrobeChangeTime >= strobeInterval) {
      strobeOn = !strobeOn;
      if (strobeOn) {
        setRgbColor(255, 255, 255);
      } else {
        setRgbColor(0, 0, 0);
      }
      lastStrobeChangeTime = currentTime;
    }
  }

  if (colorStrobeModeActive) { // NOWY TRYB: Kolorowy stroboskop
    unsigned long currentTime = millis();
    if (currentTime - lastColorStrobeChangeTime >= colorStrobeInterval) {
      currentColorStrobeIndex = (currentColorStrobeIndex + 1) % NUM_STROBE_COLORS;
      int r = strobeColors[currentColorStrobeIndex][0];
      int g = strobeColors[currentColorStrobeIndex][1];
      int b = strobeColors[currentColorStrobeIndex][2];
      setRgbColor(r, g, b);
      lastColorStrobeChangeTime = currentTime;
      Serial.print("Kolorowy stroboskop: ");
      Serial.print(r); Serial.print(","); Serial.print(g); Serial.print(","); Serial.println(b);
    }
  }

  if (bezierModeActive) {
    unsigned long currentTime = millis();
    unsigned long elapsedMillis = currentTime - bezierStartTime;
    float t = (float)elapsedMillis / bezierDuration;
    
    if (t >= 1.0) {
        bezierStartTime = currentTime;
        t = fmod(t, 1.0);
        Serial.println("Bezier cycle reset.");
    }

    float interpolatedHue = bezierCurveCubic(bezierP0, bezierP1, bezierP2, bezierP3, t);
    
    int r, g, b;
    hsvToRgb(interpolatedHue, 1.0, 1.0, r, g, b);
    setRgbColor(r, g, b);

    Serial.print("Bezier: t=");
    Serial.print(t, 4);
    Serial.print(", Hue=");
    Serial.print(interpolatedHue);
    Serial.print(", RGB=(");
    Serial.print(r); Serial.print(","); Serial.print(g); Serial.print(","); Serial.print(b);
    Serial.println(")");
  }
}