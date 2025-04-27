package com.example.application.lights;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.addons.tatu.ColorPicker;

import java.util.Arrays;

@PageTitle("LightsView")
@Route("lights")
@Menu(order = 3, icon = "line-awesome/svg/lightbulb.svg")
@RolesAllowed("ADMIN")


public class LightsView extends VerticalLayout {

    public LightsView() {
        // Picker kolorów
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setLabel("Color");
        colorPicker.setPresets(Arrays.asList(
                new ColorPicker.ColorPreset("#00ff00", "Color 1"),
                new ColorPicker.ColorPreset("#ff0000", "Color 2")
        ));

        // Pole z kodem HEX
        TextField hexField = new TextField("Selected HEX");
        hexField.setReadOnly(true);

        // Pole z wartością RGB
        TextField rgbField = new TextField("Selected RGB");
        rgbField.setReadOnly(true);

        // Podgląd koloru
        Div colorPreview = new Div();
        colorPreview.setWidth("100px");
        colorPreview.setHeight("30px");
        colorPreview.getStyle().set("border", "1px solid #ccc");

        // Listener zmiany koloru
        colorPicker.addValueChangeListener(event -> {
            String hex = event.getValue(); // np. "#ff0000"

            hexField.setValue(hex.toUpperCase());

            // Konwersja HEX -> RGB
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            String rgb = "rgb(" + r + ", " + g + ", " + b + ")";
            rgbField.setValue(rgb);

            // Podgląd
            colorPreview.getStyle().set("background-color", hex);

            Notification.show("Wybrano kolor: " + hex + " / " + rgb);
        });

        // Dodajemy do layoutu
        add(colorPicker, hexField, rgbField, colorPreview);
    }
}
