package schedule.gui;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import schedule.database.Database;
import schedule.database.Event;

public class Layout extends BorderPane {

    private Database database;
    private EventWindow eventWindow;
    private UpcomingWindow upcomingWindow;
    private OffsetDateTime current;
    private Label date = new Label();
    private List<AnchorPane> weekAnchorPanes = new ArrayList<>();
    private boolean week = true;

    public Layout(Database database) {
        this.database = database;
        eventWindow = new EventWindow(database);
        upcomingWindow = new UpcomingWindow(database);
        eventWindow.setOnHidden(e -> refresh());
        setTop(createTop());
        setCenter(createCenter());
        setLeft(createLeft());
    }

    private HBox createTop() {
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER);
        top.getStyleClass().add("top");
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        Button add = new Button("dodaj");
        add.setOnAction(e -> eventWindow.resetAndShow());
        Button upcoming = new Button("nadchodzące");
        upcoming.setOnAction(e -> upcomingWindow.showEvents());
        date.setFont(Font.font(20));
        top.getChildren().addAll(date, region, upcoming, add);
        return top;
    }

    private VBox createLeft() {
        VBox left = new VBox();
        left.getStyleClass().add("left");
        for (String s : Arrays.asList("dzisiaj", "jutro", "w tym tygodniu", "w następnym tygodniu")) {
            left.getChildren().add(new Button(s));
        }
        setDayAction((Button) left.getChildren().get(0), OffsetDateTime::now);
        setDayAction((Button) left.getChildren().get(1), () -> OffsetDateTime.now().plusDays(1));
        setWeekAction((Button) left.getChildren().get(2), OffsetDateTime::now);
        setWeekAction((Button) left.getChildren().get(3), () -> OffsetDateTime.now().plusDays(7));
        ((Button) left.getChildren().get(2)).fire();
        return left;
    }

    private BorderPane createCenter() {
        BorderPane center = new BorderPane();
        center.getStyleClass().add("center");
        center.setBottom(createCenterBottom());
        center.setCenter(createCenterCenter());
        center.setTop(createCenterTop());
        return center;
    }

    private ScrollPane createCenterCenter() {
        HBox hbox = new HBox();
        AnchorPane time = new AnchorPane();
        time.getStyleClass().add("time-anchor-pane");
        weekAnchorPanes.add(time);
        hbox.getChildren().add(time);
        for (int i = 0; i < 24; ++i) {
            Label label = new Label(String.format("%02d:00", i));
            label.getStyleClass().add("time");
            AnchorPane.setTopAnchor(label, i * 120.0);
            AnchorPane.setLeftAnchor(label, 0.0);
            AnchorPane.setRightAnchor(label, 0.0);
            time.getChildren().add(label);
        }
        for (int i = 0; i < 7; ++i) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.getStyleClass().add("anchor-pane");
            anchorPane.prefWidthProperty().bind(hbox.widthProperty().divide(8));
            HBox.setHgrow(anchorPane, Priority.ALWAYS);
            anchorPane.managedProperty().bind(anchorPane.visibleProperty());
            weekAnchorPanes.add(anchorPane);
            hbox.getChildren().add(anchorPane);
            for (int j = 1; j < 24; ++j) {
                Region region = new Region();
                region.setStyle("-fx-border-style: solid centered");
                AnchorPane.setTopAnchor(region, j * 120.0);
                AnchorPane.setLeftAnchor(region, 0.0);
                AnchorPane.setRightAnchor(region, 0.0);
                anchorPane.getChildren().add(region);
            }
        }
        ScrollPane scrollPane = new ScrollPane(hbox);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        scrollPane.setVvalue(scrollPane.getVmax() / 2);
        return scrollPane;
    }

    private HBox createCenterBottom() {
        HBox bottom = new HBox();
        bottom.getStyleClass().add("center-bottom");
        Button prev = new Button("<-");
        HBox.setHgrow(prev, Priority.ALWAYS);
        Button next = new Button("->");
        HBox.setHgrow(next, Priority.ALWAYS);
        bottom.getChildren().addAll(prev, next);
        prev.setOnAction(event -> {
            if (current == null) return;
            try {
                weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(week));
                current = current.minusDays(week ? 7 : 1);
                showEvents();
            } catch (SQLException e) {
            }
        });
        next.setOnAction(event -> {
            if (current == null) return;
            try {
                weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(week));
                current = current.plusDays(week ? 7 : 1);
                showEvents();
            } catch (SQLException e) {
            }
        });
        return bottom;
    }

    private HBox createCenterTop() {
        HBox top = new HBox();
        top.getStyleClass().add("center-top");
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        region.maxWidthProperty().bind(weekAnchorPanes.get(0).widthProperty());
        region.prefWidthProperty().bind(weekAnchorPanes.get(0).widthProperty());
        top.getChildren().add(region);
        int i = 1;
        for (String s : Arrays.asList("Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela")) {
            Button button = new Button(s);
            button.setUserData(i);
            button.setOnAction(event -> {
                if (week) {
                    week = false;
                    current = current.plusDays((int) button.getUserData() - current.getDayOfWeek().getValue());
                    date.setText(current.toLocalDate().toString());
                    weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(false));
                    weekAnchorPanes.get((int) button.getUserData()).setVisible(true);
                } else {
                    week = true;
                    try {
                        showEvents(database.selectEventWhereWeek(current));
                        weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(true));
                        date.setText(current.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
                                + " - " + current.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString());
                    } catch (SQLException e) {
                    }
                }
            });
            HBox.setHgrow(button, Priority.ALWAYS);
            button.managedProperty().bind(button.visibleProperty());
            button.visibleProperty().bind(weekAnchorPanes.get(i).visibleProperty());
            button.maxWidthProperty().bind(weekAnchorPanes.get(i).widthProperty());
            button.prefWidthProperty().bind(weekAnchorPanes.get(i++).widthProperty());
            top.getChildren().add(button);
        }
        return top;
    }

    private Label createEventLabel(Event event) {
        Label label = new Label(event.getName());
        label.setOnMouseClicked(e -> eventWindow.fillAndShow(event));
        long top = ChronoUnit.MINUTES.between(LocalTime.of(0, 0), event.getStartDateTime().toLocalTime());
        AnchorPane.setTopAnchor(label, top * 2.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);
        long height = ChronoUnit.MINUTES.between(event.getStartDateTime(), event.getEndDateTime());
        label.setPrefHeight(height * 2);
        return label;
    }

    private void setWeekAction(Button button, Supplier<OffsetDateTime> dateTimeSupplier) {
        button.setOnAction(event -> {
            LocalDate d = dateTimeSupplier.get().toLocalDate();
            date.setText(d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
                    + " - " + d.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString());
            weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(true));
            week = true;
            try {
                current = dateTimeSupplier.get();
                showEvents(database.selectEventWhereWeek(current));
            } catch (SQLException e) {
            }
        });
    }

    private void setDayAction(Button button, Supplier<OffsetDateTime> dateTimeSupplier) {
        button.setOnAction(event -> {
            date.setText(dateTimeSupplier.get().toLocalDate().toString());
            weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(false));
            week = false;
            try {
                current = dateTimeSupplier.get();
                weekAnchorPanes.get(current.getDayOfWeek().getValue()).setVisible(true);
                showEvents(database.selectEventWhereDay(current));
            } catch (SQLException e) {
            }
        });
    }

    private void showEvents(List<Event> events) {
        weekAnchorPanes.stream().skip(1).forEach(a -> a.getChildren().removeIf(x -> x instanceof Label));
        for (Event e : events) {
            weekAnchorPanes.get(e.getStartDateTime().getDayOfWeek().getValue()).getChildren().add(createEventLabel(e));
        }
    }

    private void showEvents() throws SQLException {
        if (week) {
            showEvents(database.selectEventWhereWeek(current));
            date.setText(current.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
                    + " - " + current.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString());
        } else {
            weekAnchorPanes.get(current.getDayOfWeek().getValue()).setVisible(true);
            showEvents(database.selectEventWhereDay(current));
            date.setText(current.toLocalDate().toString());
        }
    }

    private void refresh() {
        try {
            if (week) {
                showEvents(database.selectEventWhereWeek(current));
            } else {
                showEvents(database.selectEventWhereDay(current));
            }
        } catch (SQLException e) {
        }
    }

}
