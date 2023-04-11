package uk.ac.soton.comp1206.game;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.utils.Colour;

public class MultiplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private final Communicator communicator;

    private Queue<GamePiece> nextPieces = new ArrayDeque<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.clearListeners();
        communicator.addListener(this::onCommunication);
    }

    private void onCommunication(String communication) {
        var split = communication.split(" ", 2);

        var type = split[0];
        String message;
        if (split.length > 1) {
            message = split[1];
        } else {
            message = "";
        }

        switch (type) {
            case "PIECE":
                var piece = GamePiece.createPiece(Integer.parseInt(message));
                nextPieces.add(piece);
                break;

            case "START":

            case "ERROR":
                logger.error("Received error from server: " + Colour.error(message));
                break;

            default:
                logger.warn("Received unknown message from server: " + Colour.warn(message));
                break;
        }
    }

    @Override
    public void loseLife() {
        lives.set(lives.get() - 1);
        if (lives.get() <= 0) {
            onDied();
        } else {
            nextPiece();
            resetTimer();
            communicator.send("LIVES " + lives.get());
        }

    }
}