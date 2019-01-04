package schedule.gui;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    private List<AnchorPane> anchorPanes = new ArrayList<>();
    private Button add = new Button("dodaj");

    public Layout(Database database) {
        this.database = database;
        setTop(createTop());
        setLeft(createLeft());
        setCenter(createCenter());
    }

    public void setAddButtonAction(EventHandler<ActionEvent> action) {
        add.setOnAction(action);
    }

    private HBox createTop() {
        HBox top = new HBox();
        top.getStyleClass().add("top");
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
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
        setThisWeekAction(left.getChildren().get(2));
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
        for (int i = 0; i < 7; ++i) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.getStyleClass().add("anchor-pane");
            HBox.setHgrow(anchorPane, Priority.ALWAYS);
            anchorPanes.add(anchorPane);
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
        return bottom;
    }

    private HBox createCenterTop() {
        HBox top = new HBox();
        top.getStyleClass().add("center-top");
        int i = 0;
        for (String s : Arrays.asList("Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela")) {
            Button button = new Button(s);
            HBox.setHgrow(button, Priority.ALWAYS);
            button.maxWidthProperty().bind(anchorPanes.get(i).widthProperty());
            button.prefWidthProperty().bind(anchorPanes.get(i++).widthProperty());
            top.getChildren().add(button);
        }
        return top;
    }

    private Label createEventLabel(Event event) {
        Label label = new Label(event.getName());
        long top = ChronoUnit.MINUTES.between(LocalTime.of(0, 0), event.getStartDateTime().toLocalTime());
        AnchorPane.setTopAnchor(label, top * 2.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);
        long height = ChronoUnit.MINUTES.between(event.getStartDateTime(), event.getEndDateTime());
        label.setPrefHeight(height * 2);
        return label;
    }

    private void setThisWeekAction(Node node) {
        node.setOnMouseClicked(event -> {
            try {
                List<Event> events = database.selectEventWhereWeek(OffsetDateTime.now());
                for (Event e : events) {
                    anchorPanes.get(e.getStartDateTime().getDayOfWeek().getValue() - 1)
                            .getChildren().add(createEventLabel(e));
                }
            } catch (SQLException e) {
            }
        });
    }

}
