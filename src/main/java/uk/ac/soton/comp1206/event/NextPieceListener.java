package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceListener {
    public void onNextPiece(GamePiece nextPiece, GamePiece followingPiece);
}