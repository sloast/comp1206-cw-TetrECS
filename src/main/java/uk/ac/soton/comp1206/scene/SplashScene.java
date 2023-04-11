package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Multimedia;

/**
 * The splash screen when first starting up the game.
 */
public class SplashScene extends BaseScene {

    private boolean isExiting = false;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public SplashScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    public SplashScene(GameWindow gameWindow, boolean isExiting) {
        super(gameWindow);
        this.isExiting = isExiting;
    }

    @Override
    public void initialise() {

    }

    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var pane = new StackPane();
        pane.setMaxWidth(gameWindow.getWidth());
        pane.setMaxHeight(gameWindow.getHeight());
        pane.getStyleClass().add("intro");
        root.getChildren().add(pane);

        var mainPane = new BorderPane();
        pane.getChildren().add(mainPane);

        ImageView splash = new ImageView(Multimedia.getImage("surosuto.png", 512));
        mainPane.setCenter(splash);
        splash.opacityProperty().set(0);

        var scaleAnim = new ScaleTransition(Duration.seconds(5), splash);
        scaleAnim.setFromX(.5);
        scaleAnim.setFromY(.5);
        scaleAnim.setToX(1.25);
        scaleAnim.setToY(1.25);

        scaleAnim.setInterpolator(Interpolator.LINEAR);


        var fadeAnim = new FadeTransition(Duration.seconds(1), splash);
        fadeAnim.setFromValue(0);
        fadeAnim.setToValue(1);
        fadeAnim.setDelay(Duration.seconds(.5));


        var fadeOutAnim = new FadeTransition(Duration.seconds(1), splash);
        fadeOutAnim.setFromValue(1);
        fadeOutAnim.setToValue(0);
        fadeOutAnim.setDelay(Duration.seconds(1));

        fadeAnim.setOnFinished((e) -> fadeOutAnim.play());

        if (isExiting) {

            splash.setScaleX(0.75);
            splash.setScaleY(0.75);

            fadeAnim.setDelay(Duration.seconds(0));
            fadeAnim.setDuration(Duration.seconds(0.5));
            fadeOutAnim.setDelay(Duration.seconds(0));
            fadeOutAnim.setDuration(Duration.seconds(0.5));

            fadeOutAnim.setOnFinished((e) -> App.getInstance().shutdown());

        } else {
            fadeOutAnim.setOnFinished((e) -> gameWindow.startMenu());
        }


        if (!isExiting) {
            scaleAnim.play();
        }

        fadeAnim.play();
    }
}