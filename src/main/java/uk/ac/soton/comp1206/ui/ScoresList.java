package uk.ac.soton.comp1206.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.ScoresList.Score.ScoreType;

public class ScoresList extends GridPane {

    private static Logger logger = LogManager.getLogger(ScoresList.class);

    /**
     * Represents a single score
     */
    public static class Score {

        public Score(String username, int score, Score.ScoreType type) {
            this.username = username.replaceAll(":", "");
            this.score = score;
            this.type = type;
            if (type == Score.ScoreType.NEWSCORE) {
                this.usernameProperty = new SimpleStringProperty("");
            }
        }

        public Score(String username, int score) {
            this.username = username;
            this.score = score;
        }

        public Score(String text) {
            var split = text.split(" *: *");
            this.username = split[0];
            this.score = Integer.parseInt(split[1]);
        }

        public String username;
        public int score;
        public Node[] nodes = new Node[3];

        public enum ScoreType {
            NORMAL, MYSCORE, NEWSCORE
        }

        public ScoreType type = ScoreType.NORMAL;
        public StringProperty usernameProperty;


        public void animate(Duration delay) {
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

    protected boolean frozen = true;
    protected static final int MAX_LIST_LENGTH = 10;

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
    public Score max() {
        return scores.stream()
                .sorted(scoreComparator)
                .limit(MAX_LIST_LENGTH)
                .min(scoreComparator)
                .orElseThrow();
    }

    /**
     * Returns the lowest score in the list.
     */
    public Score min() {
        return scores.stream()
                .sorted(scoreComparator)
                .limit(MAX_LIST_LENGTH)
                .max(scoreComparator)
                .orElseThrow();
    }

}