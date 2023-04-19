package uk.ac.soton.comp1206.scene;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private final BorderPane mainPane = new BorderPane();

    /**
     * The items in the menu, in order from top to bottom
     */
    private List<Node> menuItems;

    /**
     * The number of items in the menu
     */
    private int menuSize;

    /**
     * The index of the currently selected menu item
     */
    private int menuIndex = -1;

    /**
     * Create a new menu scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
        gameWindow.getCommunicator().clearListeners();
    }

    /**
     * Create the title image
     *
     * @param parent the parent node to add the image to
     * @return the resulting node
     */
    private Node makeTitleImg(BorderPane parent) {
        var titleImg = new ImageView(Multimedia.getImage("TetrECS.png"));
        var titleImgContainer = new HBox();
        titleImg.setFitWidth(400);
        titleImg.setPreserveRatio(true);
        titleImgContainer.getChildren().add(titleImg);
        titleImgContainer.setAlignment(Pos.CENTER);
        titleImgContainer.setPadding(new Insets(40, 20, 20, 20));
        parent.setTop(titleImgContainer);
        return titleImgContainer;
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var backgroundPane = new StackPane();
        backgroundPane.setMaxWidth(gameWindow.getWidth());
        backgroundPane.setMaxHeight(gameWindow.getHeight());
        backgroundPane.getStyleClass().add("challenge-background");
        root.getChildren().add(backgroundPane);

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        //menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        //var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        mainPane.setPadding(new Insets(20, 20, 20, 20));

        var titleImgContainer = makeTitleImg(mainPane);

        var menuBox = new VBox();
        menuBox.setSpacing(0);
        menuBox.setAlignment(Pos.BOTTOM_CENTER);
        //menuBox.getStyleClass().add("menuItem");
        mainPane.setCenter(menuBox);

        menuItems = menuBox.getChildren();

        // Create and add the menu items

        var startSinglePlayer = new MenuItem("Single Player", this::startGame);
        menuItems.add(startSinglePlayer);

        var multiplayer = new MenuItem("Multiplayer", this::startLobby);
        menuItems.add(multiplayer);

        var instructions = new MenuItem("Instructions", this::startInstructions);
        menuItems.add(instructions);

        var highScores = new MenuItem("High Scores", this::startScores);
        menuItems.add(highScores);

        var settings = new MenuItem("Settings", this::startSettings);
        menuItems.add(settings);

        var exit = new MenuItem("Exit", this::exit);
        menuItems.add(exit);

        menuSize = menuItems.size();

        backgroundPane.opacityProperty().set(0);
        var backgroundFade = new FadeTransition(Duration.millis(2000), backgroundPane);
        backgroundFade.setFromValue(0);
        backgroundFade.setToValue(1);
        backgroundFade.play();

        // Animate the title
        animateTitle(titleImgContainer);

        // Animate the menu items
        int startDelay = 500;
        for (Node node : menuItems) {
            animate(node, Duration.millis(startDelay));
            startDelay += 100;
        }
    }

    /**
     * Animates the given node sliding on from the bottom of the screen
     *
     * @param node the node to animate
     * @param delay the delay before the animation starts
     * @param duration the duration of the animation
     */
    private void animate(Node node, Duration delay, Duration duration) {
        node.setTranslateY(gameWindow.getHeight());
        TranslateTransition slideOn = new TranslateTransition(duration, node);
        slideOn.setFromY(gameWindow.getHeight());
        slideOn.setToY(0);
        slideOn.setInterpolator(Interpolator.LINEAR);
        slideOn.setDelay(delay);
        slideOn.setOnFinished(e -> beep());
        slideOn.play();
    }

    /**
     * Animates the given node sliding on from the bottom of the screen
     *
     * @param node the node to animate
     * @param delay the delay before the animation starts
     */
    private void animate(Node node, Duration delay) {
        animate(node, delay, Duration.millis(500));
    }

    /**
     * Set up the title animation
     *
     * @param title the node to animate
     */
    private void animateTitle(Node title) {
        // Bounce back and forth horizontally
        TranslateTransition moveX = new TranslateTransition(Duration.millis(6400), title);
        moveX.setFromX(-205);
        moveX.setToX(205);
        moveX.setInterpolator(Interpolator.LINEAR);
        moveX.setCycleCount(Animation.INDEFINITE);
        moveX.setAutoReverse(true);

        // Bounce back and forth vertically
        TranslateTransition moveY = new TranslateTransition(Duration.millis(2700), title);
        moveY.setFromY(95);
        moveY.setToY(-65);
        // Bounce normally at the top, but smoothly turn at the bottom
        moveY.setInterpolator(Interpolator.EASE_IN);
        moveY.setCycleCount(Animation.INDEFINITE);
        moveY.setAutoReverse(true);

        // Fade the title in gradually at the start
        FadeTransition fade = new FadeTransition(Duration.millis(1000), title);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(1000));
        title.setOpacity(0);

        moveX.play();
        moveY.play();
        fade.play();
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

        // Listen for keypresses
        scene.setOnKeyPressed(this::onKeyPress);

        // Listen for arrow keys, and handle them before they are consumed by the scene
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onArrowPressed);

        // Start the music if it isn't already playing
        if (!Multimedia.isPlayingMusic("menu.mp3")) {
            Multimedia.fadeOutMusic(() -> Multimedia.startMusic("menu.mp3"));
        }
    }

    /**
     * Handle when a key is pressed
     *
     * @param keyEvent the key event
     */
    public void onKeyPress(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        logger.info("Key pressed: " + keyCode);
        if (Objects.requireNonNull(keyCode) == KeyCode.ESCAPE) {
            exit();
        }

        // Keybinds for testing
        if (!App.DEBUG_MODE) {
            return;
        }

        switch (keyCode) {
            // Simulate ending a game with the given score
            case DIGIT8 -> gameWindow.startScores(7654);
            case DIGIT9 -> gameWindow.startScores(98765);

            // Create a new game and start it
            case M -> {
                var communicator = gameWindow.getCommunicator();
                communicator.send("PART");
                communicator.send("CREATE " + UUID.randomUUID());
                communicator.send("NICK " + UUID.randomUUID());
                javafx.beans.property.StringProperty myNick =
                        new javafx.beans.property.SimpleStringProperty();
                communicator.addListener((m) -> {
                    var split = m.split(" ", 2);
                    if (split[0].equals("NICK")) {
                        myNick.set(split[1]);
                        communicator.send("START");
                    }
                });
                communicator.addListener((m) -> {
                    if (m.equals("START")) {
                        Platform.runLater(() -> gameWindow.startMultiplayerGame(myNick.get()));
                        communicator.clearListeners();
                    }
                });
            }
            case L -> gameWindow.startLobby();
        }
    }

    /**
     * Handles arrow key (or WASD) presses for keyboard navigation
     *
     * @param keyEvent the key event
     */
    private void onArrowPressed(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        //logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case UP, W -> selectMenuItem(-1);
            case DOWN, S -> selectMenuItem(1);
            default -> {
                return;
            }
        }
        Multimedia.playSound("beep.wav");
        keyEvent.consume();
    }

    /**
     * Selects the next menu item in the given direction
     *
     * @param direction {@code -1} for up, {@code 1} for down
     */
    private void selectMenuItem(int direction) {
        // if nothing is selected, select the first item
        if (menuIndex == -1) {
            menuIndex = 0;
            var selected = menuItems.get(menuIndex);
            ((MenuItem) selected).setSelected(true);
            return;
        }

        var selected = menuItems.get(menuIndex);
        ((MenuItem) selected).setSelected(false);
        menuIndex += direction;

        // wrap around the ends of the menu
        if (menuIndex < 0) {
            menuIndex = menuSize - 1;
        } else if (menuIndex >= menuSize) {
            menuIndex = 0;
        }

        selected = menuItems.get(menuIndex);
        ((MenuItem) selected).setSelected(true);
    }

    /**
     * Clears the current menu selection when the mouse hovers over a menu item
     */
    private void clearSelection() {
        // if nothing is selected, do nothing
        if (menuIndex == -1) {
            return;
        }

        // deselect the current menu item
        var selected = menuItems.get(menuIndex);
        ((MenuItem) selected).setSelected(false);

        // reset the selection
        menuIndex = -1;
    }

    /**
     * Plays a beep sound
     */
    private void beep() {
        Multimedia.playSound("beep.wav", 0.5);
    }

    /**
     * Start the single player game
     */
    private void startGame() {
        gameWindow.startChallenge();
    }

    /**
     * Opens the multiplayer lobby
     */
    private void startLobby() {
        gameWindow.startLobby();
    }

    /**
     * Opens the high scores list
     */
    private void startScores() {
        gameWindow.startScores();
    }

    /**
     * Opens the instructions screen
     */
    private void startInstructions() {
        gameWindow.startInstructions();
    }

    /**
     * Opens the settings menu
     */
    private void startSettings() {
        gameWindow.startSettings();
    }

    /**
     * Exit the game
     */
    private void exit() {
        gameWindow.getCommunicator().send("QUIT");
        gameWindow.exitGame();
    }

    /**
     * Represents a menu item
     */
    public class MenuItem extends Button {

        /**
         * Creates a new menu item
         *
         * @param name the name of the menu item
         * @param onAction the action to perform when the menu item is clicked
         */
        public MenuItem(String name, Runnable onAction) {
            super(name);
            getStyleClass().add("menuItem");

            // Call onAction when clicked
            setOnAction(e -> {
                beep();
                onAction.run();
            });

            //clear keyboard selection when hovered
            setOnMouseEntered(e -> MenuScene.this.clearSelection());
        }

        /**
         * Sets whether this menu item is selected
         *
         * @param focused true if selected
         */
        public void setSelected(boolean focused) {
            if (focused) {
                getStyleClass().add("selected");
                requestFocus();
            } else {
                getStyleClass().remove("selected");
            }
        }
    }
}