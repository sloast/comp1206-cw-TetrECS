package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.utils.Vector2Int;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;
    private final int[][] staticGrid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
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
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
                staticGrid[x][y] = 0;
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
        staticGrid[x][y] = value;
    }

    public void set(Vector2Int pos, int value) {
        set(pos.x, pos.y, value);
    }

    public void setPreview(int x, int y, int value) {
        if (value == -1) {
            grid[x][y].set(-staticGrid[x][y] - 1);
            return;
        }

        grid[x][y].set(value + 100);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
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

    public int get(Vector2Int pos) {
        return get(pos.x, pos.y);
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public void resetTempValue(int x, int y) {
        grid[x][y].set(staticGrid[x][y]);
    }

    public void resetAllTempValues() {
        for (var x = 0; x < cols; x++) {
            for (var y = 0; y < rows; y++) {
                resetTempValue(x, y);
            }
        }
    }

    public void reset() {
        for (var x = 0; x < cols; x++) {
            for (var y = 0; y < rows; y++) {
                set(x, y, 0);
            }
        }
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

    public boolean canPlayPiece(GamePiece piece, int x, int y) {
        x--;y--; // offset to start from center of piece
        for (var cx = 0; cx < piece.getBlocks().length; cx++) {
            for (var cy = 0; cy < piece.getBlocks()[0].length; cy++) {
                if (piece.getBlocks()[cx][cy] != 0 && get(x + cx, y + cy) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void playPiece(GamePiece piece, int x, int y) {
        x--;y--; // offset to start from center of piece
        for (var cx = 0; cx < piece.getBlocks().length; cx++) {
            for (var cy = 0; cy < piece.getBlocks()[0].length; cy++) {
                if (piece.getBlocks()[cx][cy] != 0) {
                    set(x + cx, y + cy, piece.getValue());
                }
            }
        }
    }

    public void previewPiece(GamePiece piece, int x, int y, boolean valid) {
        x--;y--; // offset to start from center of piece
        int value = valid ? piece.getValue() : -1;
        for (var cx = 0; cx < piece.getBlocks().length; cx++) {
            for (var cy = 0; cy < piece.getBlocks()[0].length; cy++) {
                if (piece.getBlocks()[cx][cy] != 0 && inBounds(x + cx, y + cy)) {
                    setPreview(x + cx, y + cy, value);
                }
            }
        }
    }

    public void removeTempPiece(GamePiece piece, int x, int y) {
        x--;y--; // offset to start from center of piece
        for (var cx = 0; cx < piece.getBlocks().length; cx++) {
            for (var cy = 0; cy < piece.getBlocks()[0].length; cy++) {
                if (piece.getBlocks()[cx][cy] != 0 && inBounds(x + cx, y + cy)) {
                    resetTempValue(x + cx, y + cy);
                }
            }
        }
    }
}