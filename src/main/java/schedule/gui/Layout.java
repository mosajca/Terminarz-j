package schedule.gui;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import schedule.database.Database;
import schedule.database.Event;

public class Layout extends BorderPane {

    private Database database;
    private EventWindow eventWindow;
    private OffsetDateTime current;
    private List<AnchorPane> weekAnchorPanes = new ArrayList<>();
    private boolean week = true;

    public Layout(Database database) {
        this.database = database;
        eventWindow = new EventWindow(database);
        setTop(createTop());
        setLeft(createLeft());
        setCenter(createCenter());
    }

    private HBox createTop() {
        HBox top = new HBox();
        top.getStyleClass().add("top");
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        Button add = new Button("dodaj");
        add.setOnAction(e -> eventWindow.resetAndShow());
        top.getChildren().addAll(region, add);
        return top;
    }

    private VBox createLeft() {
        VBox left = new VBox();
        left.getStyleClass().add("left");
        ToggleGroup toggleGroup = new ToggleGroup();
        for (String s : Arrays.asList("dzisiaj", "jutro", "w tym tygodniu", "w następnym tygodniu")) {
            ToggleButton toggleButton = new ToggleButton(s);
            toggleButton.setToggleGroup(toggleGroup);
            left.getChildren().add(toggleButton);
        }
        setDayAction(left.getChildren().get(0), OffsetDateTime::now);
        setDayAction(left.getChildren().get(1), () -> OffsetDateTime.now().plusDays(1));
        setWeekAction(left.getChildren().get(2), OffsetDateTime::now);
        setWeekAction(left.getChildren().get(3), () -> OffsetDateTime.now().plusDays(7));
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
        }
        ScrollPane scrollPane = new ScrollPane(hbox);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
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
                if (week) {
                    showEvents(database.selectEventWhereWeek(current));
                } else {
                    weekAnchorPanes.get(current.getDayOfWeek().getValue()).setVisible(true);
                    showEvents(database.selectEventWhereDay(current));
                }
            } catch (SQLException e) {
            }
        });
        next.setOnAction(event -> {
            if (current == null) return;
            try {
                weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(week));
                current = current.plusDays(week ? 7 : 1);
                if (week) {
                    showEvents(database.selectEventWhereWeek(current));
                } else {
                    weekAnchorPanes.get(current.getDayOfWeek().getValue()).setVisible(true);
                    showEvents(database.selectEventWhereDay(current));
                }
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
                week = false;
                current = current.plusDays((int) button.getUserData() - current.getDayOfWeek().getValue());
                weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(false));
                weekAnchorPanes.get((int) button.getUserData()).setVisible(true);
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

    private void setWeekAction(Node node, Supplier<OffsetDateTime> dateTimeSupplier) {
        node.setOnMouseClicked(event -> {
            weekAnchorPanes.stream().skip(1).forEach(a -> a.setVisible(true));
            week = true;
            try {
                current = dateTimeSupplier.get();
                showEvents(database.selectEventWhereWeek(current));
            } catch (SQLException e) {
            }
        });
    }

    private void setDayAction(Node node, Supplier<OffsetDateTime> dateTimeSupplier) {
        node.setOnMouseClicked(event -> {
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
        weekAnchorPanes.stream().skip(1).forEach(a -> a.getChildren().clear());
        for (Event e : events) {
            weekAnchorPanes.get(e.getStartDateTime().getDayOfWeek().getValue()).getChildren().add(createEventLabel(e));
        }
    }

}
