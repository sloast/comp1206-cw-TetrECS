package uk.ac.soton.comp1206.scene;

import java.util.concurrent.ScheduledExecutorService;
import javafx.animation.ScaleTransition;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
import uk.ac.soton.comp1206.ui.GamePane;
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
    protected Game game;
    protected GameBoard board;
    protected VBox livesContainer;

    public PieceBoard currentPieceBoard;
    public PieceBoard nextPieceBoard;

    public ScheduledExecutorService gameTimer;

    public BorderPane mainPane;


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

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),
                gameWindow.getWidth() / 2.,
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

        var infoBox = new BorderPane();
        {

            var scoresBox = new VBox();
            {
                var scoreBox = new HBox();
                var scoreLabel = new Label("score ");
                var score = new Label("0");
                score.textProperty().bind(game.displayedScore.asString());
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
                node.setTranslateX(20);
            });
        }
        mainPane.setLeft(livesContainer);

        // Set up event listeners
        game.setOnLineCleared(s -> board.lineCleared(s, mainPane));
        game.setOnNextPiece((nextPiece, followingPiece) -> {
            this.currentPieceBoard.setPiece(nextPiece);
            this.nextPieceBoard.setPiece(followingPiece);
        });
        game.setOnGameOver(this::gameOver);
        game.setOnGameLoop(this::resetTimer);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnBlockHoverEnter(this::blockHoverEnter);
        board.setOnBlockHoverExit(this::blockHoverExit);
        board.setOnRightClick(this::rotateCurrentPiece);

        currentPieceBoard.setOnRightClick(this::rotateCurrentPiece);
        currentPieceBoard.setOnBlockClick(this::rotateCurrentPiece);
        nextPieceBoard.setOnBlockClick(this::swapCurrentPiece);
        nextPieceBoard.setOnRightClick(this::swapCurrentPiece);

        mainPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                game.rotateCurrentPiece();
                logger.info(Colour.orange("Intercepted right click"));
                e.consume();
            }
        });
    }

    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    protected void blockHoverEnter(GameBlock gameBlock) {
        game.onBlockHoverEnter(gameBlock);
    }

    protected void blockHoverExit(GameBlock gameBlock) {
        game.onBlockHoverExit(gameBlock);
    }

    protected void rotateCurrentPiece(GameBlock ignored) {
        game.rotateCurrentPiece();
    }

    protected void swapCurrentPiece(GameBlock ignored) {
        game.swapPieces();
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info(Colour.cyan("Initialising Challenge"));

        scene.setOnKeyPressed(this::onKeyPress);
        game.lives.addListener(this::onLifeChange);

        Multimedia.startMusicIntro("game_start.wav", "game.wav");
        Multimedia.musicVolume.set(0);

        if (App.DEBUG_MODE) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, this::testingKeyBinds);
        }

        game.start();
    }


    public void onKeyPress(KeyEvent event) {
        var keyCode = event.getCode();
        logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case LEFT, A -> onArrowKeyPressed(Vector2.left());
            case RIGHT, D -> onArrowKeyPressed(Vector2.right());
            case DOWN, S -> onArrowKeyPressed(Vector2.down());
            case UP, W -> onArrowKeyPressed(Vector2.up());
            case E, C, CLOSE_BRACKET -> game.rotateCurrentPiece();
            case Q, Z, OPEN_BRACKET -> game.rotateCurrentPieceReverse();
            case R, SPACE -> game.swapPieces();
            case ENTER -> game.keyboardPlayPiece();
            case ESCAPE -> {
                game.stop();
                gameWindow.startMenu();
            }
        }
    }

    public void testingKeyBinds(KeyEvent event) {
        var keyCode = event.getCode();
        logger.info(Colour.red("Debug key pressed: " + keyCode));
        switch (keyCode) {
            case NUMBER_SIGN -> game.resetBoard();
            case N -> game.nextPiece();
            case EQUALS -> game.score(1, 5);
            case L -> game.loseLife();
        }
    }

    void onLifeChange(ObservableValue<? extends Number> observableValue,
            Number oldValue, Number newValue) {
        logger.info(Colour.purple(
                "Lives changed from " + oldValue + " to " + newValue));
        int max = game.MAX_LIVES;
        for (int i = 0; i < max; i++) {
            if (i < newValue.intValue()) {
                ((ImageView) livesContainer.getChildren().get(max - i - 1))
                        .setImage(Multimedia.getImage("heart_small.png", 80));
            } else {
                ((ImageView) livesContainer.getChildren().get(max - i - 1))
                        .setImage(Multimedia.getImage("heart_small_empty.png", 80));
            }
        }
    }

    public void onArrowKeyPressed(Vector2 direction) {
        if (!game.isRunning()) {
            return;
        }

        logger.info(Colour.cyan("Arrow key pressed: " + direction));

        var pos = new Vector2(game.getCols() / 2, game.getRows() / 2);
        var hoveredBlock = game.hoveredBlock;

        if (hoveredBlock != null) { // && game.usingKeyboard) {
            pos.x = hoveredBlock.getX();
            pos.y = hoveredBlock.getY();

            pos = pos.add(direction).clamp(
                    Vector2.zero(),
                    new Vector2(game.getCols() - 1, game.getRows() - 1)
            );
        }

        hoveredBlock = board.getBlock(pos.x, pos.y);

        game.hoverBlockKeyboard(hoveredBlock);
    }

    void resetTimer(double ms) {
        Rectangle timerBar = new Rectangle(0, 0, 0, 0);
        timerBar.setFill(Color.GREEN);

        ScaleTransition transition = new ScaleTransition(Duration.millis(ms), timerBar);

        transition.setFromX(1);
        transition.setToX(0);
        transition.setCycleCount(1);
        transition.setAutoReverse(false);

        transition.setOnFinished(e -> game.loseLife());

        transition.play();

        mainPane.setBottom(timerBar);
    }

    public void gameOver() {
        logger.info(Colour.red("Game Over"));
        Multimedia.stop(Category.MUSIC);
        //Multimedia.playSound("game_over.wav");
        gameWindow.startMenu();
    }
}