package uk.ac.soton.comp1206.game;

import java.util.ArrayDeque;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.utils.Colour;

public class MultiplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private final Communicator communicator;

    private Queue<GamePiece> nextPieces = new ArrayDeque<>();
    private final int TARGET_QUEUE_SIZE = 5;

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

    @Override
    public void loseLife() {
        super.loseLife();
        communicator.send("LIVES " + lives.get());
    }

    @Override
    public void stop() {
        communicator.send("DIE");
        super.stop();
    }

    @Override
    public synchronized void nextPiece() {
        for (int i = nextPieces.size(); i < TARGET_QUEUE_SIZE; i++) {
            communicator.send("PIECE");
        }

        while (nextPieces.isEmpty()) {
            try {
                communicator.send("PIECE");
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        currentPiece = nextPiece;
        nextPiece = nextPieces.remove();

        if (currentPiece == null) {
            nextPiece();
        }

        refreshPreview();
    }

    @Override
    public void afterPiece() {
        super.afterPiece();
        communicator.send(grid.toString());
    }

    @Override
    public void score(int linesCleared, int blocksCleared) {
        super.score(linesCleared, blocksCleared);
        communicator.send("SCORE " + this.score.get());
    }

}