package schedule.gui;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import schedule.database.Database;
import schedule.database.Event;

public class Notification extends Stage {

    private double x, y;
    private Database database;
    private ScheduledExecutorService executorService;
    private Label label = new Label();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    public Notification(Database database) {
        super(StageStyle.UNDECORATED);
        this.database = database;
        label.setOnMouseClicked(e -> close());
        label.setPadding(new Insets(10));
        label.setStyle("-fx-background-color: yellow; -fx-border-color: black");
        label.setFont(Font.font(25));
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        x = visualBounds.getMinX() + visualBounds.getWidth();
        y = visualBounds.getMinY() + visualBounds.getHeight();
        setScene(new Scene(label));
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
            try {
                String str = database.selectEventWhereDay(now).stream()
                        .filter(e -> e.getStartDateTime().isAfter(now)
                                && e.getStartDateTime().isBefore(now.plusMinutes(10)))
                        .min(Comparator.comparing(Event::getStartDateTime))
                        .map(e -> e.getName() + "\no godzinie: " + e.getStartDateTime().toLocalTime().format(dtf))
                        .orElse("");
                if (!str.isEmpty()) Platform.runLater(() -> show(str));
            } catch (SQLException e1) {
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

}