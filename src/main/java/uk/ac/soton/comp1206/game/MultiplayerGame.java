package uk.ac.soton.comp1206.game;

import java.util.ArrayDeque;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.utils.Colour;

/**
 * The MultiplayerGame class represents the multiplayer game. It adds communication with the server,
 * and the ability to view opponents' boards while playing.
 *
 * @see Game
 */
public class MultiplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private static final int TARGET_QUEUE_SIZE = 5;
    private final Communicator communicator;

    /**
     * The queue containing the next pieces received from the server
     */
    private final Queue<GamePiece> nextPieces = new ArrayDeque<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param communicator the communicator to use for communication with the server
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.clearListeners();
        communicator.addListener(this::onCommunication);
    }

    /**
     * Handle messages received from the server
     *
     * @param communication the message received
     */
    private synchronized void onCommunication(String communication) {
        var split = communication.split(" ", 2);

        var type = split[0];
        String message;
        if (split.length > 1) {
            message = split[1];
        } else {
            message = "";
        }

        switch (type) {
            case "PIECE" -> {
                var piece = GamePiece.createPiece(Integer.parseInt(message));
                nextPieces.add(piece);
                notifyAll();
            }
            case "ERROR" -> logger.error("Received error from server: " + Colour.error(message));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loseLife() {
        super.loseLife();
        if (lives.get() >= 0) {
            communicator.send("LIVES " + lives.get());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        communicator.send("DIE");
        super.stop();
    }

    /**
     * Get the next piece from the queue.<br> If the queue is shorter than the target size, request
     * more pieces from the server.
     */
    @Override
    public synchronized void nextPiece() {
        // Request pieces up to the target queue size
        for (int i = nextPieces.size(); i < TARGET_QUEUE_SIZE; i++) {
            communicator.send("PIECE");
        }

        // Wait for pieces to be available
        while (nextPieces.isEmpty()) {
            try {
                communicator.send("PIECE");
                wait(); // The thread will be notified when a PIECE message is received
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Get the next piece
        currentPiece = nextPiece;
        nextPiece = nextPieces.remove();

        if (currentPiece == null) {
            nextPiece();
        }

        refreshPreview();
    }

    /**
     * After a piece has been placed, checks for any cleared rows or columns and updates the score.
     * <br> Also sends the current grid state to the server.
     */
    @Override
    void afterPiece() {
        super.afterPiece();
        communicator.send(grid.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void score(int linesCleared, int blocksCleared) {
        super.score(linesCleared, blocksCleared);
        communicator.send("SCORE " + this.score.get());
    }

}