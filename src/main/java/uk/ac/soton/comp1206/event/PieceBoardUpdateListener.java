package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Interface for listening for changes to the pieces displayed on the {@code PieceBoard}s. When a
 * new piece is added or a piece is rotated, this listener will be called to update the UI.
 */
public interface PieceBoardUpdateListener {

    /**
     * Handle the piece board update
     *
     * @param currentPiece the current piece
     * @param nextPiece the next piece
     */
    void updatePieceBoards(GamePiece currentPiece, GamePiece nextPiece);
}