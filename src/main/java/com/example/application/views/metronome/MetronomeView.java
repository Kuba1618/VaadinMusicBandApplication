package com.example.application.views.metronome;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Metronome")
@Route("metronome")
@Menu(order = 5, icon = "line-awesome/svg/stopwatch-solid.svg")
@RolesAllowed("USER")
public class MetronomeView extends Composite<VerticalLayout> {

    public MetronomeView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
