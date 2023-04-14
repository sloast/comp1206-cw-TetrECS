package uk.ac.soton.comp1206.scene;

import java.util.List;
import java.util.UUID;
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
    }

    public void updateTitle() {
        var writeableImage = new WritableImage(128, 64);
        titleImg.snapshot(null, writeableImage);
        title.setFill(javafx.scene.paint.Color.WHITE);
        title.setOpacity(1);
        var reader = writeableImage.getPixelReader();

        StringBuilder text = new StringBuilder();

        for (int y = 0; y < writeableImage.getHeight(); y += 4) {
            for (int x = 0; x < writeableImage.getWidth(); x += 2) {
                var color = reader.getColor(x, y);
                if (color.getBrightness() > .1) {
                    text.append("#");
                } else {
                    text.append(" ");
                }

            }
            text.append("\n");
        }

        title.setText(text.toString());
    }

    private void makeTextTitle() {
        titleBox.getChildren().add(titleImg);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setMaxHeight(100);
        titleBox.setStyle("-fx-background-color: black;");
        //mainPane.setLeft(titleBox);
        RotateTransition rotateTitle = new RotateTransition(Duration.millis(1000), titleImg);
        rotateTitle.setFromAngle(-10);
        rotateTitle.setToAngle(10);
        rotateTitle.setAutoReverse(true);
        rotateTitle.setCycleCount(Timeline.INDEFINITE);
        rotateTitle.play();

        title = new Text();
        title.getStyleClass().add("titleart");
        var titleImgContainer = new HBox();
        //titleImg.setFitWidth(400);
        //titleImg.setPreserveRatio(true);
        titleImgContainer.getChildren().add(title);
        titleImgContainer.setAlignment(Pos.CENTER);
        titleImgContainer.setPadding(new Insets(40, 20, 20, 20));
        mainPane.setTop(titleImgContainer);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100),
                event -> updateTitle()));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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
        menuBox.setAlignment(Pos.CENTER);
        //menuBox.getStyleClass().add("menuItem");
        mainPane.setCenter(menuBox);

        menuItems = menuBox.getChildren();

        var startSinglePlayer = new MenuItem("Single Player", this::startGame);
        menuItems.add(startSinglePlayer);

        var multiplayer = new MenuItem("Multiplayer", e -> gameWindow.startLobby());
        menuItems.add(multiplayer);

        var instructions = new MenuItem("Instructions", this::startInstructions);
        menuItems.add(instructions);

        var settings = new MenuItem("Settings", null);
        settings.setOnAction(e -> settings.setText("Coming soon"));
        menuItems.add(settings);

        var exit = new MenuItem("Exit", e -> gameWindow.exitGame());
        menuItems.add(exit);

        menuSize = menuItems.size();

        backgroundPane.opacityProperty().set(0);
        var backgroundFade = new FadeTransition(Duration.millis(1000), backgroundPane);
        backgroundFade.setFromValue(0);
        backgroundFade.setToValue(1);
        backgroundFade.play();

        animate(titleImgContainer, 0, Duration.millis(400));
        animate(startSinglePlayer, 500);
        animate(multiplayer, 600);
        animate(instructions, 700);
        animate(settings, 800);
        animate(exit, 900);


    }

    public void animate(Node node, int delay, Duration duration) {
        node.setTranslateY(gameWindow.getHeight());
        TranslateTransition slideOn = new TranslateTransition(duration, node);
        slideOn.setFromY(gameWindow.getHeight());
        slideOn.setToY(0);
        slideOn.setInterpolator(Interpolator.LINEAR);
        slideOn.setDelay(Duration.millis(delay));
        slideOn.setOnFinished(e -> Multimedia.playSound("hit1.wav"));
        slideOn.play();
    }

    public void animate(Node node, int delay) {
        animate(node, delay, Duration.millis(500));
    }

    public void onKeyPress(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case ESCAPE -> gameWindow.exitGame();
            case S -> gameWindow.startScores();
            case M -> {
                var communicator = gameWindow.getCommunicator();
                communicator.send("PART");
                communicator.send("CREATE " + UUID.randomUUID());
                //communicator.send("NICK " + "MyPlayer");
                communicator.send("NICK " + UUID.randomUUID());
                communicator.send("START");
                communicator.addListener((m) -> {
                    if (m.equals("START")) Platform.runLater(gameWindow::startMultiplayerGame);
                });
            }
            case J -> {
                var communicator = gameWindow.getCommunicator();
                communicator.send("PART");
                communicator.send("JOIN " + "a8d485f084bc984");
                communicator.send("NICK " + "MyPlayer");
                communicator.addListener((m) -> {
                    if (m.equals("START")) Platform.runLater(gameWindow::startMultiplayerGame);
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
        Multimedia.playSound("hit1.wav");
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

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(this::onKeyPress);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onArrowPress);
    }

    /**
     * Handle when the Start Game button is pressed
     *
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    private void startInstructions(ActionEvent event) {
        gameWindow.startInstructions();
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