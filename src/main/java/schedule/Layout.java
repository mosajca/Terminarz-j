package schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class Layout extends BorderPane {

    private List<AnchorPane> anchorPanes = new ArrayList<>();

    public Layout() {
        setTop(createTop());
        setLeft(createLeft());
        setCenter(createCenter());
    }

    private HBox createTop() {
        HBox top = new HBox();
        top.getStyleClass().add("top");
        Button add = new Button("dodaj");
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

}
