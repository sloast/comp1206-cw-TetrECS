package uk.ac.soton.comp1206;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Colour;

/**
 * JavaFX Application class
 */
public class App extends Application {

    /**
     * Whether the application should run in debug mode
     */
    public static final boolean DEBUG_MODE = true;
    private static final Logger logger = LogManager.getLogger(App.class);

    /**
     * The singleton instance of the app
     */
    private static App instance;

    /**
     * Base resolution width
     */
    private final int width = 800;

    /**
     * Base resolution height
     */
    private final int height = 600;
    private Stage stage;

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
     * Get the singleton App instance
     *
     * @return the app
     */
    public static App getInstance() {
        return instance;
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

        //Open game window
        openGame();
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

}