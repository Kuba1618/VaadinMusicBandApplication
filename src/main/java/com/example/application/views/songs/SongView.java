package com.example.application.views.songs;

import com.example.application.PathConstants;
import com.example.application.views.dedications.Dedication;
import com.example.application.views.liveview.LiveView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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

@PageTitle("Songs")
@Route("songs")
@Menu(order = 1, icon = "line-awesome/svg/music-solid.svg")
@RolesAllowed("USER")
public class SongView extends VerticalLayout {

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
    public SongView(){

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
        this.refreshGrid();
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
        grid.addColumn(Song::getAuthor).setHeader("Author").setAutoWidth(true).setFlexGrow(9);
        grid.addColumn(Song::getDescription).setHeader("Description").setAutoWidth(true).setFlexGrow(9);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, song) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_TERTIARY,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> this.shareSong(song)); //@ToDo TUTAJ wrócić i dokończyć
                    button.setIcon(new Icon(VaadinIcon.SHARE));
                })).setHeader("Share").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, song) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> this.removeSong(song)); //@ToDo TUTAJ wrócić i dokończyć
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
        String resourcesPath = PathConstants.SONGS_LIBRARY;
        Path songsPath = Paths.get(resourcesPath, "library.txt");

        try {
            // Wczytanie wszystkich linii z pliku
            List<String> lines = Files.readAllLines(songsPath);

            // Przechodzenie przez linie i tworzenie dedykacji
            for (int i = 0; i < lines.size(); i += 6) {
                String id = lines.get(i).trim(); // Pierwsza linia to id
                String author = lines.get(i + 1).trim(); // Druga linia to autor
                String category = lines.get(i + 2).trim(); // Trzecia linia to kategoria
                String title = lines.get(i + 3).trim(); // Czwarta linia to tytuł
                String description = lines.get(i + 4).trim(); // Piata linia to opis

                // Tworzenie obiektu Dedication i dodanie go do listy
                Song song = new Song(id,title,author,category, description);
                songs.add(song);
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return songs;
    }

    private void shareSong(Song song) {

        Notification.show(song.getTitle() + " shared!");
        try {
            //System.out.println(loadSongIdByTitle(song.getTitle()));
            LiveView.setLiveSongTitle(loadSongIdByTitle(song.getTitle()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeSongFile(String songId) {
        String resourcesPath = PathConstants.SONGS_LIBRARY;
        Path filePath = Paths.get(resourcesPath, songId);

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                System.out.println("Plik został usunięty: " + filePath);
            } else {
                System.out.println("Plik nie istnieje: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas usuwania pliku: " + filePath);
            e.printStackTrace();
        }
    }

    private void removeSong(Song song) {
        removeSongFile(song.getId());
        listOfSongs.remove(song);
        refreshGrid();
        saveAllSongs(); // <-- nadpisuje cały plik nową wersją
    }

    private void saveAllSongs() {
        String resourcesPath = PathConstants.SONGS_LIBRARY;
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        StringBuilder allSongsBuilder = new StringBuilder();

        for (Song song : listOfSongs) {
            allSongsBuilder.append(song.getId()).append(System.lineSeparator());
            allSongsBuilder.append(song.getAuthor()).append(System.lineSeparator());
            allSongsBuilder.append(song.getCategory()).append(System.lineSeparator());
            allSongsBuilder.append(song.getTitle()).append(System.lineSeparator());
            allSongsBuilder.append(song.getDescription()).append(System.lineSeparator());
            allSongsBuilder.append("----------------------").append(System.lineSeparator());
        }

        try {
            Files.write(libraryPath, allSongsBuilder.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisywania dedykacji: " + e.getMessage());
        }
    }

    public static String loadSongIdByTitle(String songTitle) throws IOException {
        String resourcesPath = PathConstants.SONGS_LIBRARY;
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
                // Tytuł piosenki znajduje się w linii po ID (linia 4, czyli i+3)
                String title = lines.get(i + 3).trim(); // Tytuł piosenki
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

    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }
}
