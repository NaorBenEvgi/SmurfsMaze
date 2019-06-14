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


    public MyViewModel(IModel model) {
        this.model = model;
    }

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

    public int getCharacterRowIndex(){
        return model.getCharacterPositionRow();
    }

    public int getCharacterColumnIndex(){
        return model.getCharacterPositionColumn();
    }

    public void generateMaze(int rows, int cols) {
        model.generateMaze(rows,cols);
    }

    public void solveMaze() {
        model.solveMaze();
    }

    public Maze getMaze(){
        return model.getMaze();
    }

    public void moveCharacter(KeyCode movement) {
        model.moveCharacter(movement);
    }

    public int getGoalRow(){
        return model.getGoalRow();

    }

    public int getGoalColumn(){
        return model.getGoalColumn();
    }

    public ArrayList<Position> getSolution(){
        return model.getSolution();
    }

    public void saveMaze(File compressedMaze) {
        model.saveMaze(compressedMaze);
    }

    public void loadMaze(File compressedMaze) {
        model.loadMaze(compressedMaze);
    }

    public void exitGame() {
        model.exitGame();
    }
}
