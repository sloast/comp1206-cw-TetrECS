package uk.ac.soton.comp1206.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A utility class for loading and playing music and sound effects, and loading images.
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    private static final String MUSIC_PATH = "/music/";
    private static final String SOUND_PATH = "/sounds/";
    private static final String IMAGE_PATH = "/images/";

    private static MediaPlayer soundEffectPlayer;
    private static MediaPlayer musicPlayer;
    private static MediaPlayer musicPlayer2;

    public static DoubleProperty musicVolume = new SimpleDoubleProperty(0.5);
    public static DoubleProperty soundEffectVolume = new SimpleDoubleProperty(0.5);
    public static DoubleProperty masterVolume = new SimpleDoubleProperty(0.8);


    private static Media loadMusic(String filename) throws NullPointerException {
        return new Media(Multimedia.class.getResource(MUSIC_PATH + filename).toExternalForm());
    }

    private static Media loadSound(String filename) throws NullPointerException {
        return new Media(Multimedia.class.getResource(SOUND_PATH + filename).toExternalForm());
    }

    public static void startMusic(String filename) throws NullPointerException {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        musicPlayer = new MediaPlayer(loadMusic(filename));
        musicPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();
    }

    public static void startMusicIntro(String intro, String mainLoop) throws NullPointerException {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        musicPlayer = new MediaPlayer(loadMusic(intro));
        musicPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
        musicPlayer.setOnEndOfMedia(() -> {
            startMusic(mainLoop);
        });
        musicPlayer.play();
    }

    public static void startMusic(String filename, String altFilename) throws NullPointerException {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        musicPlayer = new MediaPlayer(loadMusic(filename));
        musicPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();

        musicPlayer2 = new MediaPlayer(loadMusic(altFilename));
        musicPlayer2.volumeProperty().set(0.);
        musicPlayer2.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer2.play();
    }

    public static void startMusicIntro(String intro, String mainLoop, String altMainLoop)
            throws NullPointerException {
        startMusicIntro(intro, mainLoop);
        musicPlayer2 = new MediaPlayer(loadMusic(altMainLoop));
        musicPlayer2.volumeProperty().set(0.);
        musicPlayer2.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer2.play();
    }

    public static void playSound(String filename) throws NullPointerException {
        if (soundEffectPlayer != null) {
            soundEffectPlayer.stop();
        }
        soundEffectPlayer = new MediaPlayer(loadSound(filename));
        soundEffectPlayer.volumeProperty().bind(masterVolume.multiply(soundEffectVolume));
        soundEffectPlayer.play();
    }

    public static void crossfadeMusic(Duration duration) throws NullPointerException {

        if (musicPlayer == null || musicPlayer2 == null) {
            logger.error("Cannot crossfade music when only one music track is playing");
            return;
        }

        DoubleProperty volume1 = new SimpleDoubleProperty(musicVolume.get());
        DoubleProperty volume2 = new SimpleDoubleProperty(0);

        musicPlayer.volumeProperty().unbind();
        musicPlayer.volumeProperty().bind(volume1);
        musicPlayer2.volumeProperty().unbind();
        musicPlayer2.volumeProperty().bind(volume2);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(volume1, 1)),
                new KeyFrame(Duration.ZERO,
                        new KeyValue(volume2, 0)),
                new KeyFrame(duration,
                        new KeyValue(volume1, 0)),
                new KeyFrame(duration,
                        new KeyValue(volume2, 1))
        );

        timeline.play();
    }

    public static void revertCrossfade() {
        if (musicPlayer != null) {
            musicPlayer.volumeProperty().unbind();
            musicPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
        }
        if (musicPlayer2 != null) {
            musicPlayer2.volumeProperty().unbind();
            musicPlayer2.volumeProperty().set(0.);
        }
    }


    /**
     * There's no real reason for this enum, I just wanted to see if it worked
     */
    public enum Category {
        ALL, MUSIC, SOUND_EFFECT
    }

    /**
     * Stops music or sound effects
     *
     * @param category which type of sound to stop
     */
    public static void stop(Category category) {
        switch (category) {
            case ALL:
                if (soundEffectPlayer != null) {
                    soundEffectPlayer.stop();
                }
            case MUSIC:
                if (musicPlayer != null) {
                    musicPlayer.stop();
                }
                break;
            case SOUND_EFFECT:
                if (soundEffectPlayer != null) {
                    soundEffectPlayer.stop();
                }
                break;
        }
    }

    public static Image getImage(String filename, int size) throws NullPointerException {
        return new Image(Multimedia.class.getResource(IMAGE_PATH + filename).toExternalForm(),
                size, size, true, false);
    }

    public static Image getImage(String filename) throws NullPointerException {
        return new Image(Multimedia.class.getResource(IMAGE_PATH + filename).toExternalForm());
    }


}