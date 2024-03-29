package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.component.ScoresList.Score;
import uk.ac.soton.comp1206.component.ScoresList.Score.ScoreType;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Colour;

/**
 * The scene for the high scores. The user can view the local and online high scores, and add theirs
 * if they get a new high score.
 */
public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private static final Path scoresPath = Path.of("scores.txt");

    private boolean checkLastScore = false;
    private int newScore = -1;
    private HiScoresList onlineScores;

    private Score newScoreObj;
    private HiScoresList localScores;

    private boolean fromMultiplayer = false;
    private ScoresList multiplayerScores;

    /**
     * Create a new {@link ScoresScene} when coming from a multiplayer game
     *
     * @param gameWindow  the game window
     * @param score       the score achieved in the last game
     * @param leaderboard the leaderboard of the multiplayer game
     */
    public ScoresScene(GameWindow gameWindow, int score, ScoresList leaderboard) {
        this(gameWindow, score);
        fromMultiplayer = true;
        multiplayerScores = leaderboard;
        checkLastScore = true;
        newScore = score;
    }

    /**
     * Creates the scene, and checks if the last score is a high score
     *
     * @param gameWindow the game window
     * @param score      the score achieved in the last game
     */
    public ScoresScene(GameWindow gameWindow, int score) {
        this(gameWindow);
        checkLastScore = true;
        newScore = score;
    }

    /**
     * Creates the scene, without providing a new score to check. Use when coming directly from the
     * menu
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow) {
        super(gameWindow);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {

        BorderPane mainPane = setupMain("challenge-background");

        // Titles
        var header = new VBox();

        // "Game Over" / "High Scores" text
        String headerText = newScore == -1 ? "HIGH SCORES" : "GAME OVER";
        var gameOverBox = new HBox();
        var gameOverLabel = new Label(headerText);
        gameOverLabel.getStyleClass().add("title");
        gameOverBox.getChildren().add(gameOverLabel);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setPadding(new Insets(10));
        header.getChildren().add(gameOverBox);

        // "High Scores" text
        var titleBox = new HBox();
        var title = new Label("YOUR SCORE: " + newScore);
        titleBox.getChildren().add(title);
        title.getStyleClass().add("title");
        //title.alignmentProperty().set(Pos.CENTER);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        header.getChildren().add(titleBox);
        if (newScore == -1) {
            titleBox.setVisible(false);
        }

        mainPane.setTop(header);

        // Contains the two score lists
        var scoresContainer = new HBox();

        localScores = loadScores();

        if (!fromMultiplayer) {

            var localScoresContainer = new VBox();
            var localScoresTitle = new Label("LOCAL SCORES");
            localScoresTitle.getStyleClass().add("title");

            localScoresContainer.getStyleClass().add("generic-box");

            localScoresContainer.getChildren().addAll(localScoresTitle, localScores);
            localScoresContainer.setAlignment(Pos.TOP_CENTER);
            //localScoresContainer.setPadding(new Insets(20,0,0,0));
            localScoresContainer.setSpacing(30);

            scoresContainer.getChildren().add(localScoresContainer);

        } else {
            var thisGameScoresContainer = new VBox();
            var thisGameScoresLabel = new Label("THIS GAME");
            thisGameScoresLabel.getStyleClass().add("title");

            thisGameScoresContainer.getStyleClass().add("generic-box");
            multiplayerScores.setMaxWidth(Double.MAX_VALUE);
            thisGameScoresContainer.getChildren().addAll(thisGameScoresLabel, multiplayerScores);
            thisGameScoresContainer.setAlignment(Pos.TOP_CENTER);
            //localScoresContainer.setPadding(new Insets(20,0,0,0));
            thisGameScoresContainer.setSpacing(30);

            scoresContainer.getChildren().add(thisGameScoresContainer);
        }

        var onlineScoresContainer = new VBox();

        var onlineScoresTitle = new Label("ONLINE SCORES");
        onlineScoresTitle.getStyleClass().add("title");

        onlineScoresContainer.getStyleClass().add("generic-box");
        onlineScores = new HiScoresList();
        onlineScoresContainer.getChildren().addAll(onlineScoresTitle, onlineScores);
        onlineScoresContainer.setAlignment(Pos.TOP_CENTER);
        onlineScoresContainer.setSpacing(30);

        scoresContainer.getChildren().add(onlineScoresContainer);
        scoresContainer.setPrefWidth(800);

        // make each list the same width
        localScores.setPrefWidth(scoresContainer.getPrefWidth() / 2);
        onlineScores.setPrefWidth(scoresContainer.getPrefWidth() / 2);

        if (fromMultiplayer) {
            multiplayerScores.setPrefWidth(scoresContainer.getPrefWidth() / 2);
            multiplayerScores.setScaleX(1);
            multiplayerScores.setScaleY(1);
        }

        mainPane.setCenter(scoresContainer);

        // Check if user beat a score on the leaderboard
        newScoreObj = new Score("%ENTER_NAME%", newScore, ScoreType.NEWSCORE);
        if (checkLastScore && !fromMultiplayer) {
            Score lowest = localScores.min();

            if (newScore > lowest.score) {
                //localScores.scores.remove(lowest);
                localScores.scores.add(newScoreObj);
                title.setText("NEW HIGH SCORE!");
            }
        }
        // Show the scores
        localScores.reveal();
        loadOnlineScores();
        //onlineScores.reveal();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialise() {

        scene.setOnKeyPressed(this::onKeyPressed);
    }

    /**
     * Loads the local scores from the file in {@code this.scoresPath}
     */
    private HiScoresList loadScores() {
        try {

            if (!Files.exists(scoresPath)) {
                createDefaultScoresFile();
            }

            List<String> scores = Files.readAllLines(scoresPath);

            return new HiScoresList(scores);
        } catch (Exception e) {
            logger.error(Colour.red("Error reading scores file"));
            //check if file exists, create it if it doesn't
            if (!Files.exists(scoresPath)) {
                try {
                    Files.createFile(scoresPath);
                } catch (Exception ex) {
                    logger.error(Colour.red("Error creating scores file"));
                }
            }

            return new HiScoresList();
        }
    }

    /**
     * Creates a scores.txt file with some default scores
     */
    private void createDefaultScoresFile() {
        try {

            var in = getClass().getResourceAsStream("/misc/default-scores.txt");
            List<String> defaultScores = new BufferedReader(
                    new InputStreamReader(in)).lines().toList();

            Files.write(Path.of("scores.txt"), defaultScores);

            logger.info(Colour.cyan("Template scores file created"));

        } catch (Exception ex) {
            logger.error(Colour.error("Error creating template scores file: " + ex));
        }
    }

    /**
     * Loads scores from the online server
     */
    private void loadOnlineScores() {
        Communicator communicator = gameWindow.getCommunicator();

        communicator.clearListeners();
        communicator.addListener((message) -> {
            if (message.startsWith("HISCORES")) {
                var lines = Arrays.asList(message.substring(9).split("\n"));
                Platform.runLater(() -> displayOnlineScores(lines));
            }
        });
        communicator.send("HISCORES");
    }

    /**
     * Displays the online scores.
     *
     * @param lines the scores to display, in text format
     */
    private void displayOnlineScores(List<String> lines) {
        logger.info(Colour.green("Online scores received"));

        onlineScores.setAll(lines);

        if (newScoreObj != null && newScore > onlineScores.min().score) {
            //onlineScores.scores.remove(onlineScores.min());
            onlineScores.scores.add(newScoreObj);
        }

        onlineScores.reveal();
    }

    /**
     * Saves the scores to the {@code scores.txt} file.
     */
    private void saveScores(List<String> scores) {
        try {
            Files.write(scoresPath, scores);
        } catch (Exception e) {
            logger.error(Colour.red("Error writing scores file"));
        }
    }

    /**
     * Rebuilds the {@link ScoresList} objects
     */
    private void rebuildAll() {
        localScores.rebuild();
        onlineScores.rebuild();
    }

    /**
     * Saves the updated scores to the file, and sends the new score to the server if it is high
     * enough
     */
    private void onNameEntered() {
        rebuildAll();

        // Make sure the new score is in the list
        if (fromMultiplayer) {
            localScores.scores.add(newScoreObj);
        }

        // Save scores to the file
        localScores.save();

        // Send the new score to the server
        if (newScore > onlineScores.min().score) {
            gameWindow.getCommunicator()
                    .send("HISCORE " + newScoreObj.username + ":" + newScoreObj.score);
        }
    }

    /**
     * Handle key presses
     *
     * @param keyEvent the key event
     */
    public void onKeyPressed(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case ESCAPE, SPACE -> gameWindow.startMenu();
        }
    }

    /**
     * Represents the lists of scores displayed in this scene Extends GridPane, so that the lines
     * can be aligned properly
     */
    private class HiScoresList extends ScoresList {

        /**
         * Creates a new HiScoresList with default scores
         */
        public HiScoresList() {
            this(List.of(
                    "Player 1: 100",
                    "Player 2: 200",
                    "Player 3: 300",
                    "Player 4: 400",
                    "Player 5: 500",
                    "Player 6: 600",
                    "Player 7: 700",
                    "Player 8: 800",
                    "Player 9: 900",
                    "Player 10: 1000"
            ));
        }

        /**
         * Creates a new HiScoresList with the given scores
         *
         * @param scores the scores to display
         */
        public HiScoresList(List<String> scores) {
            super(scores);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void rebuild() {
            if (frozen) {
                return;
            }

            logger.info("Rebuilding scores list");

            var sortedScores = scores.stream()
                    .sorted(scoreComparator)
                    .limit(MAX_LIST_LENGTH)
                    .toList();

            getChildren().clear();

            sortedScores.forEach((score) -> {
                Node username = new Label(score.username);
                username.getStyleClass().add("scores-item");
                var separator = new Label(":");
                separator.getStyleClass().add("scores-item");
                var scoreLabel = new Label(Integer.toString(score.score));
                scoreLabel.getStyleClass().add("scores-item");

                if (score.type == ScoreType.NEWSCORE) {
                    final var usernameField = new TextField();
                    usernameField.setPromptText("ENTER NAME:");
                    usernameField.setAlignment(Pos.CENTER_RIGHT);
                    username = usernameField;
                    usernameField.setOnAction((event) -> {
                        score.username = usernameField.getText();
                        score.type = ScoreType.MYSCORE;
                        onNameEntered();
                    });

                    usernameField.textProperty().bindBidirectional(score.usernameProperty);

                    username.getStyleClass().add("score-entry-box");
                    username.getStyleClass().add("my-score");
                    separator.getStyleClass().add("my-score");
                    scoreLabel.getStyleClass().add("my-score");
                }

                if (score.type == ScoreType.MYSCORE) {
                    username.getStyleClass().add("my-score");
                    separator.getStyleClass().add("my-score");
                    scoreLabel.getStyleClass().add("my-score");
                }

                score.nodes = new Node[]{username, separator, scoreLabel};

                var row = this.getRowCount();

                add(username, 0, row);
                add(separator, 1, row);
                add(scoreLabel, 2, row);
            });

            layout();
        }


        /**
         * Saves the current list of scores to {@code scores.txt}.
         */
        void save() {
            saveScores(scores.stream()
                    .sorted(scoreComparator)
                    .map(Score::toString)
                    .toList());
        }

    }
}