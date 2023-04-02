package uk.ac.soton.comp1206.game;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.utils.Colour;
import uk.ac.soton.comp1206.utils.Colour.TextColour;
import uk.ac.soton.comp1206.utils.Colour.TextMode;
import uk.ac.soton.comp1206.utils.Vector2;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);
    public int MAX_LIVES = 3;
    public static boolean USE_EXECUTOR_SERVICE = false;

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    private boolean running = false;

    public boolean usingKeyboard = false;

    private GamePiece currentPiece = null;
    private GamePiece nextPiece = null;

    private final Random random = new Random();

    public GameBlock hoveredBlock = null;
    public GameBlock lastHoveredBlock = null;

    private Queue<Integer> pieceQueue = new LinkedList<>();

    public IntegerProperty displayedScore = new SimpleIntegerProperty(1);
    protected IntegerProperty actualScore = new SimpleIntegerProperty(0);
    public IntegerProperty lives = new SimpleIntegerProperty(3);
    public IntegerProperty level = new SimpleIntegerProperty(0);
    public IntegerProperty multiplier = new SimpleIntegerProperty(1);

    protected NextPieceListener nextPieceListener;
    protected LineClearedListener lineClearedListener;
    protected GameOverListener gameOverListener;
    protected GameLoopListener gameLoopListener;

    Timer timer;
    double startTime;
    boolean timerFast = false;

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


    public void loseLife() {
        lives.set(lives.get() - 1);
        if (lives.get() <= 0) {
            onDied();
        } else {
            nextPiece();
            resetTimer();
        }

    }

    /**
     * When the player loses all their lives
     */
    public void onDied() {
        running = false;
        logger.info(Colour.colour("Game over!", TextColour.PURPLE, TextMode.BOLD));

        stop();
    }

    public void stop() {
        logger.info(Colour.colour("Game stopped.", TextColour.PURPLE, TextMode.BOLD));

        grid.reset();
        //currentPieceBoard.setPiece(null);
        //nextPieceBoard.setPiece(null);

        running = false;
        if (timer != null) timer.cancel();

        gameOverListener.onGameOver();
    }

    public int getScore() {
        return actualScore.get();
    }

    void resetTimer() {
        gameLoopListener.onGameLoop(getTimerDelayMillis());

        if (USE_EXECUTOR_SERVICE) {

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
            nextPiece();
            afterPiece();
            resetTimer();
            refreshPreview();
        }
    }

    public void keyboardPlayPiece() {
        if (hoveredBlock != null) { // && usingKeyboard) {
            this.blockClicked(hoveredBlock);
        }
    }

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

    public void onBlockHoverExit(GameBlock gameBlock) {
        if (!running) {
            return;
        }

        lastHoveredBlock = hoveredBlock;
        hoveredBlock = null;
        gameBlock.hoverExit();
        refreshPreview();
    }

    public void rotateCurrentPiece() {
        if (!running) {
            return;
        }

        logger.info("Rotating current piece");

        currentPiece.rotate();
        refreshPreview();
    }

    public void rotateCurrentPieceReverse() {
        if (!running) {
            return;
        }

        logger.info("Rotating current piece");

        for (int i = 0; i < 3; i++) {
            currentPiece.rotate();
        }
        refreshPreview();
    }

    private void refreshPreview() {
        if (!running) {
            return;
        }

        //logger.info("refresh");

        grid.resetAllTempValues();
        if (hoveredBlock != null) {
            previewPiece(hoveredBlock);
        }
        updatePieceBoards();
        // currentPieceBoard.setPiece(currentPiece);
        // nextPieceBoard.setPiece(nextPiece);
    }

    private void previewPiece(GameBlock gameBlock) {
        boolean valid = grid.canPlayPiece(currentPiece, gameBlock.getX(), gameBlock.getY());
        grid.previewPiece(currentPiece, gameBlock.getX(), gameBlock.getY(), valid);
    }

    private void afterPiece() {
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

    private void updatePieceBoards() {
        if (nextPieceListener != null) {
            nextPieceListener.onNextPiece(currentPiece, nextPiece);
        }
    }

    public void setOnNextPiece(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
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

    public void resetBoard() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid.set(x, y, 0);
            }
        }
        nextPiece(true);
        refreshPreview();
    }

    public void score(int linesCleared, int blocksCleared) {

        if (linesCleared > 0) {
            actualScore.set(
                    linesCleared * blocksCleared * 10 * multiplier.get() + actualScore.get());
            level.set(actualScore.get() / 1000);
            multiplier.set(multiplier.get() + 1);

            Timeline anim = new Timeline();
            anim.getKeyFrames().add(new KeyFrame(
                    Duration.ZERO,
                    new KeyValue(displayedScore, displayedScore.get())
            ));
            anim.getKeyFrames().add(new KeyFrame(
                    Duration.millis(300),
                    new KeyValue(displayedScore, actualScore.get())
            ));
            anim.play();

            logger.info(Colour.green("Scored {} points"), linesCleared * blocksCleared * 10 * multiplier.get());
        } else {
            multiplier.set(1);
        }

        //displayedScore.set(actualScore.get());

    }

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


    public boolean isRunning() {
        return running;
    }

    public long getTimerDelayMillis() {
        return Math.max(2500, 12000 - 500 * level.get());
    }

    public Duration getTimerDelay() {
        return Duration.millis(getTimerDelayMillis());
    }
}