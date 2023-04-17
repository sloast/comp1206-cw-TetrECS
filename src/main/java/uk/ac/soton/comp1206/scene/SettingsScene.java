package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Multimedia;

public class SettingsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(SettingsScene.class);

    public SettingsScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void build() {
        var mainPane = setupMain("challenge-background");

        var titleContainer = new HBox();
        titleContainer.setAlignment(Pos.CENTER);

        var title = new Label("Settings");
        title.getStyleClass().add("title");

        titleContainer.getChildren().add(title);
        mainPane.setTop(titleContainer);

        // master volume
        var masterVolumeSlider = new Slider(0, 100, Multimedia.masterVolume.get() * 100);
        Multimedia.masterVolume.bind(masterVolumeSlider.valueProperty().divide(100));

        var masterVolumeLabel = new Label("Master Volume");
        masterVolumeLabel.getStyleClass().add("heading");

        var masterVolumeNumber = new Label();
        masterVolumeNumber.getStyleClass().add("heading");
        masterVolumeNumber.setPrefWidth(50);
        masterVolumeNumber.textProperty().bind(masterVolumeSlider.valueProperty().asString("%.0f"));

        // music volume
        var musicVolumeSlider = new Slider(0, 100, Multimedia.musicVolume.get() * 100);
        Multimedia.musicVolume.bind(musicVolumeSlider.valueProperty().divide(100));

        var musicVolumeLabel = new Label("Music Volume");
        musicVolumeLabel.getStyleClass().add("heading");

        var musicVolumeNumber = new Label();
        musicVolumeNumber.getStyleClass().add("heading");
        musicVolumeNumber.textProperty().bind(musicVolumeSlider.valueProperty().asString("%.0f"));

        // sfx volume

        var sfxVolumeSlider = new Slider(0, 100, Multimedia.soundEffectVolume.get() * 100);
        Multimedia.soundEffectVolume.bind(sfxVolumeSlider.valueProperty().divide(100));

        var sfxVolumeLabel = new Label("SFX Volume");
        sfxVolumeLabel.getStyleClass().add("heading");

        var sfxVolumeNumber = new Label();
        sfxVolumeNumber.getStyleClass().add("heading");
        sfxVolumeNumber.textProperty().bind(sfxVolumeSlider.valueProperty().asString("%.0f"));

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10);
        settingsGrid.setAlignment(Pos.CENTER);

        settingsGrid.add(masterVolumeLabel, 0, 0);
        settingsGrid.add(masterVolumeSlider, 1, 0);
        settingsGrid.add(masterVolumeNumber, 2, 0);
        settingsGrid.add(musicVolumeLabel, 0, 1);
        settingsGrid.add(musicVolumeSlider, 1, 1);
        settingsGrid.add(musicVolumeNumber, 2, 1);
        settingsGrid.add(sfxVolumeLabel, 0, 2);
        settingsGrid.add(sfxVolumeSlider, 1, 2);
        settingsGrid.add(sfxVolumeNumber, 2, 2);

        mainPane.setCenter(settingsGrid);
    }

    @Override
    public void initialise() {
        scene.setOnKeyPressed(this::onKeyPress);
    }

    public void onKeyPress(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        //logger.info("Key pressed: " + keyCode);
        switch (keyCode) {
            case ESCAPE -> gameWindow.startMenu();
        }
    }

}