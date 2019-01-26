package schedule.gui;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import schedule.database.Database;
import schedule.database.Event;

public class EventWindow extends Stage {

    private Event event;

    private TextField name = new TextField();
    private TextArea description = new TextArea();
    private DatePicker date = new DatePicker();
    private TimePicker startTime = new TimePicker();
    private TimePicker endTime = new TimePicker();

    private ZoneOffset offset = OffsetDateTime.now().getOffset();
    private Button insert, update, delete;
    private HBox buttons = new HBox();
    private Popup popup;
    private Database database;

    public EventWindow(Database database) {
        super(StageStyle.UTILITY);
        this.database = database;
        date.setEditable(false);
        popup = createPopup();
        insert = createButtonInsert();
        update = createButtonUpdate();
        delete = createButtonDelete();
        name.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().length() < 256 ? c : null));
        description.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().length() < 512 ? c : null));
        setTitle("Wydarzenie");
        initModality(Modality.APPLICATION_MODAL);
        setScene(new Scene(createRoot()));
        sizeToScene();
        setResizable(false);
        reset();
    }

    public void resetAndShow() {
        reset();
        showButtons(insert);
        super.show();
    }

    public void fillAndShow(Event event) {
        this.event = event;
        name.setText(event.getName());
        description.setText(event.getDescription());
        date.setValue(event.getStartDateTime().toLocalDate());
        startTime.setValue(event.getStartDateTime().toLocalTime());
        endTime.setValue(event.getEndDateTime().toLocalTime());
        showButtons(update, delete);
        super.show();
    }

    private void showButtons(Button... buttons) {
        this.buttons.getChildren().clear();
        this.buttons.getChildren().addAll(buttons);
    }

    private Button createButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setOnAction(handler);
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private Button createButtonInsert() {
        return createButton("Dodaj", e -> {
            try {
                Event event = createEvent();
                if (overlap(event) || wrongTime(event)) {
                    popup.show(this);
                } else {
                    database.insertEvent(event);
                    hide();
                }
            } catch (SQLException ex) {
                popup.show(this);
            }
        });
    }

    private Button createButtonUpdate() {
        return createButton("Aktualizuj", e -> {
            try {
                Event event = updateEvent();
                if (overlap(event) || wrongTime(event)) {
                    popup.show(this);
                } else {
                    database.updateEvent(event);
                    hide();
                }
            } catch (SQLException ex) {
                popup.show(this);
            }
        });
    }

    private Button createButtonDelete() {
        return createButton("Usuń", e -> {
            try {
                database.deleteEvent(event);
                hide();
            } catch (SQLException ex) {
                popup.show(this);
            }
        });
    }

    private boolean overlap(Event y) throws SQLException {
        return database.selectEventWhereDay(y.getStartDateTime()).stream()
                .anyMatch(x -> !x.getId().equals(y.getId())
                        && x.getStartDateTime().isBefore(y.getEndDateTime())
                        && y.getStartDateTime().isBefore(x.getEndDateTime()));
    }

    private boolean wrongTime(Event e) {
        return e.getStartDateTime().isAfter(e.getEndDateTime()) || e.getStartDateTime().isEqual(e.getEndDateTime());
    }

    private Event createEvent() {
        return new Event(name.getText(), description.getText(),
                OffsetDateTime.of(date.getValue(), startTime.getValue(), offset),
                OffsetDateTime.of(date.getValue(), endTime.getValue(), offset));
    }

    private Event updateEvent() {
        event.setName(name.getText());
        event.setDescription(description.getText());
        event.setStartDateTime(OffsetDateTime.of(date.getValue(), startTime.getValue(), offset));
        event.setEndDateTime(OffsetDateTime.of(date.getValue(), endTime.getValue(), offset));
        return event;
    }

    private Popup createPopup() {
        Popup popup = new Popup();
        Label label = new Label("Wystąpił błąd.");
        label.setPadding(new Insets(10));
        label.setStyle("-fx-background-color: yellow; -fx-border-color: black");
        label.setOnMouseClicked(e -> popup.hide());
        label.setFont(Font.font(25));
        popup.getContent().add(label);
        popup.setAutoHide(true);
        return popup;
    }

    private GridPane createRoot() {
        GridPane root = new GridPane();
        root.add(new Label("Nazwa: "), 0, 0);
        root.add(new Label("Opis: "), 0, 1);
        root.add(new Label("Dzień: "), 0, 2);
        root.add(new Label("Początek: "), 0, 3);
        root.add(new Label("Koniec: "), 0, 4);
        root.add(name, 1, 0);
        root.add(description, 1, 1);
        root.add(date, 1, 2);
        root.add(startTime, 1, 3);
        root.add(endTime, 1, 4);
        root.add(buttons, 0, 5, 2, 1);
        return root;
    }

    private void reset() {
        name.clear();
        description.clear();
        date.setValue(LocalDate.now());
        startTime.setValue(LocalTime.now());
        endTime.setValue(LocalTime.now().plusHours(1));
    }

}
