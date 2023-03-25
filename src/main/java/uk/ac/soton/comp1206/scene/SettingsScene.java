package uk.ac.soton.comp1206.scene;

import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class SettingsScene extends BaseScene {

    public SettingsScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void initialise() {

    }

    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
    }

}