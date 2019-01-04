package schedule;

import javafx.application.Application;
import javafx.stage.Stage;

import schedule.database.Database;
import schedule.gui.GUI;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        GUI gui = new GUI(primaryStage, new Database());
        gui.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
