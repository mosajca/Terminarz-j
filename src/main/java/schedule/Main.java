package schedule;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import schedule.database.Database;
import schedule.gui.Layout;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Layout layout = new Layout(new Database());
        Scene scene = new Scene(layout);
        scene.getStylesheets().add("style.css");
        primaryStage.setTitle("Terminarz");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
