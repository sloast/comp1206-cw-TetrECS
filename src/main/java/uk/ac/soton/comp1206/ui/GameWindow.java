package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.BaseScene;
import uk.ac.soton.comp1206.scene.ChallengeScene;
import uk.ac.soton.comp1206.scene.InstructionsScene;
import uk.ac.soton.comp1206.scene.LobbyScene;
import uk.ac.soton.comp1206.scene.MenuScene;
import uk.ac.soton.comp1206.scene.MultiplayerScene;
import uk.ac.soton.comp1206.scene.ScoresScene;
import uk.ac.soton.comp1206.scene.SettingsScene;
import uk.ac.soton.comp1206.scene.SplashScene;

/**
 * The GameWindow is the single window for the game where everything takes place. To move between
 * screens in the game, we simply change the scene.
 * <p>
 * The GameWindow has methods to launch each of the different parts of the game by switching scenes.
 * You can add more methods here to add more screens to the game.
 */
public class GameWindow {

    private static final Logger logger = LogManager.getLogger(GameWindow.class);

    private static final boolean SHOW_SPLASH_SCREEN = true;
    private static final boolean SHOW_EXIT_SPLASH = true;
    final Communicator communicator;
    private final int width;
    private final int height;
    private final Stage stage;
    private BaseScene currentScene;
    private Scene scene;

    /**
     * Create a new GameWindow attached to the given stage with the specified width and height
     *
     * @param stage  stage
     * @param width  width
     * @param height height
     */
    public GameWindow(Stage stage, int width, int height) {
        this.width = width;
        this.height = height;

        this.stage = stage;

        //Setup window
        setupStage();

        //Setup resources
        setupResources();

        //Setup default scene
        setupDefaultScene();

        //Setup communicator
        communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");

        //Load first scene
        if (SHOW_SPLASH_SCREEN) {
            loadScene(new SplashScene(this));
        } else {
            startMenu();
        }
    }

    /**
     * Setup the font and any other resources we need
     */
    private void setupResources() {
        logger.info("Loading resources");

        //We need to load fonts here due to the Font loader bug with spaces in URLs in the CSS files
        Font.loadFont(getClass().getResourceAsStream("/style/manaspace.ttf"), 32);
    }

    /**
     * Display the main menu
     */
    public void startMenu() {
        loadScene(new MenuScene(this));
    }

    /**
     * Display the single player challenge
     */
    public void startChallenge() {
        loadScene(new ChallengeScene(this));
    }

    /**
     * Starts the multiplayer game
     *
     * @param username the player's nickname in the current channel
     */
    public void startMultiplayerGame(String username) {
        loadScene(new MultiplayerScene(this, username));
    }

    /**
     * Display the multiplayer lobby
     */
    public void startLobby() {
        loadScene(new LobbyScene(this));
    }

    /**
     * Display the instructions screen
     */
    public void startInstructions() {
        loadScene(new InstructionsScene(this));
    }

    /**
     * Display the settings screen
     */
    public void startSettings() {
        loadScene(new SettingsScene(this));
    }

    /**
     * Display high scores
     */
    public void startScores() {
        loadScene(new ScoresScene(this));
    }

    /**
     * Display the game over screen
     *
     * @param score the player's score in the current game
     */
    public void startScores(int score) {
        loadScene(new ScoresScene(this, score));
    }

    /**
     * Display the game over screen, with the specified multiplayer leaderboard replacing the local
     * scores
     *
     * @param score       the player's score in the current game
     * @param leaderboard the multiplayer leaderboard
     */
    public void startScores(int score, ScoresList leaderboard) {
        loadScene(new ScoresScene(this, score, leaderboard));
    }

    /**
     * Closes down the application.
     * If the exit splash is enabled, it will be shown before the application closes.
     */
    public void exitGame() {
        if (SHOW_EXIT_SPLASH && SHOW_SPLASH_SCREEN) {
            loadScene(new SplashScene(this, true));
        } else {
            App.getInstance().shutdown();
        }
    }

    /**
     * Set up the default settings for the stage itself (the window), such as the title and minimum
     * width and height.
     */
    public void setupStage() {
        stage.setTitle("SUPER TETRECS 64");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);
        stage.setOnCloseRequest(ev -> App.getInstance().shutdown());
    }

    /**
     * Load a given scene which extends BaseScene and switch over.
     *
     * @param newScene new scene to load
     */
    public void loadScene(BaseScene newScene) {
        //Cleanup remains of the previous scene
        cleanup();

        //Create the new scene and set it up
        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        stage.setScene(scene);

        //Initialise the scene when ready
        Platform.runLater(() -> currentScene.initialise());
    }

    /**
     * Set up the default scene (an empty black scene) when no scene is loaded
     */
    public void setupDefaultScene() {
        this.scene = new Scene(new Pane(), width, height, Color.BLACK);
        stage.setScene(this.scene);
    }

    /**
     * When switching scenes, perform any cleanup needed, such as removing previous listeners
     */
    public void cleanup() {
        logger.info("Clearing up previous scene");
        communicator.clearListeners();
    }

    /**
     * Get the current scene being displayed
     *
     * @return scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Get the width of the Game Window
     *
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the height of the Game Window
     *
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the communicator
     *
     * @return communicator
     */
    public Communicator getCommunicator() {
        return communicator;
    }
}