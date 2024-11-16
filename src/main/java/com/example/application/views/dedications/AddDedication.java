package com.example.application.views.dedications;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@PageTitle("Add Dedication")
@Route("add-dedication")
@Menu(order = 1, icon = "line-awesome/svg/heart-solid.svg")
@RolesAllowed("ADMIN")
public class AddDedication extends VerticalLayout {

    public Set<String> categories;
    public ComboBox<String> songCategoryCmbBox,songCmbBox;
    public Button saveBtn;
    public TextArea textArea;
    public VerticalLayout layoutColumn2 = new VerticalLayout();
    private static List<Dedication> listOfDedications = new ArrayList<>();
    private static Grid<Dedication> grid = new Grid<>();
    private static Div hint = new Div();
    public int width;
    public int height;
    public AddDedication(){

        width = isMobileDevice() ? 98 : 80;

        layoutColumn2.setWidthFull();
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layoutColumn2.setAlignItems(FlexComponent.Alignment.CENTER);

        createDynamicGrid();

        add(layoutColumn2);
    }
    public void createDynamicGrid(){
        this.setupGrid();
        this.createAddDedicationForm();
        this.refreshGrid();
    }
    public void createAddDedicationForm(){
        songCategoryCmbBox = new ComboBox<>("Song Category");
        songCategoryCmbBox.setWidth(width + "%");
        songCategoryCmbBox.setHeight("15%");
        songCategoryCmbBox.setItems(loadSongCategories());
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, songCategoryCmbBox);
        layoutColumn2.add(songCategoryCmbBox);

        songCategoryCmbBox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>>) event -> {
            Set<String> songsToCmbBox = loadSongTitlesBasedOnCategorie(songCategoryCmbBox.getValue());
            songCmbBox.setItems(songsToCmbBox);
        });

        songCmbBox = new ComboBox<>("Song Title");
        songCmbBox.setWidth(width + "%");
        songCmbBox.setHeight("15%");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, songCmbBox);
        layoutColumn2.add(songCmbBox);

        songCmbBox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>>) comboBoxStringComponentValueChangeEvent -> System.out.println(songCmbBox.getValue()));

        textArea = new TextArea("Description");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, textArea);
        textArea.setWidth(width + "%");
        textArea.setHeight("65%");
        layoutColumn2.add(textArea);

        saveBtn = new Button("Save");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, saveBtn);
        saveBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            saveDedication(new Dedication(songCategoryCmbBox.getValue(), songCmbBox.getValue(),textArea.getValue()));

            songCategoryCmbBox.setValue(null);
            songCmbBox.setValue(null);
            textArea.clear();
        });
        layoutColumn2.add(saveBtn);
    }

    private void setupGrid() {

        grid = new Grid<>(Dedication.class, false);
        grid.setWidth("100%");
        grid.setAllRowsVisible(true);
        //grid.addColumn(Dedication::getCategory).setHeader("Category").setAutoWidth(true);
        grid.addColumn(dedication -> String.join(", ", dedication.getCategory()))
                .setHeader("Category")
                .setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(Dedication::getTitle).setHeader("Title").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Dedication::getDescription).setHeader("Description").setAutoWidth(true).setFlexGrow(9);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, song) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_TERTIARY,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> this.shareDedicationSong(song));
                    button.setIcon(new Icon(VaadinIcon.SHARE));
                })).setHeader("Share").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, song) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> this.removeDedication(song));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setHeader("Delete").setAutoWidth(true).setFlexGrow(0);

        listOfDedications = loadDataFromFile();
        grid.setItems(listOfDedications);
        grid.setWidth(width + "%");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, grid);
        layoutColumn2.add(grid);
    }

    private List<Dedication> loadDataFromFile() {
        List<Dedication> dedications = new ArrayList<>();
        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "dedications").toString();
        Path dedicationsPath = Paths.get(resourcesPath, "dedications.txt");

        try {
            // Wczytanie wszystkich linii z pliku
            List<String> lines = Files.readAllLines(dedicationsPath);

            // Przechodzenie przez linie i tworzenie dedykacji
            for (int i = 0; i < lines.size(); i += 4) {
                String title = lines.get(i).trim(); // Pierwsza linia to tytuł
                String category = lines.get(i + 1).trim(); // Druga linia to kategoria
                String description = lines.get(i + 2).trim(); // Trzecia linia to opis

                // Tworzenie obiektu Dedication i dodanie go do listy
                Dedication dedication = new Dedication(title,category, description);
                dedications.add(dedication);
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return dedications;
    }

    private void shareDedicationSong(Dedication dedication) {
        Notification.show(dedication.getTitle() + " shared!");
    }

    public Set<String> loadSongTitlesBasedOnCategorie(String songCategory) {
        Set<String> songs = new HashSet<>();

        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        try (Scanner scanner = new Scanner(libraryPath)) {
            while (scanner.hasNextLine()) {
                // Wczytujemy ID
                String id = scanner.nextLine().trim();
                if (!scanner.hasNextLine()) break;

                // Wczytujemy tytuł
                String title = scanner.nextLine().trim();
                if (!scanner.hasNextLine()) break;

                // Wczytujemy autora
                String author = scanner.nextLine().trim();
                if (!scanner.hasNextLine()) break;

                // Wczytujemy kategorię
                String category = scanner.nextLine().trim();

                // Wczytujemy tonację lub separator
                if (scanner.hasNextLine()) {
                    String tonationOrSeparator = scanner.nextLine().trim();
                    if (tonationOrSeparator.equals("----------------------")) {
                        // Jeśli kategoria pasuje, dodaj tytuł do listy
                        if (category.equals(songCategory)) {
                            songs.add(title);
                        }
                    } else {
                        // Błąd - oczekiwano separatora, ale znaleziono coś innego
                        System.err.println("Błąd: Oczekiwano separatora, ale znaleziono: " + tonationOrSeparator);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return songs;
    }





    private void refreshGrid() {
        if (listOfDedications.size() > 0) {
            grid.setVisible(true);
            hint.setVisible(false);
            grid.getDataProvider().refreshAll();
        } else {
            grid.setVisible(false);
            hint.setVisible(true);
        }
    }

    private void saveDedication(Dedication dedication) {
        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "dedications").toString();
        Path libraryPath = Paths.get(resourcesPath, "dedications.txt");

        String dedicationDescription = dedication.toString().trim();

        try {
            if (Files.exists(libraryPath)) {

                List<String> lines = Files.readAllLines(libraryPath);

                if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                    // Jeśli ostatnia linia pliku nie jest pusta, dodaj nową linię przed dedykacją
                    dedicationDescription = System.lineSeparator() + dedicationDescription;
                }
            }

            // Dodanie tekstu na końcu pliku dedications.txtD
            Files.write(libraryPath, dedicationDescription.getBytes(), StandardOpenOption.APPEND);
            Notification notification = new Notification("Dedication saved !", NotificationVariant.LUMO_PRIMARY.ordinal());

        } catch (IOException e) {
            System.err.println("Wystąpił błąd podczas dodawania tekstu do pliku: " + e.getMessage());
        }

        listOfDedications.add(dedication);
        this.refreshGrid();
    }


    private void removeDedication(Dedication dedication) {
        listOfDedications.remove(dedication);

        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "dedications").toString();
        Path dedicationsPath = Paths.get(resourcesPath, "dedications.txt");

        try {
            // Wczytanie wszystkich linii z pliku
            List<String> lines = Files.readAllLines(dedicationsPath);
            List<String> updatedLines = new ArrayList<>();
            boolean skipNextLines = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                if (skipNextLines) {
                    // Pomiń kolejne 4 linie (tytuł, kategoria, opis, separator)
                    i += 3; // Pomiń 3 linie, a separator zostanie pominięty automatycznie w następnym obrocie
                    skipNextLines = false;
                    continue;
                }

                // Sprawdź, czy linia nie jest separatorem
                if (!line.equals("----------------------") && i + 2 < lines.size()) {
                    String title = lines.get(i).trim();
                    String category = lines.get(i + 1).trim();
                    String description = lines.get(i + 2).trim();

                    // Porównaj tytuł, kategorię i opis z usuwaną dedykacją (ignorując białe znaki)
                    if (title.equalsIgnoreCase(dedication.getTitle().trim()) &&
                            category.equalsIgnoreCase(dedication.getCategory().trim()) &&
                            description.equalsIgnoreCase(dedication.getDescription().trim())) {
                        skipNextLines = true; // Ustaw flagę na pominięcie następnych linii
                        continue; // Pomiń dodawanie tej dedykacji i separatora
                    }
                }

                // Dodaj bieżącą linię do zaktualizowanej listy
                updatedLines.add(line);
            }

            // Zapisz zaktualizowane linie do pliku
            Files.write(dedicationsPath, updatedLines);

        } catch (IOException e) {
            System.err.println("Błąd podczas aktualizacji pliku: " + e.getMessage());
        }

        this.refreshGrid();
    }

    public Set<String> loadSongCategories() {
        Set<String> categories = new HashSet<>();

        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        try (Scanner scanner = new Scanner(libraryPath)) {
            while (scanner.hasNextLine()) {
                // Ignorowanie ID
                String id = scanner.nextLine().trim();
                if (!scanner.hasNextLine()) break;

                // Ignorowanie tytułu
                String title = scanner.nextLine().trim();
                if (!scanner.hasNextLine()) break;

                // Ignorowanie autora
                String author = scanner.nextLine().trim();
                if (!scanner.hasNextLine()) break;

                // Pobieranie kategorii
                String category = scanner.nextLine().trim();

                // Pominięcie separatora
                if (scanner.hasNextLine()) {
                    String separator = scanner.nextLine().trim();
                    if (separator.equals("----------------------")) {
                        categories.add(category); // Dodaj kategorię do zbioru
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return categories;
    }

    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }
}
