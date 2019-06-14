package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.ArrayList;

public interface IModel {

    void generateMaze(int rows, int cols);
    Maze getMaze();
    int getCharacterPositionRow();
    int getCharacterPositionColumn();
    void moveCharacter(KeyCode movement);
    void solveMaze();
    int getGoalRow();
    int getGoalColumn();

    ArrayList<Position> getSolution();

    void saveMaze(File compressedMaze);

    void loadMaze(File compressedMaze);

    void exitGame();
}
