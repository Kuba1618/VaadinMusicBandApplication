package com.example.application.views.metronome;

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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Metronome")
@Route("metronome")
@Menu(order = 5, icon = "line-awesome/svg/stopwatch-solid.svg")
@RolesAllowed("USER")
public class Metronome extends VerticalLayout {

    //<a href="https://www.flaticon.com/free-icons/metronome" title="metronome icons">TuningFork icons created by Freepik - Flaticon</a>

    int width;
    int height;
    boolean isPlay = true;

    public Metronome() {
        setSpacing(false);

        width = isMobileDevice()? 80 : 35;
        height = isMobileDevice()? 50 : 60;

        Image img = new Image("images/metronome.png", "placeholder plant");
        img.setWidth(width + "%");
        img.setHeight(height + "%");
        add(img);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

//        PaperSlider paperSlider = new PaperSlider(0,100,50);
//        paperSlider.addValueChangeListener( e -> {
//            Notification.show(e.getValue() + "");
//        });

        Button soundBtn = new Button(new Icon(VaadinIcon.PLAY));
        soundBtn.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            if(isPlay){
                //SoundPlayer player = new SoundPlayer();

                soundBtn.setIcon(new Icon(VaadinIcon.STOP));
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
