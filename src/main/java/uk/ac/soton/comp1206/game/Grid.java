package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display
 * of the contents of the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside
 * the grid.
 * <p>
 * The Grid should be linked to a GameBoard for its display.
 */
public class Grid {

    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * This stores the value of each {@code GameBlock} in the grid. If a value is modified, it is
     * automatically updated in the UI
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * This stores the value of each block in respect to the game's internal logic, i.e. only the
     * fully placed pieces, not previews
     */
    private final int[][] staticGrid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];
        staticGrid = new int[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
                staticGrid[x][y] = 0;
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be
     * used for binding.
     *
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid.
     *
     * @param x     column
     * @param y     row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
        staticGrid[x][y] = value;
    }

    /**
     * Temporarily update the displayed value at the given x and y index within the grid. This can
     * be reverted by calling {@link #resetAllTempValues()}
     *
     * @param x     column
     * @param y     row
     * @param value the new value
     */
    public void setPreview(int x, int y, int value) {
        if (value == -1) {
            grid[x][y].set(-staticGrid[x][y] - 1);
            return;
        }

        grid[x][y].set(value + 100);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     *
     * @param x column
     * @param y row
     * @return the value, or -1 if the index is out of bounds
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return staticGrid[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Checks if the given (x, y) position is within the bounds of the grid
     *
     * @param x the column
     * @param y the row
     * @return {@code true} if the position is within the bounds of the grid, {@code false}
     * otherwise
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    /**
     * Resets the temporary value to its static value (i.e. removes the preview)
     *
     * @param x column
     * @param y row
     */
    public void resetTempValue(int x, int y) {
        grid[x][y].set(staticGrid[x][y]);
    }

    /**
     * Resets all temporary values to their static values (i.e. removes all previews/ghost pieces)
     */
    public void resetAllTempValues() {
        logger.debug("Resetting all temporary values");

        for (var x = 0; x < cols; x++) {
            for (var y = 0; y < rows; y++) {
                resetTempValue(x, y);
            }
        }
    }

    /**
     * Resets all values to 0 (empty)
     */
    public void reset() {
        for (var x = 0; x < cols; x++) {
            for (var y = 0; y < rows; y++) {
                set(x, y, 0);
            }
        }
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

    /**
     * Checks if the given {@link GamePiece} can be played at the specified position
     *
     * @param piece the piece to check
     * @param x     the x position
     * @param y     the y position
     * @return {@code true} if the piece can be played at the specified position, {@code false}
     * otherwise
     */
    public boolean canPlayPiece(GamePiece piece, int x, int y) {
        x--;
        y--; // offset to start from center of piece
        for (var cx = 0; cx < piece.getBlocks().length; cx++) {
            for (var cy = 0; cy < piece.getBlocks()[0].length; cy++) {
                if (piece.getBlocks()[cx][cy] != 0 && get(x + cx, y + cy) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Plays the given {@link GamePiece} at the specified position, relative to the center of the
     * piece. This method does not check if the piece can be played at the specified position, which
     * can be done with {@link Grid#canPlayPiece}
     *
     * @param piece the piece to play
     * @param x     the x position
     * @param y     the y position
     * @see Grid#canPlayPiece
     */
    public void playPiece(GamePiece piece, int x, int y) {

        x--;
        y--; // offset to start from center of piece

        for (var cx = 0; cx < piece.getBlocks().length; cx++) {
            for (var cy = 0; cy < piece.getBlocks()[0].length; cy++) {
                if (piece.getBlocks()[cx][cy] != 0) {
                    set(x + cx, y + cy, piece.getValue());
                }
            }
        }
    }

    /**
     * Creates a temporary ghost piece to show where the next piece will be placed
     *
     * @param piece the {@link GamePiece} to preview
     * @param x     the x position
     * @param y     the y position
     * @param valid whether the placement is valid or not. If false, the piece will be displayed in
     *              red
     * @see Grid#resetAllTempValues
     */
    public void previewPiece(GamePiece piece, int x, int y, boolean valid) {

        x--;
        y--; // offset to start from center of piece

        int value = valid ? piece.getValue() : -1;
        for (var cx = 0; cx < piece.getBlocks().length; cx++) {
            for (var cy = 0; cy < piece.getBlocks()[0].length; cy++) {
                if (piece.getBlocks()[cx][cy] != 0 && inBounds(x + cx, y + cy)) {
                    setPreview(x + cx, y + cy, value);
                }
            }
        }
    }

    /**
     * Returns a string representation of the board, formatted as follows:
     * <p>
     * BOARD [value at (0, 0)] [value at (0, 1)] ... [value at (0, n)] [value at (1, 0)] ... [value
     * at (m, n)]
     * </p>
     *
     * @return the resulting string
     */
    public String toString() {
        var sb = new StringBuilder("BOARD");
        for (var x = 0; x < rows; x++) {
            for (var y = 0; y < cols; y++) {
                sb.append(" ").append(get(x, y));
            }
        }

        return sb.toString();
    }
}