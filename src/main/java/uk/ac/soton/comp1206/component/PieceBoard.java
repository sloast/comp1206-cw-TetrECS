package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

public class PieceBoard extends GameBoard {


    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height, false);
    }

    public PieceBoard(double width, double height) {
        super(new Grid(3, 3), width, height, false);
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