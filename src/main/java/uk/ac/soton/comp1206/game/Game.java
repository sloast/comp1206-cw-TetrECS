package uk.ac.soton.comp1206.game;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
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

    /**
     * Whether to use the internal timer or not. If false, the game will use the timer from the UI
     */
    public static final boolean USE_INTERNAL_TIMER = false;

    // Using the internal timer keeps the logic more separated from the UI
    // but the speedup button doesn't work properly, so it is currently disabled
    /**
     * The number of lives the player starts with
     */
    public static final int MAX_LIVES = 3;
    private static final Logger logger = LogManager.getLogger(Game.class);
    /**
     * Number of rows
     */
    final int rows;
    /**
     * Number of columns
     */
    final int cols;
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

    // Properties that the UI can bind to
    /**
     * The number of lives the player has left
     */
    public IntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * The current level (player gains 1 level per 1000 points)
     */
    public IntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * The current multiplier
     */
    public IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * The current score
     */
    public IntegerProperty score = new SimpleIntegerProperty(0);
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
    public void initialiseGame() {
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
            resetTimer();
        }

    }

    /**
     * When the player loses all their lives
     */
    private void onDied() {
        running = false;
        logger.info(Colour.colour("Game over!", TextColour.PURPLE, TextMode.BOLD));

        stop();
    }

    /**
     * End the game and reset the grid
     */
    public void stop() {
        logger.info(Colour.colour("Game stopped.", TextColour.PURPLE, TextMode.BOLD));

        grid.reset();
        //currentPieceBoard.setPiece(null);
        //nextPieceBoard.setPiece(null);

        running = false;
        if (timer != null) {
            timer.cancel();
        }

        gameOverListener.onGameOver();
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
    void resetTimer() {
        gameLoopListener.onGameLoop(getTimerDelayMillis());

        // Reset internal timer
        if (USE_INTERNAL_TIMER) {

            if (timer != null) {
                timer.cancel();
            }

            timer = new Timer();
            var task = getTimerTask();

            timer.schedule(task, getTimerDelayMillis());
            startTime = System.currentTimeMillis();
        }

    }

    private TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                loseLife();
            }
        };
    }

    /**
     * Sets whether the timer is sped up or not
     *
     * @param fast if {@code true}, the timer will be sped up by 4x
     */
    public void setTimerSpeed(boolean fast) {

        if (fast != timerFast) {
            if (timer == null) {
                return;
            }
            double remainingTime = getTimerDelayMillis() - (System.currentTimeMillis() - startTime);
            remainingTime *= fast ? 0.25 : 4;
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
            grid.playPiece(currentPiece, x, y);
            Multimedia.playSound("place.wav");
            nextPiece();
            afterPiece();
            resetTimer();
            refreshPreview();
        } else {
            Multimedia.playSound("fail.wav");
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

        //logger.info("Rotating current piece");

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
        //logger.info("refresh");

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
            logger.info(Colour.colour(
                    "Cleared " + clearedRows + (clearedRows == 1 ? " row" : " rows"),
                    TextColour.GREEN, TextMode.ITALIC));

            lineClearedListener.onLineCleared(clearedBlocks);

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
            pieceBoardUpdateListener.onNextPiece(currentPiece, nextPiece);
        }
    }

    public void setOnPieceBoardUpdate(PieceBoardUpdateListener pieceBoardUpdateListener) {
        this.pieceBoardUpdateListener = pieceBoardUpdateListener;
    }

    public void setOnLineCleared(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    public void setOnGameOver(GameOverListener gameOverListener) {
        this.gameOverListener = gameOverListener;
    }

    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

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

            logger.info(Colour.green("Scored {} points"), points);
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

        if (!usingKeyboard) {
            usingKeyboard = true;
        }

        if (hoveredBlock != null) {
            hoveredBlock.hoverExit();
        }

        hoveredBlock = block;
        block.hoverEnter();

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