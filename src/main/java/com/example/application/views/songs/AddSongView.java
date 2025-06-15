package com.example.application.views.songs;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@PageTitle("Add Song")
@Route("add-song")
@Menu(order = 1, icon = "line-awesome/svg/music-solid.svg")
@RolesAllowed("ADMIN")
public class AddSongView extends VerticalLayout {

    public Set<String> categories;
    public Set<String> songs;
    public ComboBox<String> songCategoryCmbBox,songCmbBox;
    public Button saveBtn;
    public TextArea textArea;
    public VerticalLayout layoutColumn2 = new VerticalLayout();
    private static List<Song> listOfSongs = new ArrayList<>();
    private static Grid<Song> grid = new Grid<>();
    private static Div hint = new Div();
    public int width;
    public int height;

    public AddSongView(){

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
        this.createAddSongForm();
        this.refreshGrid();
    }
    public void createAddSongForm(){
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
            saveSong(new Song(songCategoryCmbBox.getValue(), songCmbBox.getValue(),textArea.getValue()));

            songCategoryCmbBox.setValue(null);
            songCmbBox.setValue(null);
            textArea.clear();
        });
        layoutColumn2.add(saveBtn);
    }

    private void setupGrid() {

        grid = new Grid<>(Song.class, false);
        grid.setWidth("100%");
        grid.setAllRowsVisible(true);
        //grid.addColumn(Dedication::getCategory).setHeader("Category").setAutoWidth(true);
        grid.addColumn(song -> String.join(", ", song.getCategory()))
                .setHeader("Category")
                .setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(Song::getTitle).setHeader("Title").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Song::getDescription).setHeader("Description").setAutoWidth(true).setFlexGrow(9);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, song) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_TERTIARY,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> this.shareSong(song));
                    button.setIcon(new Icon(VaadinIcon.SHARE));
                })).setHeader("Share").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, song) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> this.removeSong(song));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setHeader("Delete").setAutoWidth(true).setFlexGrow(0);

        listOfSongs = loadDataFromFile();
        grid.setItems(listOfSongs);
        grid.setWidth(width + "%");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, grid);
        layoutColumn2.add(grid);
    }

    private List<Song> loadDataFromFile() {
        List<Song> songs = new ArrayList<>();
        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path songsPath = Paths.get(resourcesPath, "library.txt");

        try {
            // Wczytanie wszystkich linii z pliku
            List<String> lines = Files.readAllLines(songsPath);

            // Przechodzenie przez linie i tworzenie dedykacji
            for (int i = 0; i < lines.size(); i += 4) {
                String title = lines.get(i).trim(); // Pierwsza linia to tytuł
                String category = lines.get(i + 1).trim(); // Druga linia to kategoria
                String description = lines.get(i + 2).trim(); // Trzecia linia to opis

                // Tworzenie obiektu Dedication i dodanie go do listy
                Song song = new Song(title,category, description);
                songs.add(song);
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return songs;
    }

    private void shareSong(Song song) {
        try {
            LiveView.setLiveSongTitle(loadSongIdByTitle(song.getTitle()));
            Notification notification = Notification.show(song.getTitle() + " shared!");
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
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
        if (listOfSongs.size() > 0) {
            grid.setVisible(true);
            hint.setVisible(false);
            grid.getDataProvider().refreshAll();
        } else {
            grid.setVisible(false);
            hint.setVisible(true);
        }
    }

    private void saveAllSongs() {
        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        StringBuilder allSongsBuilder = new StringBuilder();

        for (Song song : listOfSongs) {
            allSongsBuilder.append(song.getTitle()).append(System.lineSeparator());
            allSongsBuilder.append(song.getCategory()).append(System.lineSeparator());
            allSongsBuilder.append(song.getDescription()).append(System.lineSeparator());
            allSongsBuilder.append("----------------------").append(System.lineSeparator());
        }

        try {
            Files.write(libraryPath, allSongsBuilder.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisywania dedykacji: " + e.getMessage());
        }
    }



    private void saveSong(Song song) {
        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        String songDescription = song.toString().trim();

        try {
            // Dodanie tekstu na końcu pliku dedications.txtD
            Files.write(libraryPath, songDescription.getBytes(), StandardOpenOption.APPEND);
            Notification notification = new Notification("Song saved !", NotificationVariant.LUMO_PRIMARY.ordinal());

        } catch (IOException e) {
            System.err.println("Wystąpił błąd podczas dodawania tekstu do pliku: " + e.getMessage());
        }

        listOfSongs.add(song);
        this.refreshGrid();
    }


    private void removeSong(Song song) {
        listOfSongs.remove(song);
        refreshGrid();
        saveAllSongs(); // <-- nadpisuje cały plik nową wersją
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