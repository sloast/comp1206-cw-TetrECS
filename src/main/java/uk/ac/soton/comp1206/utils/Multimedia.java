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

    /**
     * The volume of the music, from 0 to 1
     */
    public static final DoubleProperty musicVolume = new SimpleDoubleProperty(0.7);
    /**
     * The volume of sound effects, from 0 to 1
     */
    public static final DoubleProperty soundEffectVolume = new SimpleDoubleProperty(0.5);
    /**
     * The overall volume, from 0 to 1
     */
    public static final DoubleProperty masterVolume = new SimpleDoubleProperty(0.5);
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    private static final String MUSIC_PATH = "/music/";
    private static final String SOUND_PATH = "/sounds/";
    private static final String IMAGE_PATH = "/images/";

    /**
     * The sound effect player that is currently playing
     */
    private static MediaPlayer soundEffectPlayer;

    /**
     * The music player that is currently playing
     */
    private static MediaPlayer musicPlayer;

    // The upcoming music player is created in advance to avoid gaps
    /**
     * The music player that will play after the current one
     */
    private static MediaPlayer nextMusicPlayer;

    /**
     * Whether the current sound effect should be interrupted
     */
    private static boolean doNotInterrupt = false;

    /**
     * Checks if the given music file is currently playing
     *
     * @param filename the filename of the music
     * @return {@code true} if the music is playing, {@code false} otherwise
     */
    public static boolean isPlayingMusic(String filename) {
        return musicPlayer != null && musicPlayer.getMedia().getSource()
                .equals(Objects.requireNonNull(Multimedia.class.getResource(MUSIC_PATH + filename))
                        .toExternalForm());
    }

    /**
     * Loads a music file from the given file in the music folder
     *
     * @param filename the filename of the music
     * @return the music as a {@link Media} object
     * @throws NullPointerException
     */
    private static Media loadMusic(String filename) throws NullPointerException {
        return new Media(Objects.requireNonNull(Multimedia.class.getResource(MUSIC_PATH + filename))
                .toExternalForm());
    }

    /**
     * Loads a sound effect from the given file in the sounds folder
     *
     * @param filename the filename of the sound effect
     * @return the sound effect
     * @throws NullPointerException if the file is not found
     */
    private static Media loadSound(String filename) throws NullPointerException {
        return new Media(Objects.requireNonNull(Multimedia.class.getResource(SOUND_PATH + filename))
                .toExternalForm());
    }

    /**
     * Plays the given music file and loops it until stopped
     *
     * @param filename the filename of the music to play
     * @throws NullPointerException if the file is not found
     */
    public static void startMusic(String filename) throws NullPointerException {
        startMusic(filename, MediaPlayer.INDEFINITE);
    }

    /**
     * Plays the given music file
     *
     * @param filename   the filename of the music to play
     * @param cycleCount the number of times to play the music
     * @throws NullPointerException if the file is not found
     */
    public static void startMusic(String filename, int cycleCount) throws NullPointerException {
        logger.info(Colour.purple("Starting music: " + filename));

        if (musicPlayer != null) {
            musicPlayer.stop();
        }

        musicPlayer = new MediaPlayer(loadMusic(filename));
        musicPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
        musicPlayer.setCycleCount(cycleCount);
        musicPlayer.play();
    }

    /**
     * Plays the intro music once, then loops over the main loop
     *
     * @param intro    the filename of the intro music
     * @param mainLoop the filename of the main loop music
     * @throws NullPointerException if the file is not found
     */
    public static void startMusicIntro(String intro, String mainLoop) throws NullPointerException {
        logger.info(Colour.purple("Starting music intro: " + intro + " -> " + mainLoop));
        startMusic(intro);
        queueMusic(mainLoop, MediaPlayer.INDEFINITE);
        musicPlayer.play();
    }

    /**
     * Sets the next music to play after the current one
     *
     * @param filename   the filename of the music to play
     * @param cycleCount the number of times to play the music
     * @throws NullPointerException if the file is not found
     */
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

    /**
     * Fades out the currently playing music
     */
    public static void fadeOutMusic() {
        fadeOutMusic(() -> {
        });
    }

    /**
     * Fades out the music and calls the specified method after
     *
     * @param onFadeEnded the {@link Runnable} to run when the fade is complete
     */
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

    /**
     * Plays the music from the queued music player
     */
    public static void playNextMusic() {
        if (nextMusicPlayer != null) {
            logger.info(
                    Colour.purple("Playing next music: " + nextMusicPlayer.getMedia().getSource()));

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
     * @param volume       the relative volume of the sound
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
        logger.debug(Colour.purple("Playing sound: " + filename));

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
     * Stops all music and sound effects
     */
    public static void stop() {
        logger.info(Colour.purple("Stopping audio"));
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        if (soundEffectPlayer != null) {
            soundEffectPlayer.stop();
        }
    }

    /**
     * Gets an image from the resources folder
     *
     * @param filename the name of the file to get
     * @param size     the size of the image, in pixels. Use this to force the image to load at a
     *                 specific resolution
     * @return the {@link Image}
     * @throws NullPointerException if the file is not found
     */
    public static Image getImage(String filename, int size) throws NullPointerException {
        return getImage(filename, size, size);
    }

    /**
     * Gets an image from the resources folder
     *
     * @param filename the name of the file to get
     * @param width    the width of the image, in pixels. Use this to force the image to load at a
     *                 specific resolution
     * @param height   the height of the image, in pixels
     * @return the {@link Image}
     * @throws NullPointerException if the file is not found
     */
    public static Image getImage(String filename, int width, int height)
            throws NullPointerException {
        return new Image(Objects.requireNonNull(Multimedia.class.getResource(IMAGE_PATH + filename))
                .toExternalForm(), width, height, true, false);
    }

    /**
     * Gets an image from the resources folder
     *
     * @param filename the name of the file to get
     * @return the {@link Image}
     * @throws NullPointerException if the file is not found
     */
    public static Image getImage(String filename) throws NullPointerException {
        return new Image(Objects.requireNonNull(Multimedia.class.getResource(IMAGE_PATH + filename))
                .toExternalForm());
    }

}