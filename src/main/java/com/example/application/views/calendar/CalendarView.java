package com.example.application.views.calendar;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;

@PageTitle("Calendar")
@Route("calendar")
@Menu(order = 3, icon = "line-awesome/svg/calendar-solid.svg")
@RolesAllowed("USER")
public class CalendarView extends VerticalLayout {


    private LocalDate currentDate = LocalDate.now();
    private DatePicker datePicker = new DatePicker(currentDate);
    private Div calendarLayout = new Div();
    private Map<LocalDate, List<String>> events = new HashMap<>();

    public CalendarView() {
        // Przyciski do nawigacji po miesiącach
        Button prevMonthButton = new Button("<", e -> changeMonth(-1));
        Button nextMonthButton = new Button(">", e -> changeMonth(1));

        HorizontalLayout headerLayout = new HorizontalLayout(prevMonthButton, datePicker, nextMonthButton);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        datePicker.addValueChangeListener(e -> {
            currentDate = e.getValue();
            updateCalendar();
        });

        add(headerLayout, calendarLayout);
        updateCalendar();
    }

    private void updateCalendar() {
        calendarLayout.removeAll();
        calendarLayout.getStyle().set("display", "grid");
        calendarLayout.getStyle().set("grid-template-columns", "repeat(7, 1fr)");
        calendarLayout.getStyle().set("gap", "1px");

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate firstDayOfCalendar = firstOfMonth.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

        // Nazwy dni tygodnia
        for (int i = 0; i < 7; i++) {
            Span dayName = new Span(firstDayOfCalendar.plusDays(i).getDayOfWeek().toString().substring(0, 3));
            dayName.getStyle().set("text-align", "center");
            dayName.getStyle().set("font-weight", "bold");
            calendarLayout.add(dayName);
        }

        // Generowanie dni miesiąca
        for (int i = 0; i < 42; i++) {  // 7 dni x 6 tygodni = 42 komórki
            LocalDate date = firstDayOfCalendar.plusDays(i);
            VerticalLayout dayCell = new VerticalLayout();
            dayCell.add(new Span(String.valueOf(date.getDayOfMonth())));

            // Jeśli data nie jest częścią bieżącego miesiąca, wyłącz ją
            if (!date.getMonth().equals(yearMonth.getMonth())) {
                dayCell.getStyle().set("background-color", "#f0f0f0");
            }

            // Dodanie wydarzeń do odpowiednich dni
            if (events.containsKey(date)) {
                for (String event : events.get(date)) {
                    Span eventLabel = new Span(event);
                    eventLabel.getStyle().set("font-size", "12px");
                    eventLabel.getStyle().set("background-color", "#e0e0ff");
                    eventLabel.getStyle().set("padding", "2px");
                    eventLabel.getStyle().set("border-radius", "4px");
                    dayCell.add(eventLabel);
                }
            }

            // Kliknięcie na dzień otwiera dialog do dodania wydarzenia
            dayCell.addClickListener(e -> openEventDialog(date));
            dayCell.getStyle().set("border", "1px solid #ccc");
            dayCell.setPadding(false);
            dayCell.setSpacing(false);
            dayCell.setHeight("100px");

            calendarLayout.add(dayCell);
        }
    }

    private void openEventDialog(LocalDate date) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField eventNameField = new TextField("Nazwa wydarzenia");
        Button saveButton = new Button("Zapisz", e -> {
            String eventName = eventNameField.getValue();
            if (!eventName.isEmpty()) {
                addEvent(date, eventName);
                dialog.close();
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(eventNameField, saveButton);

        dialog.add(formLayout);
        dialog.open();
    }

    private void addEvent(LocalDate date, String eventName) {
        events.putIfAbsent(date, new ArrayList<>());
        events.get(date).add(eventName);
        updateCalendar();
    }

    private void changeMonth(int months) {
        currentDate = currentDate.plusMonths(months);
        datePicker.setValue(currentDate);
        updateCalendar();
    }
}
