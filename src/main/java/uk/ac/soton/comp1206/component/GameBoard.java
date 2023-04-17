package uk.ac.soton.comp1206.component;

import java.util.Arrays;
import java.util.Set;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.BlockHoverEnterListener;
import uk.ac.soton.comp1206.event.BlockHoverExitListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.utils.Vector2;

/**
 * A GameBoard is a visual component to represent the visual GameBoard. It extends a GridPane to
 * hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of its own, for example, for displaying an upcoming
 * block. It can also be linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in
 * it, which should take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;
    private BlockHoverEnterListener blockHoverEnterListener;
    private BlockHoverExitListener blockHoverExitListener;
    private RightClickedListener rightClickedListener;


    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     *
     * @param grid   linked grid
     * @param width  the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        //Build the GameBoard
        build();


    }

    /**
     * Create a new GameBoard with its own internal grid, specifying the number of columns and rows,
     * along with the visual width and height.
     *
     * @param cols   number of columns for internal grid
     * @param rows   number of rows for internal grid
     * @param width  the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols, rows);

        //Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     *
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    public GameBlock getBlock(Vector2 pos) {
        return blocks[pos.x][pos.y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}", cols, rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x, y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     *
     * @param x column
     * @param y row
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block, x, y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x, y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));
        block.setOnMouseEntered((e) -> blockHoverEntered(e, block));
        block.setOnMouseExited((e) -> blockHoverExited(e, block));

        return block;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     *
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Set the listener to handle an event when a block is hovered over
     *
     * @param listener listener to add
     */
    public void setOnBlockHoverEnter(BlockHoverEnterListener listener) {
        this.blockHoverEnterListener = listener;
    }

    /**
     * Set the listener to handle an event when a block is unhovered
     *
     * @param listener listener to add
     */
    public void setOnBlockHoverExit(BlockHoverExitListener listener) {
        this.blockHoverExitListener = listener;
    }

    /**
     * Set the listener to handle an event when a block is right clicked
     *
     * @param listener listener to add
     */
    public void setOnRightClick(RightClickedListener listener) {
        this.rightClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     *
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {

        if (blockClickedListener != null && event.getButton() == MouseButton.PRIMARY) {
            logger.info("Block clicked: {}, {}", block.getX(), block.getX());
            blockClickedListener.blockClicked(block);
            event.consume();
        } else if (rightClickedListener != null && event.getButton() == MouseButton.SECONDARY) {
            logger.info("Block right clicked: {}, {}", block.getX(), block.getX());

            rightClickedListener.onRightClicked(block);
            event.consume();
        }
    }

    /**
     * Triggered when a block is hovered over. Call the attached listener.
     *
     * @param event mouse event
     * @param block block hovered over
     */
    private void blockHoverEntered(MouseEvent event, GameBlock block) {
        //logger.info("Mouse entered: {}", block);
        if (blockHoverEnterListener != null) {
            blockHoverEnterListener.blockEntered(block);
        }
    }

    /**
     * Triggered when a block is unhovered. Call the attached listener.
     *
     * @param event mouse event
     * @param block block unhovered
     */
    private void blockHoverExited(MouseEvent event, GameBlock block) {
        //logger.info("Mouse exited: {}", block);
        if (blockHoverEnterListener != null) {
            blockHoverExitListener.blockExited(block);
        }
    }

    /**
     * Get the attached grid
     *
     * @return the grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Start the animation when lines are cleared
     *
     * @param blocks   the blocks to clear
     * @param rootPane the window to add the animation to
     */
    public void lineCleared(Set<Vector2> blocks, Pane rootPane) {
        Vector2[] blocksArray = blocks.toArray(Vector2[]::new);
        Arrays.sort(blocksArray, Vector2::compareTo);
        var delay = 0;

        for (var block : blocksArray) {
            getBlock(block).clearAnimation(rootPane, delay);
            delay += 50;
        }
    }


}