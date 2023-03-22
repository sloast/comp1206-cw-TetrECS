package uk.ac.soton.comp1206.game;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.utils.Vector2Int;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

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

    private GamePiece currentPiece;
    private final Random random = new Random();

    private GameBlock hoveredBlock = null;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

        nextPiece();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {

        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //Place the piece
        if (grid.canPlayPiece(currentPiece, x, y)) {
            grid.playPiece(currentPiece, x, y);
            nextPiece();
            afterPiecePlaced();
            refreshPreview();
        }
    }

    public void onBlockHoverEnter(GameBlock gameBlock) {
        hoveredBlock = gameBlock;
        refreshPreview();
    }

    public void onBlockHoverExit(GameBlock gameBlock) {
        hoveredBlock = null;
        refreshPreview();
    }

    public void rotateCurrentPiece() {
        currentPiece.rotate();
        refreshPreview();
    }

    private void refreshPreview() {
        grid.resetAllTempValues();
        if (hoveredBlock != null) {
            previewPiece(hoveredBlock);
        }
    }

    private void previewPiece(GameBlock gameBlock) {
        boolean valid = grid.canPlayPiece(currentPiece, gameBlock.getX(), gameBlock.getY());
        grid.placeTempPiece(currentPiece, gameBlock.getX(), gameBlock.getY(), valid);
    }

    private void afterPiecePlaced() {

    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    public GamePiece spawnPiece() {
        return GamePiece.createPiece(random.nextInt(GamePiece.PIECES));
    }

    public void nextPiece() {
        currentPiece = spawnPiece();
    }
}
