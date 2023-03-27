package uk.ac.soton.comp1206;

import java.nio.file.Files;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Colour;

/**
 * JavaFX Application class
 */
public class App extends Application {

    /**
     * Base resolution width
     */
    private final int width = 800;

    /**
     * Base resolution height
     */
    private final int height = 600;

    private static App instance;
    private static final Logger logger = LogManager.getLogger(App.class);
    private Stage stage;

    public static final boolean DEBUG_MODE = true;

    /**
     * Start the game
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        logger.info("Starting client");
        launch();
    }

    /**
     * Called by JavaFX with the primary stage as a parameter. Begins the game by opening the Game
     * Window
     *
     * @param stage the default stage, main window
     */
    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;

        //eula();

        //Open game window
        openGame();
    }

    /**
     * This is just me being silly :)
     */
    private void eula() {
        try {
            String content = Files.readString(Path.of(
                    App.class.getResource("/style/eula.txt").getPath().substring(1)));
            var alert = new Alert(AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(new ButtonType("I agree", ButtonData.OK_DONE));
            alert.getButtonTypes().add(new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));

            alert.setTitle("TetrECS License Agreement");
            alert.setHeaderText("Please agree to the license terms before continuing");
            var area = new TextArea(content);
            area.setWrapText(true);
            area.setEditable(false);
            alert.getDialogPane().setContent(area);
            alert.showAndWait();
            if (alert.getResult().getText().equals("Cancel")) {
                logger.info(Colour.error("bye.."));
                System.exit(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
            //logger.error(Colour.error("Could not read EULA :("));
        }
    }

    /**
     * Create the GameWindow with the specified width and height
     */
    public void openGame() {
        logger.info("Opening game window");

        //Change the width and height in this class to change the base rendering resolution for all game parts
        var gameWindow = new GameWindow(stage, width, height);

        //Display the GameWindow
        stage.show();
    }

    /**
     * Shutdown the game
     */
    public void shutdown() {
        logger.info(Colour.cyan("Shutting down"));
        System.exit(0);
    }

    /**
     * Get the singleton App instance
     *
     * @return the app
     */
    public static App getInstance() {
        return instance;
    }

}