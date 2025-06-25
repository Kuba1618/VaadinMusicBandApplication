package com.example.application.lights;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@PageTitle("LightsView")
@Route("lights")
@Menu(order = 3, icon = "line-awesome/svg/lightbulb.svg")
@RolesAllowed({"USER", "ADMIN"})
public class LightsView extends VerticalLayout {

    private final String esp32Ip = "192.168.51.33"; // <-- Upewnij się, że to prawidłowy IP Twojego ESP32

    public LightsView() {
        setAlignItems(Alignment.CENTER);

        // Przyciski sterujące kolorami
        HorizontalLayout colorButtonsLayout = new HorizontalLayout();
        colorButtonsLayout.add(
                new Button("🔴 Czerwony", e -> sendColorCommand("/red")),
                new Button("🟢 Zielony", e -> sendColorCommand("/green")),
                new Button("🔵 Niebieski", e -> sendColorCommand("/blue")),
                new Button("⚪ Biały", e -> sendColorCommand("/white")),
                new Button("⚫ Zgaś", e -> sendColorCommand("/off"))
        );
        add(colorButtonsLayout);

        // Przyciski sterujące trybami animacji
        HorizontalLayout modeButtonsLayout = new HorizontalLayout();
        modeButtonsLayout.add(
                new Button("🌈 Tęcza", e -> sendColorCommand("/rainbow")),
                new Button("⚡ Stroboskop (biały)", e -> sendColorCommand("/strobe")), // Zmieniona etykieta
                new Button("⚡ Stroboskop (kolorowy)", e -> sendColorCommand("/colorStrobe")), // NOWY PRZYCISK
                new Button("🎨 Krzywa Beziera (3 st.)", e -> sendColorCommand("/bezier"))
        );
        add(modeButtonsLayout);

        // Pole numeryczne do regulacji szybkości stroboskopu (białego)
        add(new H3("Szybkość Stroboskopu (białego, ms)"));
        IntegerField strobeSpeedField = new IntegerField();
        strobeSpeedField.setLabel("Interwał migania");
        strobeSpeedField.setMin(10);
        strobeSpeedField.setMax(1000);
        strobeSpeedField.setValue(100);
        strobeSpeedField.setStep(10);
        strobeSpeedField.setHelperText("Większa wartość = wolniejsze miganie");

        strobeSpeedField.addValueChangeListener(event -> {
            Integer interval = event.getValue();
            if (interval != null) {
                new Thread(() -> {
                    sendColorCommand("/setStrobeInterval/" + interval);
                }).start();
            }
        });
        add(strobeSpeedField);

        // NOWE POLE NUMERYCZNE DO REGULACJI SZYBKOŚCI STROBOSKOPU (KOLOROWEGO)
        add(new H3("Szybkość Stroboskopu (kolorowego, ms)"));
        IntegerField colorStrobeSpeedField = new IntegerField();
        colorStrobeSpeedField.setLabel("Interwał zmiany koloru");
        colorStrobeSpeedField.setMin(10);
        colorStrobeSpeedField.setMax(1000);
        colorStrobeSpeedField.setValue(150); // Domyślna wartość
        colorStrobeSpeedField.setStep(10);
        colorStrobeSpeedField.setHelperText("Większa wartość = wolniejsza zmiana koloru");

        colorStrobeSpeedField.addValueChangeListener(event -> {
            Integer interval = event.getValue();
            if (interval != null) {
                new Thread(() -> {
                    sendColorCommand("/setColorStrobeInterval/" + interval); // NOWY ENDPOINT
                }).start();
            }
        });
        add(colorStrobeSpeedField);


        // Przycisk do pobierania danych o opóźnieniach
        Button getDelayDataButton = new Button("📊 Pobierz dane", e -> {
            new Thread(this::getDelayData).start();
        });
        add(getDelayDataButton);
    }

    private void sendColorCommand(String colorPath) {
        try {
            URL url = new URL("http://" + esp32Ip + colorPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Notification.show("Wysłano komendę: " + colorPath, 1000, Notification.Position.BOTTOM_END);
            } else {
                Notification.show("Błąd wysyłania komendy. Kod: " + responseCode, 3000, Notification.Position.BOTTOM_END);
            }
        } catch (IOException e) {
            Notification.show("Nie można połączyć się z ESP32: " + e.getMessage(), 3000, Notification.Position.BOTTOM_END);
        }
    }

    private void getDelayData() {
        try {
            URL url = new URL("http://" + esp32Ip + "/getDelayData");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String responseBody = in.lines().collect(Collectors.joining("\n"));
                in.close();

                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification notification = Notification.show("Dane o opóźnieniach:\n" + responseBody, 5000, Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                    notification.open();
                }));
            } else {
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Błąd pobierania danych. Kod: " + responseCode, 3000, Notification.Position.BOTTOM_END);
                }));
            }
        } catch (IOException e) {
            getUI().ifPresent(ui -> ui.access(() -> {
                Notification.show("Błąd połączenia podczas pobierania danych: " + e.getMessage(), 3000, Notification.Position.BOTTOM_END);
            }));
        }
    }
}