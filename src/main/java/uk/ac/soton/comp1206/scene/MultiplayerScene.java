package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene {
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    private MultiplayerGame multiplayerGame;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void setupGame() {
        logger.info("Creating new multiplayer game");

        //Start new game
        multiplayerGame = new MultiplayerGame(5, 5, gameWindow.getCommunicator());
        game = multiplayerGame;
        Game.USE_EXECUTOR_SERVICE = USE_GAME_INTERNAL_TIMER;
    }
}