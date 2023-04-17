package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uk.ac.soton.comp1206.game.Grid;

public class OpponentBoard extends GameBoard {

    private final StringProperty username = new SimpleStringProperty("[empty]");

    public OpponentBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    public void setContents(String username, String values) {
        this.username.set(username);

        var valueArray = values.split(" ");

        for (int i = 0; i < valueArray.length; i++) {
            int row = i % getRowCount();
            int column = i / getColumnCount();
            grid.set(column, row, Integer.parseInt(valueArray[i]));
        }
    }

    public StringProperty usernameProperty() {
        return username;
    }

}