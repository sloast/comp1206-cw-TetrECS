package uk.ac.soton.comp1206.event;

/**
 * The game loop listener is called when the game timer resets
 */
public interface GameLoopListener {

    /**
     * Handle a game loop event
     */
    void onGameLoop();
}