package uk.ac.soton.comp1206.scene;

import java.util.HashMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.OpponentBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Multimedia;

public class MultiplayerScene extends ChallengeScene {

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    private final String myUsername;
    private static final int NUM_OPPONENT_BOARDS = 3;
    private final HashMap<String, String> opponentBoardsMap = new HashMap<>();
    private final OpponentBoard[] opponentBoards = new OpponentBoard[NUM_OPPONENT_BOARDS];
    private Leaderboard leaderboard;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     * @param myUsername the username of the player
     */
    public MultiplayerScene(GameWindow gameWindow, String myUsername) {
        super(gameWindow);
        this.myUsername = myUsername;
    }

    @Override
    public void setupGame() {
        logger.info("Creating new multiplayer game");

        //Start new game
        game = new MultiplayerGame(5, 5, gameWindow.getCommunicator());
        disableTimerActions = Game.USE_INTERNAL_TIMER;
    }

    @Override
    public void build() {
        // Yes, i copy pasted this from challengescene
        logger.info("Building " + this.getClass().getName());

        setupGame();

        mainPane = setupMain("challenge-background");

        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.,
                gameWindow.getWidth() / 2.);
        mainPane.setCenter(board);

        VBox sideBar = new VBox();

        HBox pieceBoardContainer = new HBox();

        var currentPieceBoardContainer = new VBox();
        var currentPieceLabel = new Label("Current");
        currentPieceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        currentPieceBoardContainer.getChildren().add(currentPieceLabel);

        double currentPieceRatio = .15;
        this.currentPieceBoard = new PieceBoard(gameWindow.getWidth() * currentPieceRatio,
                gameWindow.getWidth() * currentPieceRatio);
        //game.currentPieceBoard = currentPieceBoard;
        currentPieceBoardContainer.getChildren().add(currentPieceBoard);
        pieceBoardContainer.getChildren().add(currentPieceBoardContainer);

        var nextPieceBoardContainer = new VBox();
        var nextPieceLabel = new Label("Next");
        nextPieceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        nextPieceBoardContainer.getChildren().add(nextPieceLabel);

        double nextPieceRatio = .1;
        this.nextPieceBoard = new PieceBoard(gameWindow.getWidth() * nextPieceRatio,
                gameWindow.getWidth() * nextPieceRatio);
        //game.nextPieceBoard = nextPiece;
        nextPieceBoardContainer.getChildren().add(nextPieceBoard);
        pieceBoardContainer.getChildren().add(nextPieceBoardContainer);

        pieceBoardContainer.alignmentProperty().set(Pos.CENTER);
        pieceBoardContainer.setSpacing(10);

        sideBar.getChildren().add(pieceBoardContainer);

        leaderboard = new Leaderboard(0.75, 300, myUsername);

        gameWindow.getCommunicator().addListener(this::onCommunication);
        gameWindow.getCommunicator().send("SCORES");

        sideBar.getChildren().add(leaderboard);

        mainPane.setRight(sideBar);

        var infoBox = new BorderPane();
        {

            var scoresBox = new VBox();
            {
                var scoreBox = new HBox();
                var scoreLabel = new Label("score ");
                var score = new Label("0");
                score.textProperty().bind(displayedScore.asString());
                game.score.addListener(super::onScoreChanged);
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

        var timerContainer = new HBox();
        timer = new GameTimer(700, 20);
        timerContainer.getChildren().add(timer);
        timerContainer.setAlignment(Pos.CENTER);
        mainPane.setBottom(timerContainer);
        HBox.setMargin(timer, new Insets(20, 0, 20, 0));

        setupMultiplayerBoards();
    }

    /**
     * Handle communication from the server
     *
     * @param communication the message that was received
     */
    public void onCommunication(String communication) {
        var split = communication.split(" ", 2);

        var command = split[0];
        var message = split[1];

        switch (command) {
            case "SCORES" -> leaderboard.setScores(message);
            case "SCORE" -> leaderboard.updateScore(message);
            case "BOARD" -> {
                var messageSplit = message.split(":", 2);
                var username = messageSplit[0];
                var board = messageSplit[1];
                opponentBoardsMap.put(username, board);
                updateOpponentBoards();
            }
        }
    }

    @Override
    void startScores() {
        gameWindow.startScores(game.getScore(), leaderboard);
    }

    private void setupMultiplayerBoards() {

        VBox bottomBar = new VBox();
        HBox boardsContainer = new HBox();
        boardsContainer.setSpacing(10);
        bottomBar.getChildren().addAll(timer, boardsContainer);
        mainPane.setBottom(bottomBar);

        for (int i = 0; i < NUM_OPPONENT_BOARDS; i++) {
            var boardContainer = new VBox();
            var board = new OpponentBoard(new Grid(5, 5), gameWindow.getWidth() / 2.,
                    gameWindow.getWidth() / 2.);
            opponentBoards[i] = board;
            var boardLabel = new Label("[unbound]");
            boardLabel.textProperty().bind(board.usernameProperty());
            boardLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
            boardContainer.getChildren().addAll(boardLabel, board);
            boardsContainer.getChildren().add(boardContainer);
        }
    }

    private void updateOpponentBoards() {
        String[] topPlayers = leaderboard.getTopPlayers(NUM_OPPONENT_BOARDS);
        for (int i = 0; i < topPlayers.length; i++) {
            var username = topPlayers[i];
            var board = opponentBoardsMap.get(username);
            if (board != null) {
                opponentBoards[i].setContents(username, board);
            }
        }
    }
}