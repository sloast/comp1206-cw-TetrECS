package uk.ac.soton.comp1206.utils;

import java.util.Objects;
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

    public static final DoubleProperty musicVolume = new SimpleDoubleProperty(0.7);
    public static final DoubleProperty soundEffectVolume = new SimpleDoubleProperty(0.4);
    public static final DoubleProperty masterVolume = new SimpleDoubleProperty(0.5);
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    private static final String MUSIC_PATH = "/music/";
    private static final String SOUND_PATH = "/sounds/";
    private static final String IMAGE_PATH = "/images/";
    private static MediaPlayer soundEffectPlayer;
    private static MediaPlayer musicPlayer;
    // The upcoming music player is created in advance to avoid gaps
    private static MediaPlayer nextMusicPlayer;

    private static boolean doNotInterrupt = false;

    public static boolean isPlayingMusic(String filename) {
        return musicPlayer != null && musicPlayer.getMedia().getSource()
                .equals(Objects.requireNonNull(Multimedia.class.getResource(MUSIC_PATH + filename))
                        .toExternalForm());
    }

    private static Media loadMusic(String filename) throws NullPointerException {
        return new Media(Objects.requireNonNull(Multimedia.class.getResource(MUSIC_PATH + filename))
                .toExternalForm());
    }

    private static Media loadSound(String filename) throws NullPointerException {
        return new Media(Objects.requireNonNull(Multimedia.class.getResource(SOUND_PATH + filename))
                .toExternalForm());
    }

    public static void startMusic(String filename) throws NullPointerException {
        startMusic(filename, MediaPlayer.INDEFINITE);
    }

    public static void startMusic(String filename, int cycleCount) throws NullPointerException {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        logger.info(Colour.purple("Starting music: " + filename));
        musicPlayer = new MediaPlayer(loadMusic(filename));
        musicPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
        musicPlayer.setCycleCount(cycleCount);
        musicPlayer.play();
    }

    public static void startMusicIntro(String intro, String mainLoop) throws NullPointerException {
        startMusic(intro);
        queueMusic(mainLoop, MediaPlayer.INDEFINITE);
        musicPlayer.play();
    }

    public static void queueMusic(String filename, int cycleCount) throws NullPointerException {
        if (musicPlayer == null) {
            startMusic(filename);
        } else {
            musicPlayer.setOnEndOfMedia(Multimedia::playNextMusic);
            musicPlayer.setCycleCount(1);

            nextMusicPlayer = new MediaPlayer(loadMusic(filename));
            nextMusicPlayer.setCycleCount(cycleCount);
        }
    }

    public static void fadeOutMusic() {
        fadeOutMusic(() -> {
        });
    }

    public static void fadeOutMusic(Runnable onFadeEnded) {
        if (musicPlayer != null) {
            musicPlayer.volumeProperty().unbind();

            DoubleProperty volume = musicPlayer.volumeProperty();

            double duration = 1000;

            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(volume, volume.get())),
                    new KeyFrame(Duration.millis(duration), new KeyValue(volume, 0)),
                    new KeyFrame(Duration.millis(duration), event -> onFadeEnded.run()));

            fadeOut.play();
        } else {
            onFadeEnded.run();
        }
    }

    public static void playNextMusic() {
        if (nextMusicPlayer != null) {
            if (musicPlayer != null) {
                musicPlayer.stop();
            }
            musicPlayer = nextMusicPlayer;
            musicPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
            musicPlayer.play();
            nextMusicPlayer = null;
        }
    }


    /**
     * Plays a sound effect after the specified delay
     *
     * @param filename     the name of the file to play
     * @param delay        the delay before playing the sound
     * @param highPriority if {@code true}, the sound cannot be interrupted by other sounds
     * @throws NullPointerException if the file is not found
     */
    public static void playSoundDelayed(String filename, double delay, double volume,
            boolean highPriority) throws NullPointerException {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(delay),
                event -> playSound(filename, volume, highPriority)));
        timeline.play();
    }

    /**
     * Plays a sound effect
     *
     * @param filename the name of the file to play
     * @throws NullPointerException if the file is not found
     */
    public static void playSound(String filename) throws NullPointerException {
        playSound(filename, 1D, false);
    }

    /**
     * Plays a sound effect
     *
     * @param filename the name of the file to play
     * @param volume   the volume of the sound
     * @throws NullPointerException if the file is not found
     */
    public static void playSound(String filename, double volume) throws NullPointerException {
        playSound(filename, volume, false);
    }

    /**
     * Plays a sound effect
     *
     * @param filename     the name of the file to play
     * @param volume       the volume of the sound
     * @param highPriority if {@code true}, the sound cannot be interrupted by other sounds
     * @throws NullPointerException if the file is not found
     */
    public static void playSound(String filename, double volume, boolean highPriority)
            throws NullPointerException {
        if (soundEffectPlayer != null) {
            if (doNotInterrupt && !highPriority) {
                return;
            }
            soundEffectPlayer.stop();
        }
        doNotInterrupt = highPriority;
        soundEffectPlayer = new MediaPlayer(loadSound(filename));
        soundEffectPlayer.volumeProperty()
                .bind(masterVolume.multiply(soundEffectVolume).multiply(volume));

        soundEffectPlayer.setOnEndOfMedia(() -> doNotInterrupt = false);

        soundEffectPlayer.play();
    }


    /**
     * Stops music or sound effects
     *
     * @param category which type of sound to stop
     */
    public static void stop(SoundCategory category) {
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
        return new Image(Objects.requireNonNull(Multimedia.class.getResource(IMAGE_PATH + filename))
                .toExternalForm(), size, size, true, false);
    }

    public static Image getImage(String filename) throws NullPointerException {
        return new Image(Objects.requireNonNull(Multimedia.class.getResource(IMAGE_PATH + filename))
                .toExternalForm());
    }

    /**
     * Which type of sound to stop
     */
    public enum SoundCategory {
        ALL, MUSIC, SOUND_EFFECT
    }

}