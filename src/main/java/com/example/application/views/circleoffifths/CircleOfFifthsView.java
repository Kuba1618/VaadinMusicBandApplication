package com.example.application.views.circleoffifths;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Circle Of Fifths")
@Route("circle-of-fifths")
@Menu(order = 6, icon = "line-awesome/svg/chart-pie-solid.svg")
@RolesAllowed("USER")
public class CircleOfFifthsView extends Composite<VerticalLayout> {

    public CircleOfFifthsView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
