package uk.ac.soton.comp1206.scene;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import uk.ac.soton.comp1206.utils.Multimedia;
import uk.ac.soton.comp1206.utils.Multimedia.Category;
import uk.ac.soton.comp1206.utils.Vector2;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {
    
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    public PieceBoard currentPieceBoard;
    public PieceBoard nextPieceBoard;
    public ScheduledExecutorService gameTimer;
    public BorderPane mainPane;
    Game game;
    GameBoard board;
    VBox livesContainer;
    GameTimer timer;
    public static boolean disableTimerActions = false;
    final IntegerProperty displayedScore = new SimpleIntegerProperty(0);


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
                var highScore = new Label("10000");
                highScoreBox.getChildren().addAll(highScoreLabel, highScore);
                highScore.getStyleClass().add("hiscore");
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

            var multiplierBox = new HBox();
            var multiplier = new Label("1");
            var multiplierX = new Label("x");
            multiplier.getStyleClass().add("multiplier");
            multiplierX.getStyleClass().add("regularlabel");
            multiplier.textProperty().bind(game.multiplier.asString());

            multiplierBox.getChildren().addAll(multiplier, multiplierX);
            infoBox.setRight(multiplierBox);


        }
        mainPane.setTop(infoBox);

        mainPane.setPadding(new Insets(20, 20, 20, 20));

        // draws a heart icon for each life
        livesContainer = new VBox();
        {
            for (int i = 0; i < game.MAX_LIVES; i++) {
                livesContainer.getChildren()
                        .add(new ImageView(Multimedia.getImage("heart_small.png", 80)));
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
     */
    protected void blockHoverEnter(GameBlock gameBlock) {
        game.onBlockHoverEnter(gameBlock);
    }

    /**
     * Handle when the mouse exits a block
     */
    protected void blockHoverExit(GameBlock gameBlock) {
        game.onBlockHoverExit(gameBlock);
    }

    /**
     * For when the right mouse button is pressed or e pressed
     */
    protected void rotateCurrentPiece(GameBlock ignored) {
        game.rotateCurrentPiece();
    }

    /**
     * Handle the next piece board being clicked
     */
    protected void swapCurrentPiece(GameBlock ignored) {
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
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info(Colour.cyan("Initialising Challenge"));

        // Set up event listeners
        game.setOnLineCleared(s -> board.lineCleared(s, mainPane));
        game.setOnNextPiece((nextPiece, followingPiece) -> {
            this.currentPieceBoard.setPiece(nextPiece);
            this.nextPieceBoard.setPiece(followingPiece);
        });
        game.setOnGameOver(this::gameOver);
        game.setOnGameLoop(timer::reset);

        mainPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                game.rotateCurrentPiece();
                logger.info(Colour.orange("Intercepted right click"));
                e.consume();
            }
        });

        scene.setOnKeyPressed(this::onKeyPress);

        // To update the lives HUD element
        game.lives.addListener(this::onLifeChange);

        // Handle block on GameBoard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnBlockHoverEnter(this::blockHoverEnter);
        board.setOnBlockHoverExit(this::blockHoverExit);
        board.setOnRightClick(this::rotateCurrentPiece);

        // Handle PieceBoard being clicked
        currentPieceBoard.setOnRightClick(this::rotateCurrentPiece);
        currentPieceBoard.setOnBlockClick(this::rotateCurrentPiece);
        nextPieceBoard.setOnBlockClick(this::swapCurrentPiece);
        nextPieceBoard.setOnRightClick(this::swapCurrentPiece);

        Multimedia.startMusicIntro("game_start.wav", "game.wav");
        Multimedia.musicVolume.set(0);

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
        logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case LEFT, A -> onArrowKeyPressed(Vector2.left());
            case RIGHT, D -> onArrowKeyPressed(Vector2.right());
            case DOWN, S -> onArrowKeyPressed(Vector2.down());
            case UP, W -> onArrowKeyPressed(Vector2.up());
            case E, C, CLOSE_BRACKET -> game.rotateCurrentPiece();
            case Q, Z, OPEN_BRACKET -> game.rotateCurrentPieceCounterClockwise();
            case R, SPACE -> game.swapPieces();
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
        logger.info(Colour.orange("Debug key pressed: " + keyCode));
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
    private void onLifeChange(ObservableValue<? extends Number> observableValue, Number oldValue,
            Number newValue) {
        logger.info(Colour.purple("Lives changed from " + oldValue + " to " + newValue));
        int max = game.MAX_LIVES;
        for (int i = 0; i < max; i++) {
            if (i < newValue.intValue()) {
                ((ImageView) livesContainer.getChildren().get(max - i - 1)).setImage(
                        Multimedia.getImage("heart_small.png", 80));
            } else {
                ((ImageView) livesContainer.getChildren().get(max - i - 1)).setImage(
                        Multimedia.getImage("heart_small_empty.png", 80));
            }
        }
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

        logger.info(Colour.cyan("Arrow key pressed: " + direction));

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
        Multimedia.stop(Category.MUSIC);
        //Multimedia.playSound("game_over.wav");
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
        anim.getKeyFrames().add(new KeyFrame(
                Duration.ZERO,
                new KeyValue(displayedScore, displayedScore.get())
        ));
        anim.getKeyFrames().add(new KeyFrame(
                Duration.millis(300),
                new KeyValue(displayedScore, newValue)
        ));
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
         * @param width the starting width of the timer
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

        public void stop() {
            scaleTransition.stop();
        }
    }
}