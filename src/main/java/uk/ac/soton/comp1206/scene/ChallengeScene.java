package uk.ac.soton.comp1206.scene;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Multimedia;
import uk.ac.soton.comp1206.utils.Colour;
import uk.ac.soton.comp1206.utils.Colour.TextColour;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;
    protected VBox livesContainer;


    /**
     * Create a new Single Player challenge scene
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

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        var board = new GameBoard(game.getGrid(),
                gameWindow.getWidth()/2.,
                gameWindow.getWidth()/2.);
        mainPane.setCenter(board);

        VBox pieceBoardContainer = new VBox();

        double currentPieceRatio = .25;
        var currentPiece = new PieceBoard(gameWindow.getWidth()*currentPieceRatio,
                gameWindow.getWidth()*currentPieceRatio);
        game.currentPieceBoard = currentPiece;
        pieceBoardContainer.getChildren().add(currentPiece);

        double nextPieceRatio = .15;
        var nextPiece = new PieceBoard(gameWindow.getWidth()*nextPieceRatio,
                gameWindow.getWidth()*nextPieceRatio);
        game.nextPieceBoard = nextPiece;
        pieceBoardContainer.getChildren().add(nextPiece);

        pieceBoardContainer.alignmentProperty().set(Pos.CENTER);
        VBox.setMargin(nextPiece, new Insets(20, 0, 0, 0));

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
                livesContainer.getChildren().add(new ImageView(Multimedia.getImage("heart_small.png", 80)));
            }

            livesContainer.alignmentProperty().set(Pos.CENTER);
            livesContainer.getChildren().forEach(node -> {
                node.setTranslateY(-40);
                node.setTranslateX(20);
            });
        }
        mainPane.setLeft(livesContainer);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnBlockHoverEnter(this::blockHoverEnter);
        board.setOnBlockHoverExit(this::blockHoverExit);
    }

    /**
     * Handle when a block is clicked
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

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, (e) -> {
            if (e.isSecondaryButtonDown()) {
                game.rotateCurrentPiece();
                e.consume();
            }
        });

        scene.setOnKeyPressed(this::onKeyPress);
        game.lives.addListener(this::onLifeChange);

        Multimedia.startMusicIntro("game_start.wav", "game.wav");
        Multimedia.musicVolume.set(0);


        game.start();
    }


    public void onKeyPress(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case E -> game.rotateCurrentPiece();
            case Q -> game.rotateCurrentPieceReverse();
            case R,SPACE -> game.swapPieces();
            case X -> game.resetBoard();
            case S -> game.score(1, 5);
            case L -> game.lives.set(game.lives.get() - 1);
            case ESCAPE -> gameWindow.startMenu();
            case ENTER -> game.blockClicked();
        }
    }

    void onLifeChange(ObservableValue<? extends Number> observableValue,
            Number oldValue, Number newValue) {
        logger.info(Colour.purple(
                "Lives changed from " + oldValue + " to " + newValue));
        int max = game.MAX_LIVES;
        for (int i = 0; i < max; i++) {
            if (i < newValue.intValue()) {
                ((ImageView)livesContainer.getChildren().get(max-i-1))
                        .setImage(Multimedia.getImage("heart_small.png", 80));
            } else {
                ((ImageView)livesContainer.getChildren().get(max-i-1))
                        .setImage(Multimedia.getImage("heart_small_empty.png", 80));
            }
        }

        if (newValue.intValue() <= 0) {
            game.onDied();
            gameWindow.startMenu();
        }
    }
}