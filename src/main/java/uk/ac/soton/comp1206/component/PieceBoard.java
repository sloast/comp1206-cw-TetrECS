package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;


/**
 * Represents the current and next pieces displayed in a game
 */
public class PieceBoard extends GameBoard {


    /**
     * Create a new 3x3 piece board
     *
     * @param width  the visual width
     * @param height the visual height
     */
    public PieceBoard(double width, double height) {
        this(width, height, false);
    }

    /**
     * Create a new 3x3 piece board
     *
     * @param width     the visual width
     * @param height    the visual height
     * @param showPivot whether to show a dot in the middle
     */
    public PieceBoard(double width, double height, boolean showPivot) {
        super(new Grid(3, 3), width, height);
        if (!showPivot) {
            getBlock(1, 1).setPivot();
        }
    }

    /**
     * Sets the displayed piece
     *
     * @param piece the {@link GamePiece} to display
     */
    public void setPiece(GamePiece piece) {
        grid.reset();
        grid.playPiece(piece, 1, 1);
    }

    /**
     * Sets the displayed piece, based on its value and orientation
     *
     * @param value    the value of the piece
     * @param rotations the number of clockwise rotations from the default orientation
     */
    public void setPiece(int value, int rotations) {
        this.setPiece(GamePiece.createPiece(value, rotations));
    }

    /**
     * Sets the displayed piece, based on its value, in the default orientation
     *
     * @param value the value of the piece
     */
    public void setPiece(int value) {
        this.setPiece(value, 0);
    }

}