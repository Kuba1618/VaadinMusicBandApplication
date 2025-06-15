package com.example.application.views.tuningfork;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Tuning Fork")
@Route("tuning-fork")
@Menu(order = 4, icon = "line-awesome/svg/sliders-h-solid.svg")
@RolesAllowed({"USER","ADMIN"})
public class TuningFork extends VerticalLayout {

    //<a href="https://www.flaticon.com/free-icons/camerton" title="camerton icons">Camerton icons created by Ylivdesign - Flaticon</a>
    int width;
    int height;
    boolean isPlay = true;

    public TuningFork() {
        setSpacing(false);

        width = isMobileDevice()? 80 : 35;
        height = isMobileDevice()? 50 : 60;

        Image img = new Image("images/thin.png", "placeholder plant");
        img.setWidth(width + "%");
        img.setHeight(height + "%");
        add(img);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        IntegerField integerField = new IntegerField();
        integerField.setLabel("Frequency (Hz)");
        integerField.setHelperText("(440 - 444)");
        integerField.setMin(440);
        integerField.setMax(444);
        integerField.setValue(442);
        integerField.setStep(1);
        integerField.setStepButtonsVisible(true);
        horizontalLayout.add(integerField);

        Button soundBtn = new Button(new Icon(VaadinIcon.PLAY));
        soundBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            if(isPlay){
//                SoundPlayer player = new SoundPlayer();
//                player.play(String.valueOf(UrlResource.from("sounds\\matronome.png")));
//                soundBtn.setIcon(new Icon(VaadinIcon.STOP));
                isPlay = false;
            } else {
                soundBtn.setIcon(new Icon(VaadinIcon.PLAY));
                isPlay = true;
            }
        });
        horizontalLayout.add(soundBtn);

        add(horizontalLayout);
        setSizeFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }
}
