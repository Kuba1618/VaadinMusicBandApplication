package com.example.application.views.liveview;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@PageTitle("LiveView")
@Route("")
@Menu(order = 0, icon = "line-awesome/svg/globe-solid.svg")
@RolesAllowed({"USER","ADMIN"})
public class LiveView extends VerticalLayout {

    public static String liveSongTitle = "note.png";

//    <a href="https://www.flaticon.com/free-icons/melody" title="melody icons">Melody icons created by msidiqf - Flaticon</a>

    public int imgWidth;
    public int imgHeigth;
    public Image img;
    public Button forteBtn = new Button("Forte",new Icon(VaadinIcon.VOLUME_UP));
    public Button pianoBtn = new Button("Piano",new Icon(VaadinIcon.VOLUME_DOWN));
    public Button fasterBtn = new Button("Faster",new Icon(VaadinIcon.FLIGHT_TAKEOFF));
    public Button slowerBtn = new Button("Slower",new Icon(VaadinIcon.FLIGHT_LANDING));
    public ComboBox<String> songCategoryCmbBox;
    public ComboBox<String> songCmbBox;

    public Set<String> categories;
    public LiveView() {
        setSpacing(false);
        setPadding(false);

        imgWidth = isMobileDevice()? 120 : 65;
        imgHeigth = isMobileDevice()? 175 : 97;


        img = new Image("songs/" + liveSongTitle, "placeholder plant");//@TODO tu zmienić ścieżkę do pliku było "images/poland.png"
        img.setWidth(imgWidth - 35 + "%");
        img.setHeight(imgHeigth - 2 + "%");
        add(img);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Button openPopupButton = new Button("Notifications", new Icon(VaadinIcon.BELL));
        openPopupButton.setHeight(imgHeigth * 0.07 + "%");
        Dialog popup = new Dialog();
        popup = setPopupDialog(popup);
        Dialog finalPopup = popup;
        openPopupButton.addClickListener(event -> finalPopup.open());
        horizontalLayout.add(openPopupButton);
        dialogClickBtnListeners();

        Button openSearchPopupButton = new Button("Search", new Icon(VaadinIcon.SEARCH));
        openSearchPopupButton.setHeight(imgHeigth * 0.07 + "%");
        Dialog searchPopup = new Dialog();
        searchPopup = setSearchPopupDialog(searchPopup);
        Dialog finalSearchPopup = searchPopup;
        openSearchPopupButton.addClickListener(event -> finalSearchPopup.open());
        horizontalLayout.add(openSearchPopupButton);
        //dialogSearchClickBtnListeners();

        add(horizontalLayout);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    public static String getLiveSongTitle() {
        return liveSongTitle;
    }

    public static void setLiveSongTitle(String liveSongTitle) {
        LiveView.liveSongTitle = liveSongTitle;
    }

    private Dialog setSearchPopupDialog(Dialog searchPopup) {
        if (isMobileDevice()) {
            searchPopup.setHeight(imgHeigth * 0.25 + "%");
            searchPopup.setWidth(imgWidth * 0.7 + "%");
        } else if (!isMobileDevice()) {
            searchPopup.setHeight(imgHeigth * 0.55 + "%");
            searchPopup.setWidth(imgWidth * 0.35 + "%");
        }
        searchPopup.setResizable(true);

        // Utwórz przycisk do zamknięcia okna dialogowego
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), event -> searchPopup.close());
        if(isMobileDevice()){
            closeButton.setHeight(imgHeigth * 0.005 + "%");
            closeButton.setWidth(imgWidth * 0.005 + "%");
        } else if (!isMobileDevice()) {
            closeButton.setHeight(imgHeigth * 0.001 + "%");
            closeButton.setWidth(imgWidth * 0.001 + "%");
        }

        VerticalLayout verticalLayout = new VerticalLayout();

        HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setSpacing(false);
        buttonLayout.setPadding(false);

        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        verticalLayout.add(buttonLayout);
        searchPopup.add(verticalLayout);

        songCategoryCmbBox = new ComboBox<>("Song Category");
        songCategoryCmbBox.setWidth(imgWidth + "%");
        songCategoryCmbBox.setHeight("15%");
        songCategoryCmbBox.setItems(loadSongCategories());
        songCategoryCmbBox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> comboBoxStringComponentValueChangeEvent) {
                songCmbBox.setItems(loadSongTitlesBasedOnCategorie(songCategoryCmbBox.getValue()));
            }
        });
        verticalLayout.setAlignSelf(FlexComponent.Alignment.CENTER, songCategoryCmbBox);
        verticalLayout.add(songCategoryCmbBox);

        songCmbBox = new ComboBox<>("Song Title");
        songCmbBox.setWidth(imgWidth + "%");
        songCmbBox.setHeight("15%");
        setListOfSongsTitleInCmbBox(songCmbBox);
        verticalLayout.setAlignSelf(FlexComponent.Alignment.CENTER, songCmbBox);
        verticalLayout.add(songCmbBox);

        Div spacer = new Div();
        spacer.setHeight("20%"); // Możesz dostosować wysokość
        verticalLayout.add(spacer);

        Button shareBtn = new Button("Share",new Icon(VaadinIcon.SHARE));
        shareBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        searchPopup.setAlignSelf(FlexComponent.Alignment.CENTER, shareBtn);
        shareBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {

            try {
                String songId = loadSongIdByTitle(songCmbBox.getValue());
                LiveView.setLiveSongTitle(songId);
                searchPopup.close();
                remove(img);
                add(img);
                UI.getCurrent().getPage().reload();
                showNotification(songCmbBox.getValue() + " shared !");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        verticalLayout.setSpacing(true);
        verticalLayout.add(shareBtn);
        verticalLayout.setAlignItems(Alignment.CENTER);

        searchPopup.add(verticalLayout);

        return searchPopup;
    }

    private boolean fileExists(String fileName) {
        String resourcesPath = Paths.get("src", "main", "resources", "META-INF.resources", "songs").toString();
        Path filePath = Paths.get(resourcesPath, fileName);
        return Files.exists(filePath);
    }


    public void refreshImage(int songId) {

        String[] extensions = {".jpg", ".png", ".jpeg"};
        boolean fileFound = false;

        for (String ext : extensions) {
            String fileName = songId + ext;
            if (fileExists(fileName)) {
                String newPath = "songs/" + fileName + "?" + System.currentTimeMillis();
                Image img = (Image) getChildren().filter(c -> c instanceof Image).findFirst().orElse(null);
                if (img != null) {
                    img.setSrc(newPath);
                    fileFound = true;
                    break;
                }
            }
        }

        if (!fileFound) {
            System.err.println("Żaden z plików obrazu nie został znaleziony.");
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

    private void setListOfSongsTitleInCmbBox(ComboBox<String> songCmbBox) {
        List<String> listOfSongs = new ArrayList<>();
        listOfSongs.add("Thinking Out Loud");
        listOfSongs.add("Shape of You");
        listOfSongs.add("Perfect");
        listOfSongs.add("All of me");
        listOfSongs.add("Girls like You");
        listOfSongs.add("Suggar");
        listOfSongs.add("Stay with Me");
        songCmbBox.setItems(listOfSongs);
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

    public Set<String> loadSongTitlesBasedOnCategorie(String songCategory) {
        Set<String> songs = new HashSet<>();

        String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
        Path libraryPath = Paths.get(resourcesPath, "library.txt");

        // Sprawdzanie, czy plik istnieje
        if (!Files.exists(libraryPath)) {
            System.err.println("Plik nie został znaleziony: " + libraryPath.toAbsolutePath());
            return songs;
        }

        try (Scanner scanner = new Scanner(libraryPath)) {
            while (scanner.hasNextLine()) {
                // Sprawdzamy, czy jest kolejna linia przed odczytem
                String id = scanner.nextLine().trim();           // Ignorujemy ID
                if (!scanner.hasNextLine()) break;
                String title = scanner.nextLine().trim();        // Tytuł piosenki
                if (!scanner.hasNextLine()) break;
                String author = scanner.nextLine().trim();       // Autor piosenki
                if (!scanner.hasNextLine()) break;
                String category = scanner.nextLine().trim();     // Kategoria piosenki

                // Sprawdzenie, czy jest dodatkowa linia (tonacja lub separator)
                if (scanner.hasNextLine()) {
                    String potentialTonationOrSeparator = scanner.nextLine().trim();
                    if (!potentialTonationOrSeparator.equals("----------------------")) {
                        // Jeśli nie jest to separator, ignorujemy tonację
                        if (scanner.hasNextLine()) {
                            // Wczytujemy separator po tonacji
                            scanner.nextLine();
                        }
                    }
                }

                if (category.equals(songCategory)) {
                    songs.add(title);
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return songs;
    }

    private Dialog setPopupDialog(Dialog popup) {
        if(isMobileDevice()){
            popup.setHeight(imgHeigth * 0.34 + "%");
            popup.setWidth(imgWidth * 0.7 + "%");
        } else if (!isMobileDevice()) {
            popup.setHeight(imgHeigth * 0.24 + "%");
            popup.setWidth(imgWidth * 0.55 + "%");
        }
        popup.setResizable(true);
        //        popup.add("To jest zawartość okna popup.");

        VerticalLayout verticalLayout = new VerticalLayout();

        // Utwórz przycisk do zamknięcia okna dialogowego
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), event -> popup.close());
        if(isMobileDevice()){
            closeButton.setHeight(imgHeigth * 0.005 + "%");
            closeButton.setWidth(imgWidth * 0.005 + "%");
        } else if (!isMobileDevice()) {
            closeButton.setHeight(imgHeigth * 0.001 + "%");
            closeButton.setWidth(imgWidth * 0.001 + "%");
        }

        HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setSpacing(false);
        buttonLayout.setPadding(false);
        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        verticalLayout.add(buttonLayout);
        popup.add(verticalLayout);

        VerticalLayout layout = new VerticalLayout();

        if(isMobileDevice()){
            // Dodaj przyciski (2x2)
            HorizontalLayout row1 = new HorizontalLayout(pianoBtn,forteBtn);
            HorizontalLayout row2 = new HorizontalLayout(slowerBtn,fasterBtn);
            layout.add(row1, row2);
        } else if (!isMobileDevice()) {
            // Dodaj przyciski (2x2)
            HorizontalLayout row1 = new HorizontalLayout(pianoBtn,forteBtn,slowerBtn,fasterBtn);
            layout.add(row1);
        }

        layout.setSpacing(true);
        layout.setPadding(true);

        // Dodaj elementy do okna dialogowego
        popup.add(layout);

        return popup;
    }

    public void showNotification(String notificationMessage){
        Notification notification = Notification.show(notificationMessage);
        if(isMobileDevice()){
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        }
        else if(!isMobileDevice()){
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        }
    }

    public void dialogClickBtnListeners(){
        forteBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            showNotification("Let's play it LOUDER !");
        });
        pianoBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            showNotification("Let's play it MORE QUIETLY !");
        });
        fasterBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            showNotification("Let's play it FASTER !");
        });
        slowerBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            showNotification("Let's play it SLOWER !");
        });
    }
    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }

}
