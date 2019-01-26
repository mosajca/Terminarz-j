package schedule.gui;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import schedule.database.Database;
import schedule.database.Event;

public class Notification extends Stage {

    private double x, y;
    private Database database;
    private Event lastShown;
    private ScheduledExecutorService executorService;
    private Label label = new Label();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    public Notification(Database database) {
        super(StageStyle.UNDECORATED);
        this.database = database;
        label.getStyleClass().add("notification");
        label.setOnMouseClicked(e -> close());
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        x = visualBounds.getMinX() + visualBounds.getWidth();
        y = visualBounds.getMinY() + visualBounds.getHeight();
        Scene scene = new Scene(label);
        scene.getStylesheets().add("style.css");
        setScene(scene);
        setAlwaysOnTop(true);
        start();
    }

    public void stop() {
        executorService.shutdown();
    }

    private void show(String text) {
        label.setText(text);
        sizeToScene();
        show();
        setX(x - getWidth());
        setY(y - getHeight());
    }

    private void start() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime nowPlusMinutes10 = now.plusMinutes(10);
            try {
                List<Event> events = database.selectEventWhereDay(now);
                if (now.getDayOfYear() != nowPlusMinutes10.getDayOfYear()) {
                    events.addAll(database.selectEventWhereDay(nowPlusMinutes10));
                }
                String str = events.stream()
                        .filter(e -> e.getStartDateTime().isAfter(now)
                                && e.getStartDateTime().isBefore(now.plusMinutes(10)))
                        .min(Comparator.comparing(Event::getStartDateTime))
                        .map(e -> e.equals(lastShown) ? null : (lastShown = e))
                        .map(e -> e.getName() + "\no godzinie: " + e.getStartDateTime().toLocalTime().format(dtf))
                        .orElse("");
                if (!str.isEmpty()) Platform.runLater(() -> show(str));
            } catch (SQLException e1) {
                Platform.runLater(() -> show("Wystąpił błąd."));
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

}
