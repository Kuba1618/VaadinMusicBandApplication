package com.example.application.views.addsong;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Add Song")
@Route("add-song")
@Menu(order = 2, icon = "line-awesome/svg/music-solid.svg")
@RolesAllowed("ADMIN")
public class AddSongView extends Composite<VerticalLayout> {

    public AddSongView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
