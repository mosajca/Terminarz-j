package schedule.gui;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import schedule.database.Database;
import schedule.database.Event;

public class UpcomingWindow extends Stage {

    private Database database;
    private OffsetDateTime today;
    private VBox vbox = new VBox();

    public UpcomingWindow(Database database) {
        super(StageStyle.UTILITY);
        this.database = database;
        setTitle("Nadchodzące wydarzenia");
        initModality(Modality.APPLICATION_MODAL);
        setScene(new Scene(new ScrollPane(vbox)));
        sizeToScene();
    }

    public void showEvents() {
        vbox.getChildren().clear();
        for (Event event : getEvents()) {
            vbox.getChildren().add(createLabel(event));
        }
        super.show();
    }

    private List<Event> getEvents() {
        today = OffsetDateTime.now();
        OffsetDateTime tomorrow = today.plusDays(1);
        try {
            List<Event> events = database.selectEventWhereDay(today);
            events.addAll(database.selectEventWhereDay(tomorrow));
            return events.stream()
                    .filter(e -> e.getStartDateTime().isAfter(today) && e.getStartDateTime().isBefore(tomorrow))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
        }
        return Collections.emptyList();
    }

    private Label createLabel(Event event) {
        long between = ChronoUnit.MINUTES.between(today, event.getStartDateTime());
        int hours = (int) (between / 60);
        int minutes = (int) (between % 60);
        return new Label(event.getName() + "\nrozpocznie się za: "
                + format(hours, "godzin") + " i " + format(minutes, "minut"));
    }

    private String format(int value, String str) {
        String result = value + " " + str;
        int modulo = value % 10;
        if (value == 1) {
            result += "ę";
        } else if ((value < 5 || value > 14) && modulo > 1 && modulo < 5) {
            result += "y";
        }
        return result;
    }

}
