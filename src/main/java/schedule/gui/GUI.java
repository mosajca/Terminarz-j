package schedule.gui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import schedule.database.Database;

public class GUI {

    private Stage primaryStage;
    private Database database;
    private Layout layout;

    public GUI(Stage primaryStage, Database database) {
        this.primaryStage = primaryStage;
        this.database = database;
        layout = new Layout(database);
        Scene scene = new Scene(layout);
        scene.getStylesheets().add("style.css");
        primaryStage.setTitle("Terminarz");
        primaryStage.setScene(scene);
    }

    public void show() {
        primaryStage.show();
    }

}
