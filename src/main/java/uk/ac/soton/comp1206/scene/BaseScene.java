package uk.ac.soton.comp1206.scene;

import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * A Base Scene used in the game. Handles common functionality between all scenes.
 */
public abstract class BaseScene {

    protected final GameWindow gameWindow;

    /**
     * The root pane
     */
    protected GamePane root;

    /**
     * The {@link Scene} object for this scene
     */
    protected Scene scene;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public BaseScene(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Initialise this scene. Called after creation
     */
    public abstract void initialise();

    /**
     * Build the layout of the scene
     */
    public abstract void build();

    /**
     * Create a new JavaFX scene using the root contained within this scene
     *
     * @return JavaFX scene
     */
    public Scene setScene() {
        var previous = gameWindow.getScene();
        Scene scene = new Scene(root, previous.getWidth(), previous.getHeight(), Color.BLACK);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/style/game.css")).toExternalForm());
        this.scene = scene;
        return scene;
    }

    /**
     * Set up the basic UI elements that are required for every scene, and return the
     * {@link BorderPane} that elements can be added to.
     *
     * @return the main pane
     */
    protected BorderPane setupMain() {
        return setupMain("main-background");
    }

    /**
     * Set up the basic UI elements that are required for every scene, and return the
     * {@link BorderPane} that elements can be added to.
     *
     * @param styleClass the style class to apply to the background
     * @return the main pane
     */
    protected BorderPane setupMain(String styleClass) {
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var stackPane = new StackPane();
        stackPane.setMaxWidth(gameWindow.getWidth());
        stackPane.setMaxHeight(gameWindow.getHeight());
        stackPane.getStyleClass().add(styleClass);
        root.getChildren().add(stackPane);

        var mainPane = new BorderPane();
        stackPane.getChildren().add(mainPane);

        return mainPane;
    }

    /**
     * Get the JavaFX scene contained inside
     *
     * @return JavaFX scene
     */
    public Scene getScene() {
        return this.scene;
    }

}