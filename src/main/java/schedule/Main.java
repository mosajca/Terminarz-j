package schedule;

import javafx.application.Application;
import javafx.scene.Scene;
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
        Scene scene = new Scene(layout);
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
