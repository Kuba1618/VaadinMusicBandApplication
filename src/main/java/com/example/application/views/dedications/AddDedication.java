package com.example.application.views.dedications;

import com.example.application.views.liveview.LiveView;
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
    public Set<String> songs;
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
        try {
            System.out.println(loadSongIdByTitle(dedication.getTitle()));
            LiveView.setLiveSongTitle(loadSongIdByTitle(dedication.getTitle()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String loadSongIdByTitle(String songTitle) throws IOException {
        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");
        // Wczytujemy wszystkie linie z pliku
        var lines = Files.readAllLines(libraryPath);

        // Iterujemy po liniach, w których każda grupa to 5 linii (ID, Tytuł, Wykonawca, Kategoria, Opis)
        for (int i = 0; i < lines.size(); i++) {
            // Pomijamy puste linie i separator
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.matches("-+")) {
                continue;
            }

            // Sprawdzamy, czy jest wystarczająco dużo linii, aby porównać tytuł
            if (i + 1 < lines.size()) {
                // Tytuł piosenki znajduje się w linii po ID (linia 2, czyli i+1)
                String title = lines.get(i + 1).trim(); // Tytuł piosenki
                String id = lines.get(i).trim(); // ID piosenki znajduje się w bieżącej linii

                // Logowanie: sprawdzamy, co porównujemy
                //System.out.println("Sprawdzam tytuł: " + title + " z tytułem: " + songTitle);

                // Sprawdzamy, czy tytuł piosenki pasuje do podanego tytułu
                if (title.equalsIgnoreCase(songTitle)) {
                    // Jeśli tytuł pasuje, zwracamy ID (pierwsza linia w grupie)
                    return id;
                }
            }

            // Skaczemy o 4 linie, aby przejść do następnej grupy
            i += 4;
        }

        // Jeśli tytuł nie został znaleziony, zwracamy null
        return null;
    }


    public Set<String> loadSongTitlesBasedOnCategorie(String songCategory) {
        songs = new HashSet<>();

        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        List<String> lines = null;
        try {
            lines = Files.readAllLines(libraryPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Ignorowanie separatorów
            if (line.matches("-+")) {
                continue;
            }

            // Upewnienie się, że mamy wystarczająco dużo linii na przetworzenie grupy
            if (i + 3 < lines.size()) {
                String category = lines.get(i + 3).trim(); // Czwórka to kategoria
                String title = lines.get(i+1).trim(); // Pierwsza linia to tytuł

                // Jeśli kategoria pasuje, dodajemy tytuł do zbioru
                if (category.equalsIgnoreCase(songCategory)) {
                    songs.add(title);
                }

                // Skaczemy o 4 linie, aby przejść do następnej grupy
                i += 4;
            }
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
        categories = new HashSet<>();

        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        // Wczytywanie wszystkich linii z pliku
        List<String> lines = null;
        try {
            lines = Files.readAllLines(libraryPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Iteracja po liniach, rozdzielając dane w grupach
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Pomijanie separatorów (linia składająca się z myślników)
            if (line.matches("-+")) {
                continue;
            }

            // Sprawdzamy, czy mamy przynajmniej 4 linie danych w grupie (ID, Tytuł, Wykonawca, Kategoria, Opis)
            if (i + 3 < lines.size()) {
                String category = lines.get(i + 3).trim(); // Czwórka to kategoria
                categories.add(category);
                i += 4; // Przechodzimy do następnej grupy (5 linia)
            }
        }

        return categories;
    }

    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }
}
