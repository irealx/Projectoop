import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameView gameView = new GameView();
        Scene scene = new Scene(gameView);
        primaryStage.setTitle("Dungeon Escape - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        gameView.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}