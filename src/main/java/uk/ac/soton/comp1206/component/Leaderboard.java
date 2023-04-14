package uk.ac.soton.comp1206.component;

import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList.Score.ScoreType;

public class Leaderboard extends ScoresList {

    private static final Logger logger = LogManager.getLogger(Leaderboard.class);


    public Leaderboard() {
        super(List.of());

        setMaxWidth(300);
        setScaleX(0.75);
        setScaleY(0.75);

        reveal();
    }

    public void updateScore(String message) {
        var split = message.split(":", 2);
        var username = split[0];
        var score = Integer.parseInt(split[1]);

        Platform.runLater(() -> {

            scores.removeIf(s -> s.username.equals(username));
            scores.add(new Score(username, score));
            rebuild();
        });
    }

    public void setScores(String scores) {
        var split = scores.split("\n");
        Platform.runLater(() -> {
            this.scores.clear();
            for (var score : split) {
                var splitScore = score.split(":", 3);
                this.scores.add(new Score(splitScore[0], Integer.parseInt(splitScore[1]),
                        splitScore[2].equals("DEAD") ? ScoreType.DIED : ScoreType.NORMAL));
            }
            rebuild();
        });
    }

    public List<String> getScoresAsStrings() {
        var strings = new LinkedList<String>();

        for (var score : scores) {
            strings.add(score.username + ":" + score.score);
        }

        return strings;
    }
}