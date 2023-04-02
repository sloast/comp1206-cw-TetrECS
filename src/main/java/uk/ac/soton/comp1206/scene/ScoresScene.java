package uk.ac.soton.comp1206.scene;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.ScoresScene.ScoresList.Score;
import uk.ac.soton.comp1206.scene.ScoresScene.ScoresList.Score.ScoreType;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Colour;

public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private static final Path scoresPath = Path.of("scores.txt");
    ;

    private boolean checkLastScore = false;
    private int newScore = 0;
    private ScoresList onlineScores;

    private Score newScoreObj;
    private ScoresList localScores;

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
     * Creates the scene, without providing a new score to check
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow) {
        super(gameWindow);
    }


    @Override
    public void build() {

        BorderPane mainPane = mainPane("challenge-background");

        // Titles
        var header = new VBox();

        // "Game Over" text
        var gameOverBox = new HBox();
        var gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("title");
        gameOverBox.getChildren().add(gameOverLabel);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setPadding(new Insets(10));
        header.getChildren().add(gameOverBox);

        // "High Scores" text
        var titleBox = new HBox();
        var title = new Label("HIGH SCORES");
        titleBox.getChildren().add(title);
        title.getStyleClass().add("title");
        //title.alignmentProperty().set(Pos.CENTER);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        header.getChildren().add(titleBox);

        mainPane.setTop(header);

        // Contains the two score lists
        var scoresContainer = new HBox();

        var localScoresContainer = new VBox();
        var localScoresTitle = new Label("LOCAL SCORES");
        localScoresTitle.getStyleClass().add("title");

        localScoresContainer.getStyleClass().add("generic-box");
        localScores = loadScores();
        localScoresContainer.getChildren().addAll(localScoresTitle, localScores);
        localScoresContainer.setAlignment(Pos.TOP_CENTER);
        //localScoresContainer.setPadding(new Insets(20,0,0,0));
        localScoresContainer.setSpacing(30);

        var onlineScoresContainer = new VBox();

        var onlineScoresTitle = new Label("ONLINE SCORES");
        onlineScoresTitle.getStyleClass().add("title");

        onlineScoresContainer.getStyleClass().add("generic-box");
        onlineScores = new ScoresList();
        onlineScoresContainer.getChildren().addAll(onlineScoresTitle, onlineScores);
        onlineScoresContainer.setAlignment(Pos.TOP_CENTER);
        onlineScoresContainer.setSpacing(30);

        scoresContainer.getChildren().addAll(localScoresContainer, onlineScoresContainer);
        scoresContainer.setPrefWidth(800);

        // make each list the same width
        localScores.setPrefWidth(scoresContainer.getPrefWidth() / 2);
        onlineScores.setPrefWidth(scoresContainer.getPrefWidth() / 2);

        mainPane.setCenter(scoresContainer);

        // Check if user beat a score on the leaderboard
        if (checkLastScore) {
            Score lowest = localScores.min();

            if (newScore > lowest.score) {
                newScoreObj = new Score("%ENTER_NAME%", newScore, ScoreType.NEWSCORE);
                localScores.scores.remove(lowest);
                localScores.scores.add(newScoreObj);
                title.setText("NEW HIGH SCORE!");
            }
        }
        // Show the scores
        localScores.reveal();
        loadOnlineScores();
        //onlineScores.reveal();

    }

    @Override
    public void initialise() {

        scene.setOnKeyPressed(this::onKeyPressed);
    }

    /**
     * Loads the local scores from the file in {@code this.scoresPath}
     */
    private ScoresList loadScores() {
        try {
            var scores = Files.lines(scoresPath).toList();
            if (scores.isEmpty()) {
                return new ScoresList();
            } else {
                return new ScoresList(scores);
            }
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

            return new ScoresList();
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
            onlineScores.scores.remove(onlineScores.min());
            onlineScores.scores.add(newScoreObj);
        }

        onlineScores.reveal();
    }

    /**
     * Saves the scores to the {@code scores.txt} file.
     */
    void saveScores(List<String> scores) {
        try {
            Files.write(scoresPath, scores);
        } catch (Exception e) {
            logger.error(Colour.red("Error writing scores file"));
        }
    }

    void rebuildAll() {
        localScores.rebuild();
        onlineScores.rebuild();
    }

    void onNameEntered() {
        rebuildAll();
        localScores.save();
        if (newScore > onlineScores.min().score) {
            gameWindow.getCommunicator()
                    .send("HISCORE " + newScoreObj.username + ":" + newScoreObj.score);
        }
    }

    /**
     * Represents the lists of scores displayed in this scene Extends GridPane, so that the lines
     * can be aligned properly
     */
    class ScoresList extends GridPane {

        /**
         * Represents a single score
         */
        static class Score {

            Score(String username, int score, ScoreType type) {
                this.username = username.replaceAll(":", "");
                this.score = score;
                this.type = type;
                if (type == ScoreType.NEWSCORE) {
                    this.usernameProperty = new SimpleStringProperty("");
                }
            }

            Score(String username, int score) {
                this.username = username;
                this.score = score;
            }

            Score(String text) {
                var split = text.split(" *: *");
                this.username = split[0];
                this.score = Integer.parseInt(split[1]);
            }

            String username;
            int score;
            Node[] nodes = new Node[3];

            public enum ScoreType {
                NORMAL, MYSCORE, NEWSCORE
            }

            ScoreType type = ScoreType.NORMAL;
            StringProperty usernameProperty;


            void animate(Duration delay) {
                for (var node : nodes) {
                    if (node == null) {
                        return;
                    }

                    var duration = Duration.millis(500);

                    node.setOpacity(0);
                    //node.setTranslateX(-300);

                    var translate = new TranslateTransition(duration, node);
                    translate.setFromY(30);
                    translate.setToY(0);

                    var fade = new FadeTransition(duration, node);
                    fade.setFromValue(0);
                    fade.setToValue(1);

                    translate.setDelay(delay);
                    fade.setDelay(delay);

                    translate.play();
                    fade.play();
                }
            }

            @Override
            public String toString() {
                return username + ":" + score;
            }
        }

        public ListProperty<Score> scores;

        public Comparator<Score> scoreComparator = (a, b) -> b.score - a.score;

        private boolean frozen = true;
        private static final int MAX_LIST_LENGTH = 10;

        /**
         * Loads scores from a list
         *
         * @param scores the scores to load, in text format
         */
        public void setAll(List<String> scores) {
            this.scores.setAll(scores.stream().map(Score::new).toList());
        }

        /**
         * Create a new {@code ScoresList} with placeholder scores
         */
        public ScoresList() {
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
         * Create a new {@code ScoresList} with the given scores
         *
         * @param scoresText the scores to load, each seperated by a newline.
         */
        public ScoresList(String scoresText) {
            this(Arrays.asList(scoresText.split("\n")));
        }

        /**
         * Create a new {@code ScoresList} with the given scores
         *
         * @param scoresTextList the scores to load, in text format. Does not need to be sorted
         */
        public ScoresList(List<String> scoresTextList) {
            super();
            List<Score> scoresList = scoresTextList.stream()
                    .map(Score::new)
                    .collect(Collectors.toList());

            scores = new SimpleListProperty<>(FXCollections.observableArrayList(scoresList));
            scores.addListener((ob, ov, nv) -> rebuild());

            ColumnConstraints usernameColumn = new ColumnConstraints();
            usernameColumn.setHalignment(HPos.RIGHT);
            //usernameColumn.setPercentWidth(50);
            ColumnConstraints separatorColumn = new ColumnConstraints();

            ColumnConstraints scoreColumn = new ColumnConstraints();
            scoreColumn.setHalignment(HPos.RIGHT);
            scoreColumn.setPercentWidth(30);

            getColumnConstraints().addAll(usernameColumn, separatorColumn, scoreColumn);

            getStyleClass().add("scores-list");

            //rebuild();
        }

        /**
         * Starts the animation to reveal the scores
         */
        public void reveal() {

            frozen = false;
            rebuild();

            var sortedScores = scores.stream()
                    .sorted(scoreComparator)
                    .toList();

            for (int i = 0; i < sortedScores.size(); i++) {
                sortedScores.get(i).animate(Duration.millis(i * 100));
            }

        }

        /**
         * Rebuilds the scores list. Called whenever the scores list is modified.
         */
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
         * Returns the highest score in the list. Uses {@code min()} because the comparator sorts in
         * descending order.
         */
        Score max() {
            return scores.stream()
                    .sorted(scoreComparator)
                    .limit(MAX_LIST_LENGTH)
                    .min(scoreComparator)
                    .orElseThrow();
        }

        /**
         * Returns the lowest score in the list.
         */
        Score min() {
            return scores.stream()
                    .sorted(scoreComparator)
                    .limit(MAX_LIST_LENGTH)
                    .max(scoreComparator)
                    .orElseThrow();
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

    /**
     * Handle key presses
     */
    public void onKeyPressed(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case ESCAPE, SPACE -> gameWindow.startMenu();
        }
    }
}