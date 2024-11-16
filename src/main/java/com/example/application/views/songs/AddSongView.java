package com.example.application.views.songs;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@PageTitle("Add Song")
@Route("add-song")
@Menu(order = 2, icon = "line-awesome/svg/music-solid.svg")
@RolesAllowed("ADMIN")
public class AddSongView extends Composite<VerticalLayout> {

    private File file;
    private String originalFileName;
    private String mimeType;
    Upload uploadSongFile = new Upload(this::receiveUpload);
    public VerticalLayout layoutColumn2 = new VerticalLayout();
    public TextField textField = new TextField();
    public TextField textField2 = new TextField();
    public TextArea textArea = new TextArea();
    public ComboBox<String> comboBoxCategory;  // Zmieniamy na ComboBox
    public HorizontalLayout layoutRow = new HorizontalLayout();
    public Icon icon = new Icon();
    public Button saveBtn = new Button();
    public String txtFieldWidth;
    public int uploadSongBtnWidth;
    public int uploadSongBtnHeight;

    public AddSongView() {
        txtFieldWidth = isMobileDevice() ? "90%" :"50%";
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutColumn2.setWidthFull();
        getContent().setFlexGrow(1.0, layoutColumn2);
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutColumn2.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layoutColumn2.setAlignItems(FlexComponent.Alignment.CENTER);

        textField.setLabel("Title");
        textField.setWidth(txtFieldWidth);
        textField2.setLabel("Author");
        textField2.setWidth(txtFieldWidth);
        textArea.setLabel("Description");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, textArea);
        textArea.setWidth(txtFieldWidth);
        textArea.setHeight("50%");

        // Tworzymy ComboBox zamiast MultiSelectComboBox
        comboBoxCategory = new ComboBox<>("Category");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.CENTER, comboBoxCategory);
        comboBoxCategory.setWidth(txtFieldWidth);
        comboBoxCategory.setHeight("50%");
        comboBoxCategory.setItems(loadSongCategories());

        layoutColumn2.add(comboBoxCategory);

        layoutRow.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth(txtFieldWidth);
        layoutRow.getStyle().set("flex-grow", "1");
        layoutRow.setAlignItems(FlexComponent.Alignment.CENTER);
        layoutRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        uploadSongBtnWidth = isMobileDevice() ? 85 : 80;
        uploadSongBtnHeight = isMobileDevice() ? 30 : 25;
        uploadSongFile.setWidth(uploadSongBtnWidth + "%");
        uploadSongFile.setHeight(uploadSongBtnHeight + "%");

        saveBtn.setText("Save");
        saveBtn.setWidth("7%");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getContent().add(layoutColumn2);
        layoutColumn2.add(textField);
        layoutColumn2.add(textField2);
        layoutColumn2.add(textArea);
        layoutColumn2.add(comboBoxCategory);  // Dodajemy ComboBox do layoutu
        layoutColumn2.add(layoutRow);
        layoutRow.add(uploadSongFile);
        layoutRow.setSpacing(true);
        layoutRow.add(saveBtn);

        displayDeviceCategory();
        saveSong();
    }

    public List<String>  loadSongCategories(){
        List<String> listOfCategories = new ArrayList<>();
        listOfCategories.add("Disco Polo");
        listOfCategories.add("Rock");
        listOfCategories.add("First dance");
        listOfCategories.add("Electronic");
        listOfCategories.add("Wedding cake");
        listOfCategories.add("90's hit");
        listOfCategories.add("For parents");
        return listOfCategories;
    } //fill song categories cmbBox

    public void displayDeviceCategory(){
        uploadSongFile.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", "image/gif");
        uploadSongFile.addSucceededListener(event -> {
            layoutColumn2.add(new Image(new StreamResource(this.originalFileName,this::loadFile),"Uploaded image"));
        });

        uploadSongFile.addFailedListener(event -> {
            Div output = new Div(new Text("(no image file uploaded yet)"));
            output.removeAll();
            output.add(new Text("Upload failed: " + event.getReason()));
        });
    }

    public void saveSong() {
        saveBtn.addClickListener(buttonClickEvent -> {

            Song song = new Song(textField.getValue(), textField2.getValue(), comboBoxCategory.getValue(), textArea.getValue(), file);
            File songFile = song.getSongFile();
            String extension = "";

            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i + 1);
            }

            String resourcesPath = Paths.get("src", "main", "resources", "META-INF", "resources", "songs").toString();
            File newSongFile = new File(resourcesPath, song.getId() + "." + extension);
            songFile.renameTo(newSongFile);

            Path libraryPath = Paths.get(resourcesPath, "library.txt");

            String songMetaData = song.getId() + "\n" +
                    song.getTitle() + "\n" +
                    song.getAuthor() + "\n" +
                    song.getCategory() + "\n" +
                    song.getDescription() + "\n----------------------\n";

            try {
                Files.write(libraryPath, songMetaData.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                showNotification("Song added successfully!");
            } catch (IOException e) {
                showNotification("An error occurred while adding the song: " + e.getMessage());
            }
        });
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

    public InputStream loadFile() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        }
        return null;
    }

    public OutputStream receiveUpload(String originalFileName, String MIMEType) {
        this.originalFileName = originalFileName;
        this.mimeType = MIMEType;
        try {
            // Create a temporary file for example, you can provide your file here.
            this.file = File.createTempFile("prefix-", "-suffix");
            file.deleteOnExit();
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed to create InputStream for: '" + this.file.getAbsolutePath(), e);
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failed to create InputStream for: '" + this.file.getAbsolutePath() + "'", e);
        }
        return null;
    }

    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }

}
