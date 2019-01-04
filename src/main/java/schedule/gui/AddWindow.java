package schedule.gui;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import schedule.database.Database;
import schedule.database.Event;

public class AddWindow extends Stage {

    private TextField name = new TextField();
    private TextArea description = new TextArea();
    private DatePicker date = new DatePicker();
    private TimePicker startTime = new TimePicker();
    private TimePicker endTime = new TimePicker();

    private ZoneOffset offset = OffsetDateTime.now().getOffset();
    private Button add;
    private Popup popup;
    private Database database;

    public AddWindow(Database database) {
        super(StageStyle.UTILITY);
        this.database = database;
        date.setEditable(false);
        popup = createPopup();
        add = createButton();
        setTitle("Dodaj wydarzenie");
        initModality(Modality.APPLICATION_MODAL);
        setScene(new Scene(createRoot()));
        sizeToScene();
        reset();
    }

    public void resetAndShow() {
        reset();
        super.show();
    }

    private Button createButton() {
        Button button = new Button("Dodaj");
        button.setOnAction(e -> {
            try {
                database.insertEvent(createEvent());
            } catch (SQLException ex) {
                popup.show(this);
            }
        });
        return button;
    }

    private Event createEvent() {
        return new Event(name.getText(), description.getText(),
                OffsetDateTime.of(date.getValue(), startTime.getValue(), offset),
                OffsetDateTime.of(date.getValue(), endTime.getValue(), offset));
    }

    private Popup createPopup() {
        Popup popup = new Popup();
        Label label = new Label("Wystąpił błąd.");
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
        root.add(add, 0, 5, 2, 1);
        add.setMaxWidth(Double.MAX_VALUE);
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
