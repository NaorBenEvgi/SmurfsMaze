package View;

import algorithms.mazeGenerators.Maze;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import ViewModel.MyViewModel;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Observable;
import java.util.Observer;

public class MyViewController implements IView, Observer {

    @FXML
    private MyViewModel viewModel;
    public MazeDisplayer mazeDisplayer;
    public StringProperty characterRow = new SimpleStringProperty();
    public StringProperty characterColumn = new SimpleStringProperty();
    public BorderPane borderPane;
    public TextField rowsTextField;
    public TextField colsTextField;
    public Label rowsLabel;
    public Label colsLabel;
    public Label hintsLabel;
    public MenuItem generateMazeButton;
    public RadioButton solveMazeButton;
    public MenuItem helpButton;
    public Button hintButton;
    public MenuItem exitButton;
    public MenuItem saveButton;
    public MenuItem loadButton;
    public MenuItem propertiesButton;
    public MenuItem programmersInfo;
    public MenuItem algorithmsInfo;
    public RadioButton volumeButton;
    public Label timeLabel;
    public static MediaPlayer player;
    public static Media song;
    private boolean songPlayed = true;
    private boolean solutionDisplayed = false;
    private int numOfHints = 0;
    private boolean started = false;
    private long startedTime, finishedTime;



    public void bindProperties(MyViewModel viewModel){
        rowsLabel.textProperty().bind(viewModel.characterRow);
        colsLabel.textProperty().bind(viewModel.characterColumn);
    }

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        bindProperties(viewModel);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o == viewModel){
            if(solutionDisplayed){
                mazeDisplayer.solutionDisplayed = true;
                solveMazeButton.setSelected(true);
            }
            else{
                mazeDisplayer.solutionDisplayed = false;
                solveMazeButton.setSelected(false);
            }
            displayMaze(viewModel.getBoard());
            generateMazeButton.setDisable(false);
            solveMazeButton.setDisable(false);
            mazeDisplayer.requestFocus();
        }
    }

    public void generateMaze(){
        int rows, cols;
        try{
            rows = Integer.valueOf(rowsTextField.getText());
            cols = Integer.valueOf(colsTextField.getText());
        }
        catch(Exception e){
            displayAlert("Illegal Maze Size", "The height and width of the maze must be integers!");
            return;
        }
        started = true;
        solutionDisplayed = false;
        solveMazeButton.setSelected(false);
        hintButton.setDisable(false);
        saveButton.setDisable(false);
        setNumOfHints(0);
        playSong("resources/music/EntranceOriginalSmurfSong.mp3");
        if(rows < 10 || cols < 10){
            rowsTextField.setText("10");
            colsTextField.setText("10");
            displayAlert("Illegal Maze Dimensions","One or both of the dimensions you entered is too small, the maze has been initialized as 10x10.");
        }
        viewModel.generateMaze(Integer.valueOf(rowsTextField.getText()),Integer.valueOf(colsTextField.getText()));
        timeLabel.setText("");
        startedTime = System.currentTimeMillis();
        mazeDisplayer.requestFocus();
    }

    public void displayMaze(Maze maze){
        if(maze != null) {
            mazeDisplayer.setGoalPosition(viewModel.getGoalRow(), viewModel.getGoalColumn());
            mazeDisplayer.setMaze(maze);
            mazeDisplayer.solutionPath = viewModel.getSolution();
            mazeDisplayer.setCharacterPosition(viewModel.getCharacterRowIndex(), viewModel.getCharacterColumnIndex());
            this.characterRow.set(viewModel.getCharacterRowIndex() + "");
            this.characterColumn.set(viewModel.getCharacterColumnIndex() + "");

            if (viewModel.getCharacterRowIndex() == viewModel.getGoalRow() && viewModel.getCharacterColumnIndex() == viewModel.getGoalColumn()) {
                finishedTime = System.currentTimeMillis();
                timeLabel.setText((finishedTime-startedTime)/1000 + "");
                if(player != null)
                    player.pause();
                playSong("resources/Music/WinningSong.mp3");
                saveButton.setDisable(true);
                displayAlert("You Won!", "Way to go! You escaped from the maze and left Gargamel stuck behind.\n" +
                        "The mission took you " + (finishedTime-startedTime)/1000 + " seconds.");
            }
        }
    }


    public String getCharacterRow() {
        return characterRow.get();
    }

    public StringProperty characterRowProperty() {
        return characterRow;
    }

    public String getCharacterColumn() {
        return characterColumn.get();
    }

    public StringProperty characterColumnProperty() {
        return characterColumn;
    }


    public void setNumOfHints(int numOfHints) {
        this.numOfHints = numOfHints;
        hintsLabel.setText(numOfHints + "");
        mazeDisplayer.numOfHints = numOfHints;
        hintButton.setDisable(false);
    }


    public void solveMaze(){
        setNumOfHints(0);
        if(!solutionDisplayed){
            displayAlert("Solution Request","calling Papa Smurf to get you out of the maze");
            solveMazeButton.setSelected(true);
            solutionDisplayed = true;
            hintButton.setDisable(true);
            mazeDisplayer.solutionPath = viewModel.getSolution();
        }
        else{
            displayAlert("Solution Request","Papa Smurf went back to the village");
            solutionDisplayed = false;
            hintButton.setDisable(false);
            solveMazeButton.setSelected(false);
        }
        viewModel.solveMaze();
    }


    public void help(){
        helpButton.setDisable(true);
        playSong("resources/Music/smurfSongAlert.mp3");
        displayAlert("Help","In this game, you are playing the group of smurfs,\n" +
                "who are trying to get out of the maze and escape from Gargamel.\n" +
                "Your mission is to move them inside the maze towards the village.\n\n" +
                "Use the arrow keys or the numpad to move the smurfs. Diagonal movement is possible, but you cannot move through the walls in any case.");
    }

    public void keyPress(KeyEvent press){
        solutionDisplayed = false;
        setNumOfHints(0);
        hintButton.setDisable(false);
        viewModel.moveCharacter(press.getCode());
        press.consume();
    }

    public void mouseClick(MouseEvent click){
        mazeDisplayer.requestFocus();
    }

    public void algorithmsInfo(){
        displayAlert("About The Algorithms",
                "This game is built on two kinds of algorithms:\n" +
                        "A maze generating algorithm and a maze solving algorithm.\n"
                        + "The maze generating algorithm implements DFS algorithm, and the maze solving algorithm implements Best First Search algorithm.");
    }

    public void programmersInfo(){
        displayAlert("About The Programmers",
                "The programmers, Naor Ben Evgi and Roy Judes,\nare second year students in ISE department in Ben Gurion University.");
    }

    private void displayAlert(String title, String alert){
        Alert displayedAlert = new Alert(Alert.AlertType.INFORMATION);
        displayedAlert.setTitle(title);
        displayedAlert.setContentText(alert);
        displayedAlert.setHeaderText("");
        DialogPane dialogPane = displayedAlert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("alert.css").toExternalForm());
        dialogPane.getStyleClass().add("alert");
        if(title.equals("Help")) {
            displayedAlert.setOnCloseRequest(event -> {
                player.pause();
                if(started) {
                    playSong("resources/music/EntranceOriginalSmurfSong.mp3");
                    player.setVolume(0.2);
                    player.play();
                    volumeButton.setSelected(false);
                }
                helpButton.setDisable(false);
            });
        }

        displayedAlert.show();
    }

    public void playSong(String songPath){
        if(player != null){
            player.pause();
        }
        songPlayed = true;
        volumeButton.setDisable(false);
        volumeButton.setSelected(false);
        song = new Media(new File(songPath).toURI().toString());
        player = new MediaPlayer(song);
        player.setVolume(0.4);
        player.play();
    }

    public void pauseMusic(){
        if(songPlayed){
            player.pause();
            songPlayed = false;
        }
        else{
            player.play();
            songPlayed = true;
        }
    }

    public void showHint(){
        if(numOfHints == viewModel.getSolution().size() && numOfHints != 0){
            displayAlert("Maximum Hints Displayed", "You can't get anymore hints since the whole way to the village is displayed...");
            hintButton.setDisable(true);
            mazeDisplayer.requestFocus();
            return;
        }
        numOfHints++;
        hintsLabel.setText(numOfHints + "");
        mazeDisplayer.numOfHints++;
        viewModel.solveMaze();
    }

    public void zoom(ScrollEvent scroll){
        if(scroll.isControlDown()){
            mazeDisplayer.setHeight(mazeDisplayer.getHeight() + scroll.getDeltaY());
            mazeDisplayer.setWidth(mazeDisplayer.getWidth() + scroll.getDeltaY());
            scroll.consume();
        }
    }


    public void saveMaze(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save maze");
        File savedMaze = new File("./savedMazes");
        chooser.setInitialDirectory(savedMaze);
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("compressed maze files",".txt"));

        //opens the file saving window
        File compressedMaze = chooser.showSaveDialog((Stage)mazeDisplayer.getScene().getWindow());
        if(compressedMaze != null){
            viewModel.saveMaze(compressedMaze);
        }
    }


    public void loadMaze(){
        songPlayed = true;
        solutionDisplayed = false;
        volumeButton.setSelected(false);
        player.play();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load maze");
        File savedMaze = new File("./savedMazes");
        chooser.setInitialDirectory(savedMaze);
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("compressed maze files",".txt"));

        //opens the file saving window
        File compressedMaze = chooser.showOpenDialog((Stage)mazeDisplayer.getScene().getWindow());
        if(compressedMaze != null){
            viewModel.loadMaze(compressedMaze);
            playSong("resources/music/EntranceOriginalSmurfSong.mp3");
            solveMazeButton.setSelected(false);
            hintButton.setDisable(false);
        }
    }

    public void exitGame(){
        viewModel.exitGame();
        Platform.exit();
        if(player != null)
            player.stop();
    }


    public void displayProperties(){
        String title = "Game Properties";
        String content,line;
        String[] properties = new String[3];
        try{
            BufferedReader br = new BufferedReader(new FileReader("resources/config.properties"));
            line = br.readLine();
            while(line != null){
                String[] splitLine = line.split("=");
                if(splitLine[0].equals("generatorType")){
                    properties[0] = splitLine[1];
                }
                else if(splitLine[0].equals("searchAlgorithm")){
                    properties[1] = splitLine[1];
                }
                else if(splitLine[0].equals("numberOfThreads")){
                    properties[2] = splitLine[1];
                }
                line = br.readLine();
            }
            br.close();
            content = "The maze generating algorithm is " + properties[0] + ",\n" +
                    "the maze solving algorithm is " + properties[1] + ",\n" +
                    "and the amount of threads that run the game is " + properties[2];
            displayAlert(title,content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setResizeEvent(Scene scene) {
        long width = 0;
        long height = 0;
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                borderPane.setPrefWidth(newSceneWidth.longValue()*0.9);
                mazeDisplayer.setWidth(borderPane.getWidth());
                mazeDisplayer.drawMaze();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                borderPane.setPrefHeight(newSceneHeight.longValue()*0.9);
                mazeDisplayer.setHeight(borderPane.getHeight());
                mazeDisplayer.drawMaze();
            }
        });
    }




    /*public void setRows(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER){
            if(Integer.valueOf(rowsTextField.getText()) < 10){
                displayAlert();
            }
        }
    }

    public void setColumns(KeyEvent event){

    }*/
}
