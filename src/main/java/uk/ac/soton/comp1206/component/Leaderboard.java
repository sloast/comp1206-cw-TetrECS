package uk.ac.soton.comp1206.component;

import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList.Score.ScoreType;

/**
 * Represents the leaderboard displayed in a multiplayer game
 *
 * @author Adam Robson
 */
public class Leaderboard extends ScoresList {

    private static final Logger logger = LogManager.getLogger(Leaderboard.class);

    private final String myUsername;

    /**
     * Create an empty leaderboard
     */
    public Leaderboard(double scale, double maxWidth, String myUsername) {
        super(List.of());

        this.myUsername = myUsername;

        setMaxWidth(maxWidth);
        setScaleX(scale);
        setScaleY(scale);

        reveal();
    }

    /**
     * Update the score of a player
     *
     * @param message The message received, in the format {@code "username:score"}
     */
    public void updateScore(String message) {
        var split = message.split(":", 2);
        var username = split[0];
        var score = Integer.parseInt(split[1]);

        Platform.runLater(() -> {

            for (Score s : scores) {
                if (s.username.equals(username)) {
                    s.score = score;
                    break;
                }
            }
            rebuild();
        });
    }

    /**
     * Set all the scores
     *
     * @param scores a newline delimited string of scores in the format
     *               {@code "username:score:lives"}
     */
    public void setScores(String scores) {
        var split = scores.split("\n");
        Platform.runLater(() -> {
            this.scores.clear();
            for (var score : split) {
                var splitScore = score.split(":", 3);

                if (splitScore[2].equals("DEAD")) {
                    this.scores.add(
                            new ScoreWithLives(
                                    splitScore[0],
                                    Integer.parseInt(splitScore[1]),
                                    ScoreType.DIED,
                                    -1));
                } else {
                    this.scores.add(
                            new ScoreWithLives(
                                    splitScore[0],
                                    Integer.parseInt(splitScore[1]),
                                    splitScore[0].equals(myUsername) ?
                                            ScoreType.MYSCORE : ScoreType.NORMAL,
                                    Integer.parseInt(splitScore[2])));
                }
            }
            rebuild();
        });
    }

    public void setDead(String username) {
        Platform.runLater(() -> {
            for (Score s : scores) {
                if (s.username.equals(username)) {
                    s.type = ScoreType.DIED;
                    break;
                }
            }
            rebuild();
        });
    }

    /**
     * Returns the scores as a list of strings
     *
     * @return a list containing strings of the format {@code "username:score"}
     * @deprecated
     */
    public List<String> getScoresAsStrings() {
        var strings = new LinkedList<String>();

        for (var score : scores) {
            strings.add(score.username + ":" + score.score);
        }

        return strings;
    }

    /**
     * Represents a single score, with information about the number of lives remaining
     */
    public static class ScoreWithLives extends Score {

        public int lives = 3;

        /**
         * Create a new score
         *
         * @param username the username
         * @param score    the score
         * @param type     the type of score
         * @param lives    the number of lives remaining
         */
        public ScoreWithLives(String username, int score, ScoreType type, int lives) {
            super(username, score, type);
            this.lives = lives;
        }

        /**
         * Get the style class for the score, coloured depending on the number of lives remaining
         *
         * @return the style class
         */
        @Override
        public String getStyleClass() {
            if (type == ScoreType.MYSCORE) {
                return "my-score";
            } else if (type == ScoreType.DIED) {
                return "dead-score";
            }
            return switch (lives) {
                case 3 -> "score-lives-3";
                case 2 -> "score-lives-2";
                case 1 -> "score-lives-1";
                case 0 -> "score-lives-0";
                default -> "dead-score";
            };
        }
    }

    public String[] getTopPlayers(int limit) {
        return scores.stream()
                .sorted(ScoresList.scoreComparator)
                .map(s -> s.username)
                .filter(u -> !u.equals(myUsername))
                .limit(limit)
                .toArray(String[]::new);
    }
}