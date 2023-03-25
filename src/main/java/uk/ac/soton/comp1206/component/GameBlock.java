package uk.ac.soton.comp1206.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.utils.Multimedia;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.MAGENTA,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE,

            //Color.rgb(255, 0, 0, .5),

            Color.RED,
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;
    private static final int CORNER_RADIUS = 15;
    private static final Image BASE_IMAGE = Multimedia.getImage("block.png", 160);

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        var val = value.get();
        if(val == 0) {
            //paintEmpty();
            paintEmpty();
        } else if (val < 0) { // Add red over existing color to show invalid placement
            var col = COLOURS[-val - 1]; // find the original color of this block

            if (val == -1) {
                paintColor(Color.rgb(255, 0, 0, 0.7));
            } else {
                paintColor(Color.RED.interpolate(col, 0.3));
            }


        } else if (val >= 100) { // preview -> paint semi-transparent
            var col = COLOURS[val - 100];
            paintColor(Color.TRANSPARENT.interpolate(col, 0.7));

        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[val]);

        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        //Fill
        //gc.setFill(Color.WHITE);
        gc.setFill(Color.rgb(100, 100, 100, 0.3));
        //gc.fillRoundRect(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS);


        //gc.drawImage(Multimedia.getImage("blockw.png"), 0, 0, width, height);

        //Border
        gc.setStroke(Color.rgb(150, 150, 150, 0.7));
        gc.strokeRoundRect(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS);

        //paintImage(Color.rgb(100, 100, 100, 0.5));
    }

    private void paintImage(Color color) {

        //Image blankImage = Multimedia.getImage("block.png");
        WritableImage newImage = new WritableImage((int) BASE_IMAGE.getWidth(),
                (int) BASE_IMAGE.getHeight());
        var reader = BASE_IMAGE.getPixelReader();
        var writer = newImage.getPixelWriter();

        for (int x = 0; x < newImage.getWidth(); x++) {
            for (int y = 0; y < newImage.getHeight(); y++) {
                if (reader.getColor(x, y).equals(Color.WHITE)) {
                    writer.setColor(x, y, color);
                } else if (reader.getColor(x, y).equals(Color.BLACK)) {
                    writer.setColor(x, y, color.interpolate(Color.BLACK, 0.2));
                } else {
                    writer.setColor(x, y, reader.getColor(x, y).interpolate(color, 0.2));
                }
            }
        }

        var gc = getGraphicsContext2D();
        gc.drawImage(newImage, 0, 0, width, height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        //gc.setFill(colour);
        //gc.fillRoundRect(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS);

        //Border
        //gc.setStroke(Color.GREY);
        //gc.strokeRoundRect(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS);

        paintImage((Color) colour);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing its colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}