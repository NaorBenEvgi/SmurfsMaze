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

    public MazeDisplayer(){
        widthProperty().addListener(ev -> drawMaze());
        heightProperty().addListener(ev -> drawMaze());
    }

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

    public void setCharacterPosition(int row, int col){
        rowIndex = row;
        colIndex = col;
        drawMaze();
    }

    public void setGoalPosition(int row, int col){
        goalColIndex = col;
        goalRowIndex = row;
    }


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


    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public String getWallImage() {
        return wallImage.get();
    }

    public String getCharacterImage(){
        return characterImage.get();
    }

    public void setWallImage(String wallImage){
        this.wallImage.set(wallImage);
    }

    public void setCharacterImage(String characterImage){
        this.characterImage.set(characterImage);
    }

    public String getGoalImage() {
        return goalImage.get();
    }

    public StringProperty goalImageProperty() {
        return goalImage;
    }

    public void setGoalImage(String goalImage) {
        this.goalImage.set(goalImage);
    }

    public String getHintImage() {
        return hintImage.get();
    }

    public StringProperty hintImageProperty() {
        return hintImage;
    }

    public void setHintImage(String hintImage) {
        this.hintImage.set(hintImage);
    }
}
