package View;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * This class is responsible of drawing the maze in the application.
 */
public class MazeDisplayer extends Canvas {

    private Maze board;
    private int[][] maze;
    private int rowIndex, colIndex;
    private int goalRowIndex,goalColIndex;
    protected ArrayList<Position> solutionPath;
    private StringProperty wallImage = new SimpleStringProperty();
    private StringProperty characterImage = new SimpleStringProperty();
    private StringProperty goalImage = new SimpleStringProperty();
    private StringProperty hintImage = new SimpleStringProperty();
    protected int numOfHints;
    protected boolean solutionDisplayed;

    /**
     * constructor
     */
    public MazeDisplayer(){
        widthProperty().addListener(ev -> drawMaze());
        heightProperty().addListener(ev -> drawMaze());
    }

    /**
     * Sets the maze object in this class to a given maze from the controller
     * @param board the given maze
     */
    public void setMaze(Maze board){
        this.board = board;
        maze = new int[board.getRows()][board.getCols()];
        for(int i=0; i<maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                maze[i][j] = board.getCellValue(i, j);
            }
        }
        setCharacterPosition(board.getStartPosition().getRowIndex(),board.getStartPosition().getColumnIndex());
        setGoalPosition(board.getGoalPosition().getRowIndex(),board.getGoalPosition().getColumnIndex());
    }

    /**
     * Sets the position of the character to given indexes.
     * @param row the row index
     * @param col the column index
     */
    public void setCharacterPosition(int row, int col){
        rowIndex = row;
        colIndex = col;
        drawMaze();
    }

    /**
     * Sets the goal position to given indexes.
     * @param row the row index
     * @param col the column index
     */
    public void setGoalPosition(int row, int col){
        goalColIndex = col;
        goalRowIndex = row;
    }

    /**
     * Overrides of canvas class functions.
     * These functions are responsible for setting the size of the maze in the application.
     * @return the dimensions of the maze in the application
     */
    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    @Override
    public double minWidth(double height) {
        return 400;
    }

    @Override
    public double minHeight(double width) {
        return 400;
    }

    @Override
    public double maxWidth(double height) {
        return 1000;
    }

    @Override
    public double maxHeight(double width) {
        return 1000;
    }

    @Override
    public void resize(double width, double height) {
        super.setHeight(height);
        super.setWidth(width);
        drawMaze();
    }

    /**
     * Draws the maze in the application.
     */
    public void drawMaze(){
        if(maze == null){
            return;
        }
        //calculates the cells' dimensions
        double height = getHeight() / maze.length;
        double width = getWidth() / maze[0].length;

        try{
            //loads the images
            Image wall = new Image(new FileInputStream(getWallImage()));
            Image character = new Image(new FileInputStream(getCharacterImage()));
            Image goal = new Image(new FileInputStream(getGoalImage()));
            Image hint = new Image(new FileInputStream(getHintImage()));

            GraphicsContext graphics = getGraphicsContext2D();
            graphics.clearRect(0,0,getWidth(),getHeight());

            //assigning the amount of hints
            if(solutionPath != null) {
                if (!solutionDisplayed)
                    numOfHints = Math.min(numOfHints, solutionPath.size());
                else
                    numOfHints = solutionPath.size();
            }

            //draws the maze
            for(int i=0; i<maze.length; i++){
                for(int j=0; j<maze[0].length; j++){
                    if(maze[i][j] == 1){
                        graphics.drawImage(wall,j*width,i*height,width,height);
                    }
                }
            }
            for(int j=0; j<numOfHints; j++){
                graphics.drawImage(hint,width*solutionPath.get(j).getColumnIndex(),height*solutionPath.get(j).getRowIndex(),width,height);
            }

            //draws the character and the goal
            graphics.drawImage(character,colIndex*width,rowIndex*height,width,height);
            graphics.drawImage(goal,goalColIndex*width,goalRowIndex*height,width,height);
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Getters of the character's position
     * @return the indexes of the character's position
     */
    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    /**
     * Getters of the images used in the application to draw the character, the walls, the solution and the goal
     * @return the images
     */
    public String getWallImage() {
        return wallImage.get();
    }

    public String getCharacterImage(){
        return characterImage.get();
    }


    public String getGoalImage() {
        return goalImage.get();
    }


    public String getHintImage() {
        return hintImage.get();
    }

}
