package schedule;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import schedule.database.Database;
import schedule.gui.Layout;
import schedule.gui.Notification;

public class Main extends Application {

    private Notification notification;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Database database = new Database();
        notification = new Notification(database);
        Layout layout = new Layout(database);
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(layout, visualBounds.getWidth() / 1.2, visualBounds.getHeight() / 1.2);
        scene.getStylesheets().add("style.css");
        primaryStage.setTitle("Terminarz");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        notification.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
