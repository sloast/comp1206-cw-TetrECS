package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uk.ac.soton.comp1206.game.Grid;

/**
 * Represents the opponent's boards displayed in a multiplayer game
 */
public class OpponentBoard extends GameBoard {

    /**
     * The username to show above the board
     */
    private final StringProperty username = new SimpleStringProperty("[empty]");

    /**
     * Create a new {@link OpponentBoard}. Board will be empty and the username will be "[empty]"
     *
     * @param grid   the grid to use
     * @param width  the visual width
     * @param height the visual height
     */
    public OpponentBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    /**
     * Set the contents of the board based on a string of values. <br> The values should be in the
     * order: {@code [value at 0,0] [value at 0,1] ... [value at 0,m] [value at 1,0] [value at 1,1] ...
     * [value at 1,m] ... [value at n,m]}
     *
     * @param username the username to show above the board
     * @param values   the values to set the board to, iterated over columns and rows
     */
    public void setContents(String username, String values) {
        this.username.set(username);

        var valueArray = values.split(" ");

        for (int i = 0; i < valueArray.length; i++) {
            int row = i % getRowCount();
            int column = i / getColumnCount();
            grid.set(column, row, Integer.parseInt(valueArray[i]));
        }
    }

    /**
     * Get the username property so it can be bound to
     *
     * @return the username property
     */
    public StringProperty usernameProperty() {
        return username;
    }

}