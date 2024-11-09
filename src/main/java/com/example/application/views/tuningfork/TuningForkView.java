package com.example.application.views.tuningfork;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Tuning Fork")
@Route("tuning-fork")
@Menu(order = 4, icon = "line-awesome/svg/sliders-h-solid.svg")
@RolesAllowed("USER")
public class TuningForkView extends Composite<VerticalLayout> {

    public TuningForkView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
