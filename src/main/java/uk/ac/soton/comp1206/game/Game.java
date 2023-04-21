package uk.ac.soton.comp1206.game;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.PieceBoardUpdateListener;
import uk.ac.soton.comp1206.utils.Colour;
import uk.ac.soton.comp1206.utils.Colour.TextColour;
import uk.ac.soton.comp1206.utils.Colour.TextMode;
import uk.ac.soton.comp1206.utils.Multimedia;
import uk.ac.soton.comp1206.utils.Vector2;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

    // Using the internal timer keeps the logic more separated from the UI, but the speedup button
    // is less reliable using this method.
    // When true, the timer is handled by the ChallengeScene instead
    /**
     * Whether to use the internal timer or not. If false, the game will use the timer from the UI.
     * It is recommended to leave this as false.
     */
    public static final boolean USE_INTERNAL_TIMER = false;

    /**
     * The number of lives the player starts with
     */
    public static final int MAX_LIVES = 3;
    private static final Logger logger = LogManager.getLogger(Game.class);
    /**
     * The number of lives the player has left
     */
    public final IntegerProperty lives = new SimpleIntegerProperty(3);
    /**
     * The current level (player gains 1 level per 1000 points)
     */
    public final IntegerProperty level = new SimpleIntegerProperty(0);
    /**
     * The current multiplier
     */
    public final IntegerProperty multiplier = new SimpleIntegerProperty(1);
    /**
     * The current score
     */
    public final IntegerProperty score = new SimpleIntegerProperty(0);
    /**
     * Number of rows
     */
    final int rows;
    /**
     * Number of columns
     */
    final int cols;

    // Properties that the UI can bind to
    /**
     * The grid model linked to the game
     */
    final Grid grid;
    private final Random random = new Random();
    private final Queue<Integer> pieceQueue = new LinkedList<>();
    /**
     * The block that is currently selected
     */
    public GameBlock hoveredBlock = null;
    // Pieces
    GamePiece currentPiece = null;
    GamePiece nextPiece = null;
    // Whether the player is currently controlling the game with the keyboard
    private boolean usingKeyboard = false;
    // Listeners
    private PieceBoardUpdateListener pieceBoardUpdateListener;
    private LineClearedListener lineClearedListener;
    private GameOverListener gameOverListener;
    private GameLoopListener gameLoopListener;
    // Game actions are only allowed while the game is running
    private boolean running = false;
    // Timer
    private Timer timer;
    private double startTime;
    private boolean timerFast = false;
    private double currentTimerDelay;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);


    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();

        running = true;

        resetTimer();
        refreshPreview();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    private void initialiseGame() {
        logger.info("Initialising game");

        nextPiece();
    }

    /**
     * Reduce the player's life count by one and reset the timer, or end the game if they have run
     * out
     */
    public void loseLife() {
        lives.set(lives.get() - 1);
        if (lives.get() < 0) {
            onDied();
        } else {
            nextPiece();
            multiplier.set(1);
            resetTimer();
        }

    }

    /**
     * When the player loses all their lives
     */
    private void onDied() {
        logger.info(Colour.colour("Game over!", TextColour.PURPLE, TextMode.BOLD));

        stop();

        gameOverListener.onGameOver();
    }

    /**
     * End the game and reset the grid
     */
    public void stop() {
        running = false;

        logger.info(Colour.colour("Game stopped.", TextColour.PURPLE, TextMode.BOLD));

        grid.reset();

        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Returns the current score
     *
     * @return the current score
     */
    public int getScore() {
        return score.get();
    }

    /**
     * Resets the timer
     */
    private void resetTimer() {
        gameLoopListener.onGameLoop();

        // Reset internal timer
        if (USE_INTERNAL_TIMER) {

            if (timer != null) {
                timer.cancel();
            }

            timer = new Timer();
            var task = getTimerTask();

            timer.schedule(task, getTimerDelayMillis());

            startTime = System.currentTimeMillis();
            currentTimerDelay = getTimerDelayMillis();
        }

    }

    protected TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(Game.this::loseLife);
            }
        };
    }

    /**
     * Sets whether the timer is sped up or not
     *
     * @param fast if {@code true}, the timer will be sped up by 4x
     */
    public void setTimerSpeedUp(boolean fast) {

        if (fast != timerFast) {
            if (timer == null) {
                return;
            }
            double currentTime = System.currentTimeMillis();
            double elapsed = currentTime - startTime;
            double remainingTime = currentTimerDelay - elapsed;
            remainingTime *= fast ? 0.25 : 4;
            currentTimerDelay = remainingTime;
            startTime = currentTime;
            timer.cancel();
            timer = new Timer();
            timer.schedule(getTimerTask(), (long) remainingTime);
            timerFast = fast;
        }

    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {

        if (!running) {
            return;
        }

        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //Place the piece
        if (grid.canPlayPiece(currentPiece, x, y)) {
            logger.info(Colour.cyan(
                    "Placing piece " + currentPiece.getValue() + " at (" + x + ", " + y + ")"));
            grid.playPiece(currentPiece, x, y);
            Multimedia.playSound("place.wav");
            nextPiece();
            afterPiece();
            resetTimer();
            refreshPreview();
        } else {
            logger.info(Colour.orange(
                    "Cannot place piece " + currentPiece.getValue() + " at (" + x + ", " + y
                            + ")"));
            Multimedia.playSound("fail.wav", 1.5);
        }
    }

    /**
     * Play a piece on the currently selected block
     */
    public void keyboardPlayPiece() {
        if (hoveredBlock != null) { // && usingKeyboard) {
            this.blockClicked(hoveredBlock);
        }
    }

    /**
     * Handle the mouse hovering over a block
     *
     * @param gameBlock the block that was hovered over
     */
    public void onBlockHoverEnter(GameBlock gameBlock) {
        if (!running) {
            return;
        }
        if (usingKeyboard) {
            usingKeyboard = false;
        }

        hoveredBlock = gameBlock;
        gameBlock.hoverEnter();
        refreshPreview();
    }

    /**
     * Handle the mouse un-hovering a block
     *
     * @param gameBlock the block that the mouse exited
     */
    public void onBlockHoverExit(GameBlock gameBlock) {
        if (!running) {
            return;
        }

        hoveredBlock = null;
        gameBlock.hoverExit();
        refreshPreview();
    }

    /**
     * Rotates the current piece clockwise
     */
    public void rotateCurrentPiece() {
        if (!running) {
            return;
        }

        //.info("Rotating current piece");

        currentPiece.rotate();
        refreshPreview();
    }

    /**
     * Rotates the current piece counter-clockwise
     */
    public void rotateCurrentPieceCounterClockwise() {
        if (!running) {
            return;
        }

        logger.debug("Rotating current piece");

        for (int i = 0; i < 3; i++) {
            currentPiece.rotate();
        }
        refreshPreview();
    }

    /**
     * Updates the board and pieceBoards to show where the next block will be placed, and its
     * orientation
     */
    void refreshPreview() {
        if (!running) {
            return;
        }

        // removed for spamming the console
        //logger.debug("refresh");

        grid.resetAllTempValues();
        if (hoveredBlock != null) {
            previewPiece(hoveredBlock);
        }
        updatePieceBoards();
    }

    /**
     * Displays a piece semi-transparently on the board to show where it will be placed
     *
     * @param gameBlock the block to display
     */
    private void previewPiece(GameBlock gameBlock) {
        boolean valid = grid.canPlayPiece(currentPiece, gameBlock.getX(), gameBlock.getY());
        grid.previewPiece(currentPiece, gameBlock.getX(), gameBlock.getY(), valid);
    }

    /**
     * After a piece has been placed, checks for any cleared rows or columns and updates the score
     */
    void afterPiece() {
        int clearedRows = 0;
        var clearedBlocks = new HashSet<Vector2>();

        // Check for cleared rows
        for (int y = 0; y < rows; y++) {
            boolean full = true;
            for (int x = 0; x < cols; x++) {
                if (grid.get(x, y) == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                clearedRows++;
                for (int x = 0; x < cols; x++) {
                    clearedBlocks.add(new Vector2(x, y));
                }
            }
        }

        // Check for cleared columns
        for (int x = 0; x < cols; x++) {
            boolean full = true;
            for (int y = 0; y < rows; y++) {
                if (grid.get(x, y) == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                clearedRows++;
                for (int y = 0; y < rows; y++) {
                    clearedBlocks.add(new Vector2(x, y));
                }
            }
        }

        score(clearedRows, clearedBlocks.size());

        if (clearedRows > 0) {
            logger.info(
                    Colour.colour("Cleared " + clearedRows + (clearedRows == 1 ? " row" : " rows"),
                            TextColour.GREEN, TextMode.ITALIC));

            // Animate the blocks clearing
            lineClearedListener.onLineCleared(clearedBlocks);

            // Remove the blocks
            for (var block : clearedBlocks) {
                grid.set(block.x, block.y, 0);
            }

        }
    }


    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    public void nextPiece() {
        this.nextPiece(false);
    }

    /**
     * Get the next piece from the queue.
     *
     * @param reset If true, the queue will be cleared and re-generated
     */
    public void nextPiece(boolean reset) {
        //currentPiece = spawnPiece();

        if (reset) {
            pieceQueue.clear();
            nextPiece = null;
        }

        // Create a list of the pieces in a random order
        if (pieceQueue.isEmpty()) {
            var pieces = new LinkedList<Integer>();
            for (int i = 0; i < GamePiece.PIECES; i++) {
                pieces.add(i);
            }
            while (!pieces.isEmpty()) {
                pieceQueue.add(pieces.remove(random.nextInt(pieces.size())));
            }

            logger.info("New piece queue: {}", pieceQueue.toString());
        }

        // Take the next piece from the queue
        currentPiece = nextPiece;
        nextPiece = GamePiece.createPiece(pieceQueue.remove(), random.nextInt(4));

        logger.info(
                "Getting next piece: " + (currentPiece == null ? "null" : currentPiece.getValue()));

        if (currentPiece == null || nextPiece.getValue() == currentPiece.getValue()) {
            nextPiece();
            return;
        }

        //currentPieceBoard.setPiece(currentPiece);
        //nextPieceBoard.setPiece(nextPiece);

        refreshPreview();
    }

    /**
     * Update the pieceBoard UI components on the current held pieces
     */
    private void updatePieceBoards() {
        if (pieceBoardUpdateListener != null) {
            pieceBoardUpdateListener.updatePieceBoards(currentPiece, nextPiece);
        }
    }

    /**
     * Set the listener for when the piece boards are updated
     *
     * @param pieceBoardUpdateListener the listener to call
     */
    public void setOnPieceBoardUpdate(PieceBoardUpdateListener pieceBoardUpdateListener) {
        this.pieceBoardUpdateListener = pieceBoardUpdateListener;
    }

    /**
     * Set the listener for when a line is cleared
     *
     * @param lineClearedListener the listener to call
     */
    public void setOnLineCleared(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Set the listener for when the game ends
     *
     * @param gameOverListener the listener to call
     */
    public void setOnGameOver(GameOverListener gameOverListener) {
        this.gameOverListener = gameOverListener;
    }

    /**
     * Set the listener for when the game loop is updated (i.e. the timer is reset)
     *
     * @param gameLoopListener the listener to call
     */
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * Swap the current piece with the next piece
     */
    public void swapPieces() {
        if (!running) {
            return;
        }

        var temp = currentPiece;
        currentPiece = nextPiece;
        nextPiece = temp;

        refreshPreview();
    }

    /**
     * Reset the board (for testing)
     */
    public void resetBoard() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid.set(x, y, 0);
            }
        }
        nextPiece(true);
        refreshPreview();
    }

    /**
     * Update the score after the player plays a piece
     *
     * @param linesCleared  the number of lines cleared by this action
     * @param blocksCleared the number of blocks cleared by this action
     */
    public void score(int linesCleared, int blocksCleared) {

        if (linesCleared > 0) {
            int points = linesCleared * blocksCleared * 10 * multiplier.get();

            score.set(points + score.get());
            level.set(score.get() / 1000);
            multiplier.set(multiplier.get() + 1);

            logger.info(Colour.green(Colour.bold("Scored {} points")), points);
        } else {
            multiplier.set(1);
        }

    }

    /**
     * Selects a block using the keyboard
     *
     * @param block the block that was selected
     */
    public void hoverBlockKeyboard(GameBlock block) {
        if (block == null) {
            return;
        }

        // enable keyboard mode
        if (!usingKeyboard) {
            usingKeyboard = true;
        }

        // deselect the previous block
        if (hoveredBlock != null) {
            hoveredBlock.hoverExit();
        }

        // select the new block
        hoveredBlock = block;
        block.hoverEnter();

        // update visuals
        refreshPreview();
    }

    /**
     * Check if the game has started
     *
     * @return true if the game is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get the timer duration for the current level
     *
     * @return timer duration in milliseconds
     */
    public long getTimerDelayMillis() {
        return Math.max(2500, 12000 - 500 * level.get());
    }

    /**
     * Get the timer duration for the current level
     *
     * @return timer duration, as a {@link Duration}
     */
    public Duration getTimerDelay() {
        return Duration.millis(getTimerDelayMillis());
    }
}