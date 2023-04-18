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

    public void setPiece(GamePiece piece) {
        grid.reset();
        grid.playPiece(piece, 1, 1);
    }

    public void setPiece(int value, int rotations) {
        this.setPiece(GamePiece.createPiece(value, rotations));
    }

    public void setPiece(int value) {
        this.setPiece(value, 0);
    }

}