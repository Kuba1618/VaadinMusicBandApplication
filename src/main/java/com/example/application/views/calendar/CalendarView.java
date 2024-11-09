package com.example.application.views.calendar;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Calendar")
@Route("calendar")
@Menu(order = 3, icon = "line-awesome/svg/calendar-solid.svg")
@RolesAllowed("USER")
public class CalendarView extends Composite<VerticalLayout> {

    public CalendarView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
