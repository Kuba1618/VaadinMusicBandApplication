package com.example.application.lights;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@PageTitle("LightsView")
@Route("lights")
@Menu(order = 3, icon = "line-awesome/svg/lightbulb.svg")
@RolesAllowed({"USER", "ADMIN"})
public class LightsView extends VerticalLayout {

    // IP Twojego ESP32
    private final String esp32Ip = "192.168.45.33";

    public LightsView() {
        setAlignItems(Alignment.CENTER);

        Button redButton = new Button("🔴 Czerwony", e -> sendColorCommand("/red"));
        Button greenButton = new Button("🟢 Zielony", e -> sendColorCommand("/green"));
        Button blueButton = new Button("🔵 Niebieski", e -> sendColorCommand("/blue"));
        Button whiteButton = new Button("⚪ Biały", e -> sendColorCommand("/white"));
        Button offButton = new Button("⚫ Zgaś", e -> sendColorCommand("/off"));

        add(redButton, greenButton, blueButton, whiteButton, offButton);
    }

    private void sendColorCommand(String colorPath) {
        try {
            URL url = new URL("http://" + esp32Ip + colorPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Notification.show("Kolor zmieniony: " + colorPath);
            } else {
                Notification.show("Błąd zmiany koloru. Kod: " + responseCode);
            }
        } catch (IOException e) {
            Notification.show("Nie można połączyć się z ESP32: " + e.getMessage());
        }
    }
}