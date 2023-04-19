package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Colour;
import uk.ac.soton.comp1206.utils.Colour.TextColour;
import uk.ac.soton.comp1206.utils.Multimedia;
import uk.ac.soton.comp1206.utils.Vector2;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    public static boolean disableTimerActions = false;
    final IntegerProperty displayedScore = new SimpleIntegerProperty(0);
    final IntegerProperty highScore = new SimpleIntegerProperty(0);
    public PieceBoard currentPieceBoard;
    public PieceBoard nextPieceBoard;
    public BorderPane mainPane;
    Game game;
    GameBoard board;
    VBox livesContainer;
    GameTimer timer;
    HBox multiplierBox;
    private Transition multiplierTransition;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        mainPane = setupMain("challenge-background");

        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.,
                gameWindow.getWidth() / 2.);
        mainPane.setCenter(board);

        VBox pieceBoardContainer = new VBox();

        double currentPieceRatio = .25;
        this.currentPieceBoard = new PieceBoard(gameWindow.getWidth() * currentPieceRatio,
                gameWindow.getWidth() * currentPieceRatio);
        //game.currentPieceBoard = currentPieceBoard;
        pieceBoardContainer.getChildren().add(currentPieceBoard);

        double nextPieceRatio = .15;
        this.nextPieceBoard = new PieceBoard(gameWindow.getWidth() * nextPieceRatio,
                gameWindow.getWidth() * nextPieceRatio);
        //game.nextPieceBoard = nextPiece;
        pieceBoardContainer.getChildren().add(nextPieceBoard);

        pieceBoardContainer.alignmentProperty().set(Pos.CENTER);
        VBox.setMargin(nextPieceBoard, new Insets(20, 0, 0, 0));

        mainPane.setRight(pieceBoardContainer);

        // Header section for scores and multiplier
        var infoBox = new BorderPane();
        {

            var scoresBox = new VBox();
            {
                var scoreBox = new HBox();
                var scoreLabel = new Label("score ");
                var score = new Label("0");
                score.textProperty().bind(displayedScore.asString());
                game.score.addListener(this::onScoreChanged);

                scoreBox.getChildren().addAll(scoreLabel, score);
                score.getStyleClass().add("score");
                scoreLabel.getStyleClass().add("regularlabel");

                var highScoreBox = new HBox();
                var highScoreLabel = new Label("hi-score ");
                var highScoreText = new Label("0");
                loadHighScore();
                highScoreText.textProperty().bind(this.highScore.asString());
                highScoreBox.getChildren().addAll(highScoreLabel, highScoreText);
                highScoreText.getStyleClass().add("hiscore");
                highScoreLabel.getStyleClass().add("smalllabel");

                var levelBox = new HBox();
                var levelLabel = new Label("level ");
                var level = new Label("0");
                level.textProperty().bind(game.level.asString());
                levelBox.getChildren().addAll(levelLabel, level);
                level.getStyleClass().add("level");
                levelLabel.getStyleClass().add("smalllabel");

                scoresBox.getChildren().addAll(scoreBox, highScoreBox, levelBox);
            }
            infoBox.setLeft(scoresBox);

            multiplierBox = new HBox();
            multiplierBox.setAlignment(Pos.CENTER);
            var multiplierText = new Label("1");
            multiplierText.getStyleClass().add("multiplier");
            multiplierText.textProperty().bind(game.multiplier.asString().concat("x"));

            multiplierBox.getChildren().add(multiplierText);
            infoBox.setRight(multiplierBox);


        }
        mainPane.setTop(infoBox);

        mainPane.setPadding(new Insets(20, 20, 20, 20));

        // draws a heart icon for each life
        livesContainer = new VBox();
        {
            for (int i = 0; i < Game.MAX_LIVES; i++) {
                livesContainer.getChildren()
                        .add(new ImageView(Multimedia.getImage("heart.png", 80)));
            }

            livesContainer.alignmentProperty().set(Pos.CENTER);
            livesContainer.getChildren().forEach(node -> {
                node.setTranslateY(-40);
                node.setTranslateX(10);
            });
        }
        mainPane.setLeft(livesContainer);

        // The timer
        var timerContainer = new HBox();
        timer = new GameTimer(700, 20);
        timerContainer.getChildren().add(timer);
        timerContainer.setAlignment(Pos.CENTER);
        mainPane.setBottom(timerContainer);
        HBox.setMargin(timer, new Insets(20, 0, 20, 0));

    }

    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Handle when the mouse hovers over a block (to highlight it)
     *
     * @param gameBlock the Game Block that was hovered over
     */
    protected void blockHoverEnter(GameBlock gameBlock) {
        game.onBlockHoverEnter(gameBlock);
    }

    /**
     * Handle when the mouse exits a block
     *
     * @param gameBlock the Game Block that was exited
     */
    protected void blockHoverExit(GameBlock gameBlock) {
        game.onBlockHoverExit(gameBlock);
    }

    /**
     * Rotates the current piece clockwise
     *
     * @param ignored ignored
     */
    protected void rotateCurrentPiece(GameBlock ignored) {
        rotateCurrentPiece();
    }

    /**
     * Rotates the current piece clockwise
     */
    protected void rotateCurrentPiece() {
        Multimedia.playSound("rotate.wav", 0.5);
        game.rotateCurrentPiece();
    }

    /**
     * Rotates the current piece counter-clockwise
     */
    protected void rotateCurrentPieceCounterClockwise() {
        Multimedia.playSound("rotate.wav", 0.5);
        game.rotateCurrentPieceCounterClockwise();
    }

    /**
     * Swap the current piece with the next piece
     */
    protected void swapPieces() {
        Multimedia.playSound("swap.wav", 0.5);
        game.swapPieces();
    }

    /**
     * Set up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
        disableTimerActions = Game.USE_INTERNAL_TIMER;
    }

    /**
     * Load the high score from the scores.txt file and set the highScore property
     */
    void loadHighScore() {
        highScore.set(10000);
        try {
            BufferedReader reader = new BufferedReader(new FileReader("scores.txt"));
            String line = reader.readLine();
            highScore.set(Integer.parseInt(line.split(":")[1].trim()));
        } catch (Exception e) {
            logger.error(Colour.error("Error reading high score: " + e));
            logger.info(Colour.cyan("Creating template scores file..."));

            try {
                Path defaultScoreFile = Path.of(Objects.requireNonNull(
                        ChallengeScene.class.getResource("/misc/default-scores.txt")).toURI());
                List<String> defaultScores = Files.readAllLines(defaultScoreFile);

                Files.write(Path.of("scores.txt"), defaultScores);

                logger.info(Colour.cyan("Template scores file created"));

            } catch (Exception ex) {
                logger.error(Colour.error("Error creating template scores file: " + ex));
            }
        }
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        // Set up event listeners
        game.setOnLineCleared(s -> board.lineCleared(s, mainPane));
        game.setOnPieceBoardUpdate((nextPiece, followingPiece) -> {
            this.currentPieceBoard.setPiece(nextPiece);
            this.nextPieceBoard.setPiece(followingPiece);
        });
        game.setOnGameOver(this::gameOver);
        game.setOnGameLoop(this.timer::reset);

        // Play sound on new level
        game.level.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > oldValue.intValue()) {
                Multimedia.playSoundDelayed("level.wav", 300, 1, true);
            }
        });

        game.multiplier.addListener(this::onMultiplierChanged);

        this.displayedScore.addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                    Number newValue) {
                if (newValue.intValue() > highScore.get()) {
                    highScore.bind(displayedScore);
                    ChallengeScene.this.displayedScore.removeListener(this);
                }
            }
        });

        mainPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                rotateCurrentPiece();
                logger.debug(Colour.orange("Intercepted right click"));
                e.consume();
            }
        });

        scene.setOnKeyPressed(this::onKeyPress);

        // To update the lives HUD element
        game.lives.addListener(this::onLivesChanged);

        // Handle block on GameBoard grid being clicked
        board.setOnBlockLeftClick(this::blockClicked);
        board.setOnBlockRightClick(this::rotateCurrentPiece);
        board.setOnBlockHoverEnter(this::blockHoverEnter);
        board.setOnBlockHoverExit(this::blockHoverExit);

        // Handle PieceBoard being clicked
        currentPieceBoard.setOnAnyBlockClick(this::rotateCurrentPiece);
        nextPieceBoard.setOnAnyBlockClick(e -> swapPieces());

        Multimedia.fadeOutMusic(() -> Multimedia.startMusicIntro("game_start.wav", "game.wav"));

        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                timer.speedUp(false);
            }
        });
        if (App.DEBUG_MODE) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, this::testingKeyBinds);
        }

        game.start();
    }

    /**
     * Handle keypress
     *
     * @param event The keyboard event
     */
    private void onKeyPress(KeyEvent event) {
        var keyCode = event.getCode();
        logger.debug("Key pressed: " + keyCode);
        switch (keyCode) {
            case LEFT, A -> onArrowKeyPressed(Vector2.left());
            case RIGHT, D -> onArrowKeyPressed(Vector2.right());
            case DOWN, S -> onArrowKeyPressed(Vector2.down());
            case UP, W -> onArrowKeyPressed(Vector2.up());
            case E, C, CLOSE_BRACKET -> rotateCurrentPiece();
            case Q, Z, OPEN_BRACKET -> rotateCurrentPieceCounterClockwise();
            case R, SPACE -> this.swapPieces();
            case ENTER -> game.keyboardPlayPiece();
            case SHIFT -> timer.speedUp(true);
            case ESCAPE -> {
                game.stop();
                gameWindow.startMenu();
            }
        }
    }

    /**
     * Handles keybinds used for testing
     */
    private void testingKeyBinds(KeyEvent event) {
        var keyCode = event.getCode();
        switch (keyCode) {
            case NUMBER_SIGN -> game.resetBoard();
            case N -> game.nextPiece();
            case EQUALS -> game.score(1, 5);
            case L -> game.loseLife();
            case DIGIT0 -> timer.stop();
        }
    }

    /**
     * Update UI when player loses a life
     */
    private void onLivesChanged(ObservableValue<? extends Number> observableValue, Number oldValue,
            Number newValue) {
        logger.info(Colour.orange("Lives changed: " + oldValue + " -> " + newValue));
        int max = Game.MAX_LIVES;
        for (int i = 0; i < max; i++) {
            if (i < newValue.intValue()) {
                ((ImageView) livesContainer.getChildren().get(max - i - 1)).setImage(
                        Multimedia.getImage("heart.png", 80));
            } else {
                ((ImageView) livesContainer.getChildren().get(max - i - 1)).setImage(
                        Multimedia.getImage("heart_empty.png", 80));
            }
        }

        if (newValue.intValue() < oldValue.intValue()) {
            Multimedia.playSound("lifelose.wav");
        }
    }

    /**
     * Update UI when multiplier changes
     */
    private void onMultiplierChanged(ObservableValue<? extends Number> observableValue,
            Number oldValue, Number newValue) {
        logger.info(Colour.green("Multiplier changed: " + oldValue + " -> " + newValue));
        String color = "white";
        double scale = 1D;
        boolean animate = false;
        switch (newValue.intValue()) {
            case 1 -> {
            }
            case 2 -> {
                color = "yellow";
                scale = 1.25D;
            }
            case 3 -> {
                color = "orange";
                scale = 1.6D;
            }
            case 4 -> {
                color = "red";
                scale = 1.75D;
                animate = true;
            }
            default -> {
                color = "red";
                scale = 2D;
                animate = true;
            }
        }

        String styleString = "-fx-text-fill: " + color + ";";
        multiplierBox.getChildren().forEach((item) -> item.setStyle(styleString));

        animateMultiplier(scale);

        if (animate) {
            Duration startPoint = Duration.millis(500);

            if (multiplierTransition != null) {
                startPoint = multiplierTransition.getCurrentTime();
                multiplierTransition.stop();
            }

            var rotateTransition = new RotateTransition(Duration.millis(1000), multiplierBox);
            rotateTransition.setFromAngle(-15);
            rotateTransition.setToAngle(15);
            rotateTransition.setCycleCount(Animation.INDEFINITE);
            rotateTransition.setAutoReverse(true);

            var scaleTransition = new ScaleTransition(Duration.millis(1400), multiplierBox);
            scaleTransition.setFromX(scale * 0.9);
            scaleTransition.setFromY(scale * 0.8);
            scaleTransition.setToX(scale * 1.1);
            scaleTransition.setToY(scale * 1.2);
            scaleTransition.setCycleCount(Animation.INDEFINITE);
            scaleTransition.setAutoReverse(true);

            multiplierTransition = new ParallelTransition(rotateTransition, scaleTransition);

            multiplierTransition.playFrom(startPoint);

        } else {
            if (multiplierTransition != null) {
                multiplierTransition.stop();
                multiplierTransition = null;

                RotateTransition returnTransition = new RotateTransition(Duration.millis(100),
                        multiplierBox);
                returnTransition.setFromAngle(multiplierBox.getRotate());
                returnTransition.setToAngle(0);
                returnTransition.play();
            }
        }
    }

    /**
     * Changes the size of the multiplier box
     *
     * @param scale the relative size to change to
     */
    private void animateMultiplier(double scale) {
        double offsetX = -25;
        double offsetY = 0;
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(100),
                multiplierBox);
        translateTransition.setFromX(multiplierBox.getTranslateX());
        translateTransition.setFromY(multiplierBox.getTranslateY());
        translateTransition.setToX((scale - 1) * offsetX);
        translateTransition.setToY((scale - 1) * offsetY);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), multiplierBox);
        scaleTransition.setFromX(multiplierBox.getScaleX());
        scaleTransition.setFromY(multiplierBox.getScaleY());
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);

        translateTransition.play();
        scaleTransition.play();
    }

    /**
     * Handle arrow keys being pressed
     *
     * @param direction The direction represented by the arrow key
     */
    private void onArrowKeyPressed(Vector2 direction) {
        if (!game.isRunning()) {
            return;
        }

        logger.debug(Colour.cyan("Arrow key pressed: " + direction));

        var pos = new Vector2(game.getCols() / 2, game.getRows() / 2);
        var hoveredBlock = game.hoveredBlock;

        if (hoveredBlock != null) { // && game.usingKeyboard) {
            pos.x = hoveredBlock.getX();
            pos.y = hoveredBlock.getY();

            pos = pos.add(direction)
                    .clamp(Vector2.zero(), new Vector2(game.getCols() - 1, game.getRows() - 1));
        }

        hoveredBlock = board.getBlock(pos.x, pos.y);

        game.hoverBlockKeyboard(hoveredBlock);
    }

    /**
     * Called when the game ends
     */
    private void gameOver() {
        logger.info(Colour.red("Game Over"));
        Multimedia.queueMusic("end.wav", 1);
        timer.scaleTransition.stop();
        //gameWindow.startMenu();
        startScores();
    }

    /**
     * Start the ScoresScene
     */
    void startScores() {
        gameWindow.startScores(game.getScore());
    }

    /**
     * Play an animation for the score when it changes
     */
    void onScoreChanged(Observable observable, Number oldValue, Number newValue) {
        Timeline anim = new Timeline();
        anim.getKeyFrames().add(new KeyFrame(Duration.ZERO,
                new KeyValue(displayedScore, displayedScore.get())));
        anim.getKeyFrames()
                .add(new KeyFrame(Duration.millis(300), new KeyValue(displayedScore, newValue)));
        anim.play();
    }


    /**
     * The Timer at the bottom of the screen
     */
    public class GameTimer extends Rectangle {

        private ScaleTransition scaleTransition;
        private int colorStage = 2;
        private Timeline colorAnimation;

        /**
         * Create a new GameTimer
         *
         * @param width  the starting width of the timer
         * @param height the height of the timer
         */
        public GameTimer(double width, double height) {
            super(0, 0, width, height);
            this.setFill(Color.GREEN);
        }

        /**
         * Reset the timer bar to the start
         *
         * @param ms the duration of the animation, in milliseconds
         */
        public void reset(double ms) {
            //timer = new Rectangle(0, 0, 700, 40);
            if (colorAnimation != null) {
                colorAnimation.stop();
            }

            // Reset the color. Is delayed to allow the previous animation to fully stop first
            Platform.runLater(() -> {
                this.setFill(Color.GREEN);
                this.setScaleX(1);
            });
            this.setFill(Color.GREEN);
            colorStage = 2;

            // Resets the scaleTransition if it exists
            if (scaleTransition != null) {
                scaleTransition.stop();
                this.setFill(Color.GREEN);
                scaleTransition.setDuration(Duration.millis(ms));
                scaleTransition.play();
                return;
            }

            scaleTransition = new ScaleTransition(Duration.millis(ms), this);

            scaleTransition.setFromX(1);
            scaleTransition.setToX(0);
            scaleTransition.setInterpolator(Interpolator.LINEAR);

            if (!disableTimerActions) {
                scaleTransition.setOnFinished(e -> game.loseLife());
            }

            scaleTransition.play();

            // May not be the best way to do this, but it works with the speedup button.
            // Checks 10 times per second if the color should be changed.
            ScheduledExecutorService checkColor = Executors.newSingleThreadScheduledExecutor();
            checkColor.scheduleAtFixedRate(() -> {
                if (colorStage == 2 && getProportionComplete() > 0.5) {
                    animateColour(Color.YELLOW);
                    colorStage = 1;
                } else if (colorStage == 1 && getProportionComplete() > 0.75) {
                    animateColour(Color.RED);
                    colorStage = 0;
                }
            }, 100, 100, TimeUnit.MILLISECONDS);
        }

        /**
         * Create and start a new transition from the current colour to {@code endColor}
         * <br>Stops any previous color transition
         */
        private void animateColour(Color endColor) {
            if (colorAnimation != null) {
                colorAnimation.stop();
            }
            colorAnimation = new Timeline(
                    new KeyFrame(Duration.millis(scaleTransition.getDuration().toMillis() / 5),
                            new KeyValue(this.fillProperty(), endColor)));
            colorAnimation.play();
        }

        /**
         * Returns the proportion of the animation that has elapsed, from 0 to 1
         */
        private double getProportionComplete() {
            var time = scaleTransition.getCurrentTime();
            var duration = scaleTransition.getDuration();
            return time.toMillis() / duration.toMillis();
        }


        /**
         * Speed up the timer by 4x
         */
        public void speedUp(boolean active) {
            // This feature makes everything else in the timer more complicated

            game.setTimerSpeed(active);
            var time = scaleTransition.getCurrentTime();
            var duration = scaleTransition.getDuration();
            var atNormalSpeed = duration.equals(game.getTimerDelay());
            if (active && atNormalSpeed) {
                time = time.multiply(0.25);
                duration = duration.multiply(0.25);
            } else if (!active && !atNormalSpeed) {
                duration = game.getTimerDelay();
                time = time.multiply(4);
            }
            scaleTransition.stop();
            scaleTransition.setDuration(duration);
            scaleTransition.playFrom(time);
        }

        /**
         * Cancels the timer
         */
        public void stop() {
            scaleTransition.stop();
        }
    }
}