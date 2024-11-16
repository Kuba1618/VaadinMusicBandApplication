package com.example.application.views.circleoffifths;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Circle Of Fifths")
@Route("circle-of-fifths")
@Menu(order = 6, icon = "line-awesome/svg/chart-pie-solid.svg")
@RolesAllowed({"USER","ADMIN"})
public class CircleOfFifths extends VerticalLayout {

    public int imgWidth;
    public int imgHeigth;

    public CircleOfFifths(){

        setSpacing(false);
        setPadding(false);

        imgWidth = isMobileDevice()? 100 : 60;
        imgHeigth = isMobileDevice()? 75 : 95;

        Image image = new Image("images/koloKwintowe.png", "placeholder plant");
        image.setWidth(imgWidth + "%");
        image.setHeight(imgHeigth + "%");
        add(image);

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
