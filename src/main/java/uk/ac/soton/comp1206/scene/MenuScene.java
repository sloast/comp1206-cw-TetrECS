package uk.ac.soton.comp1206.scene;

import java.util.List;
import java.util.UUID;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    private final ImageView titleImg = new ImageView(Multimedia.getImage("TetrECS_small.png"));
    private final VBox titleBox = new VBox();
    private final VBox menuBox = new VBox();
    private List<Node> menuItems;
    private int menuSize;
    private int menuIndex = -1;
    private Text title;

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

        //var menuBox = new VBox();
        menuBox.setSpacing(0);
        menuBox.setAlignment(Pos.BOTTOM_CENTER);
        //menuBox.getStyleClass().add("menuItem");
        mainPane.setCenter(menuBox);

        menuItems = menuBox.getChildren();

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

        setupTitle(titleImgContainer);

        int startDelay = 500;

        for (Node node : menuItems) {
            animate(node, startDelay);
            startDelay += 100;
        }
    }

    public void animate(Node node, int delay, Duration duration) {
        node.setTranslateY(gameWindow.getHeight());
        TranslateTransition slideOn = new TranslateTransition(duration, node);
        slideOn.setFromY(gameWindow.getHeight());
        slideOn.setToY(0);
        slideOn.setInterpolator(Interpolator.LINEAR);
        slideOn.setDelay(Duration.millis(delay));
        slideOn.setOnFinished(e -> beep());
        slideOn.play();
    }

    public void animate(Node node, int delay) {
        animate(node, delay, Duration.millis(500));
    }

    public void setupTitle(Node title) {
        TranslateTransition moveX = new TranslateTransition(Duration.millis(6400), title);
        moveX.setFromX(-205);
        moveX.setToX(205);
        moveX.setInterpolator(Interpolator.LINEAR);
        moveX.setCycleCount(Animation.INDEFINITE);
        moveX.setAutoReverse(true);

        TranslateTransition moveY = new TranslateTransition(Duration.millis(2700), title);
        moveY.setFromY(95);
        moveY.setToY(-65);
        moveY.setInterpolator(Interpolator.EASE_IN);
        moveY.setCycleCount(Animation.INDEFINITE);
        moveY.setAutoReverse(true);

        moveX.play();
        moveY.play();

        FadeTransition fade = new FadeTransition(Duration.millis(1000), title);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(1000));

        title.setOpacity(0);

        fade.play();
    }

    public void onKeyPress(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case ESCAPE -> exit();
        }

        // For testing

        if (!App.DEBUG_MODE) {
            return;
        }

        switch (keyCode) {
            case S -> gameWindow.startScores(7654);
            case DIGIT9 -> gameWindow.startScores(98765);
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

    public void onArrowPress(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        //logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case UP -> selectMenuItem(-1);
            case DOWN -> selectMenuItem(1);
            default -> {
                return;
            }
        }
        Multimedia.playSound("beep.wav");
        keyEvent.consume();
    }

    private void selectMenuItem(int direction) {
        if (menuIndex == -1) {
            menuIndex = 0;
            var selected = menuItems.get(menuIndex);
            ((MenuItem) selected).setSelected(true);
            return;
        }

        var selected = menuItems.get(menuIndex);
        ((MenuItem) selected).setSelected(false);
        menuIndex += direction;
        if (menuIndex < 0) {
            menuIndex = menuSize - 1;
        } else if (menuIndex >= menuSize) {
            menuIndex = 0;
        }
        selected = menuItems.get(menuIndex);
        ((MenuItem) selected).setSelected(true);
    }

    private void clearSelection() {
        if (menuIndex == -1) {
            return;
        }
        var selected = menuItems.get(menuIndex);
        ((MenuItem) selected).setSelected(false);

        // should it reset back to the top?
        //menuIndex = -1;
    }

    private void beep() {
        Multimedia.playSound("beep.wav", 0.5);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(this::onKeyPress);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onArrowPress);

        if (!Multimedia.isPlayingMusic("menu.mp3")) {
            Multimedia.fadeOutMusic(() -> Multimedia.startMusic("menu.mp3"));
        }
    }

    /**
     * Handle when the Start Game button is pressed
     *
     * @param event event
     */
    private void startGame(ActionEvent event) {
        beep();
        gameWindow.startChallenge();
    }

    private void startLobby(ActionEvent event) {
        beep();
        gameWindow.startLobby();
    }

    private void startScores(ActionEvent event) {
        beep();
        gameWindow.startScores();
    }

    private void startInstructions(ActionEvent event) {
        beep();
        gameWindow.startInstructions();
    }

    private void startSettings(ActionEvent event) {
        beep();
        gameWindow.startSettings();
    }

    private void exit(ActionEvent event) {
        exit();
    }

    private void exit() {
        beep();
        gameWindow.getCommunicator().send("QUIT");
        gameWindow.exitGame();
    }

    public class MenuItem extends Button {

        public MenuItem(String name, EventHandler<ActionEvent> eventHandler) {
            super(name);
            getStyleClass().add("menuItem");
            setOnAction(eventHandler);

            //clear keyboard selection when hovered
            setOnMouseEntered(e -> MenuScene.this.clearSelection());
        }

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