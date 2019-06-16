package ViewModel;

import Model.IModel;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer{

    private IModel model;
    public StringProperty characterRow = new SimpleStringProperty();
    public StringProperty characterColumn = new SimpleStringProperty();

    /**
     * constructor - sets a model to this class
     * @param model the given model object
     */
    public MyViewModel(IModel model) {
        this.model = model;
    }

    /**
     * Getter of the maze from the model.
     * @returnthe maze from the model
     */
    public Maze getBoard(){
        return model.getMaze();
    }


    @Override
    public void update(Observable o, Object arg) {
        if(o == model){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    characterRow.set(model.getCharacterPositionRow() + "");
                    characterColumn.set(model.getCharacterPositionColumn() + "");
                }
            });
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Getters of the character's position from the model.
     * @return the character's position
     */
    public int getCharacterRowIndex(){
        return model.getCharacterPositionRow();
    }

    public int getCharacterColumnIndex(){
        return model.getCharacterPositionColumn();
    }

    /**
     * Generates a maze with the given dimensions.
     * @param rows the maze's height
     * @param cols the maze's width
     */
    public void generateMaze(int rows, int cols) {
        model.generateMaze(rows,cols);
    }

    /**
     * Solves the maze
     */
    public void solveMaze() {
        model.solveMaze();
    }

    /**
     * Moves the character in the maze in the direction of the pressed key.
     * @param movement the pressed key
     */
    public void moveCharacter(KeyCode movement) {
        model.moveCharacter(movement);
    }

    /**
     * Getters of the goal's position indexes
     * @return
     */
    public int getGoalRow(){
        return model.getGoalRow();

    }

    public int getGoalColumn(){
        return model.getGoalColumn();
    }

    /**
     * Getter of the solution of the maze.
     * @return the positions that the solution is compounded of, ordered in an array list
     */
    public ArrayList<Position> getSolution(){
        return model.getSolution();
    }

    /**
     * Saves the maze in a text file.
     * @param compressedMaze the file that contains the maze
     */
    public void saveMaze(File compressedMaze) {
        model.saveMaze(compressedMaze);
    }

    /**
     * Loads a maze from a text file.
     * @param compressedMaze the file that contains the maze
     */
    public void loadMaze(File compressedMaze) {
        model.loadMaze(compressedMaze);
    }

    /**
     * Exits the game.
     */
    public void exitGame() {
        model.exitGame();
    }
}
