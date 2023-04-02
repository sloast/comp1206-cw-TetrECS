package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Multimedia;

public class InstructionsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void initialise() {
        logger.info("Initialising " + this.getClass().getName());

        scene.setOnKeyPressed(e -> onKeyPress());
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        var mainPane = mainPane("menu-background");

        var instructions = new VBox();
        instructions.alignmentProperty().set(Pos.CENTER);
        mainPane.setCenter(instructions);

        var title = new Label("How to play:");
        title.getStyleClass().add("title");
        instructions.getChildren().add(title);

        var pieceGrid = new GridPane();
        pieceGrid.setHgap(10);
        pieceGrid.setVgap(10);
        pieceGrid.setAlignment(Pos.CENTER);
        var numColumns = 8;
        var numRows = GamePiece.PIECES / numColumns + 1;
        var boardSize = 60;

        for (int i = 0; i < GamePiece.PIECES; i++) {
            var board = new PieceBoard(boardSize, boardSize);
            board.setPiece(i);
            pieceGrid.add(board, i % numColumns, i / numColumns);
        }

        var padding = 100;

        var instructionImage = new ImageView(Multimedia.getImage("instructions.png"));
        instructionImage.setFitHeight(
                gameWindow.getHeight() - boardSize * numRows - padding);
        instructionImage.setFitWidth(gameWindow.getWidth() - padding);
        instructionImage.setPreserveRatio(true);
        instructions.getChildren().add(instructionImage);

        var pieceLabel = new Label("Available pieces:");
        pieceLabel.getStyleClass().add("title");
        instructions.getChildren().add(pieceLabel);

        instructions.getChildren().add(pieceGrid);
    }

    public void onKeyPress() {
        gameWindow.startMenu();
    }
}