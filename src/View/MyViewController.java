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

import java.io.*;
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
    public MenuItem highScores;
    public MenuItem programmersInfo;
    public MenuItem algorithmsInfo;
    public RadioButton volumeButton;
    public Label timeLabel;
    public static MediaPlayer player;
    public static Media song;
    private boolean songPlayed = true;
    private boolean solutionDisplayed = false;
    private int numOfHints = 0;
    private boolean started = false, finished = false;
    private long startedTime, finishedTime;

    /**
     * Binds the properties of the controller and the ViewModel
     * @param viewModel the matching ViewModel
     */
    public void bindProperties(MyViewModel viewModel){
        rowsLabel.textProperty().bind(viewModel.characterRow);
        colsLabel.textProperty().bind(viewModel.characterColumn);
    }

    /**
     * ViewModel setter
     * @param viewModel the given ViewModel
     */
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

    /**
     * Reads the text boxes in the application and generates a maze accordingly
     */
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
        finished = false;
        solutionDisplayed = false;
        solveMazeButton.setSelected(false);
        hintButton.setDisable(false);
        saveButton.setDisable(false);
        generateMazeButton.setDisable(true);
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

    /**
     * Sets the maze in the maze displayer class and shows it on the screen in the application.
     * In case the character has reached the goal, the music changes and the game ends.
     * @param maze the given maze
     */
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
                updateHighscore((int)((finishedTime-startedTime)/1000));
                finished = true;
                if(player != null)
                    player.pause();
                playSong("resources/Music/WinningSong.mp3");
                saveButton.setDisable(true);
                displayAlert("You Won!", "Way to go! You escaped from the maze and left Gargamel stuck behind.\n" +
                        "The mission took you " + (finishedTime-startedTime)/1000 + " seconds.");
                solveMazeButton.setDisable(true);
                hintButton.setDisable(true);
            }
        }
    }

    /**
     * Character position getters
     * @return the indexes of the character's position
     */
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

    /**
     * Sets the number of hints to the given value
     * @param numOfHints the given amount of hints
     */
    public void setNumOfHints(int numOfHints) {
        this.numOfHints = numOfHints;
        hintsLabel.setText(numOfHints + "");
        mazeDisplayer.numOfHints = numOfHints;
        hintButton.setDisable(false);
    }

    /**
     * Solves the maze and displays the solution in the application.
     */
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
        mazeDisplayer.requestFocus();
    }

    /**
     * Opens a window with description about the game and its rules.
     */
    public void help(){
        helpButton.setDisable(true);
        playSong("resources/Music/smurfSongAlert.mp3");
        displayAlert("Help","In this game, you are playing the group of smurfs,\n" +
                "who are trying to get out of the maze and escape from Gargamel.\n" +
                "Your mission is to move them inside the maze towards the village.\n\n" +
                "Use the arrow keys or the numpad to move the smurfs. Diagonal movement is possible, but you cannot move through the walls in any case.");
    }

    /**
     * Activates when a key is pressed, and moves the character in the maze if the pressed key is one of the arrows.
     * @param press the type of key pressed
     */
    public void keyPress(KeyEvent press){
        if(!finished) {
            solutionDisplayed = false;
            setNumOfHints(0);
            viewModel.moveCharacter(press.getCode());
            press.consume();
        }
    }

    /**
     * Sets the focus on the maze when the user clicks the mouse.
     * @param click the mouse click
     */
    public void mouseClick(MouseEvent click){
        mazeDisplayer.requestFocus();
    }

    /**
     * Opens a window with description about the algorithms that generate and solve the maze.
     */
    public void algorithmsInfo(){
        displayAlert("About The Algorithms",
                "This game is built on two kinds of algorithms:\n" +
                        "A maze generating algorithm and a maze solving algorithm.\n"
                        + "The maze generating algorithm implements DFS algorithm, and the maze solving algorithm implements Best First Search algorithm.");
    }

    /**
     * Opens a window with description about the amazing programmers, Roy Judes and Naor Ben Evgi.
     */
    public void programmersInfo(){
        displayAlert("About The Programmers",
                "The programmers, Naor Ben Evgi and Roy Judes,\nare second year students in ISE department in Ben Gurion University.");
    }

    /**
     * Opens a window with given title and text, in order to send a message to the user.
     * @param title the title of the message
     * @param alert the content of the message
     */
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

    /**
     * Plays a song in the background.
     * @param songPath the path of the song's location in the project
     */
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

    /**
     * Pauses the music when the mute button is pressed.
     */
    public void pauseMusic(){
        if(songPlayed){
            player.pause();
            songPlayed = false;
        }
        else{
            player.play();
            songPlayed = true;
        }
        mazeDisplayer.requestFocus();
    }

    /**
     * Draws a hint in the maze when the matching button in the application is pressed.
     */
    public void showHint(){
        if(numOfHints == viewModel.getSolution().size() && numOfHints != 0){
            displayAlert("Maximum Hints Displayed", "You can't get anymore hints since the whole way to the village is displayed...");
            hintButton.setDisable(true);
            mazeDisplayer.requestFocus();
            return;
        }
        setNumOfHints(numOfHints+1);
        viewModel.solveMaze();
    }

    /**
     * Zooms in/out according to the user's scroll.
     * @param scroll the scroll event
     */
    public void zoom(ScrollEvent scroll){
        if(scroll.isControlDown()){
            mazeDisplayer.setHeight(mazeDisplayer.getHeight() + scroll.getDeltaY());
            mazeDisplayer.setWidth(mazeDisplayer.getWidth() + scroll.getDeltaY());
            scroll.consume();
        }
    }

    /**
     * Saves the game as a text file.
     */
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

    /**
     * Loads a game from the folder of the saved games.
     */
    public void loadMaze(){
        setNumOfHints(0);
        finished = false;
        songPlayed = true;
        solutionDisplayed = false;
        volumeButton.setSelected(false);
        if(player != null)
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
            mazeDisplayer.requestFocus();
            startedTime = System.currentTimeMillis();
            timeLabel.setText("");
        }
    }

    /**
     * Exits the game and closes the application when the exit button is pressed.
     */
    public void exitGame(){
        viewModel.exitGame();
        Platform.exit();
        if(player != null)
            player.stop();
    }


    /**
     * Opens a window and describes the properties written in the configurations file.
     */
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

    /**
     * Redraws the maze in the application in a matching size to the changed size of the main window.
     * @param scene the window which the user resized
     */
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

    /**
     * Shows the current record of the game when the highscore button is pressed.
     */
    public void showHighscore(){
        try{
            BufferedReader br = new BufferedReader(new FileReader("highscore"));
            String line;
            int grade, time, rows, cols;
            if((line = br.readLine()) == null){
                grade = 0;
                time = 0;
                rows = 0;
                cols = 0;
            }
            else{
                String[] scores = new String[3];
                scores[0] = line.split(" ")[1];
                scores[1] = br.readLine().split(" ")[1];
                scores[2] = br.readLine().split(" ")[1];
                grade = Integer.valueOf(scores[0]);
                time = Integer.valueOf(scores[1]);
                rows = Integer.valueOf(scores[2].split("x")[0]);
                cols = Integer.valueOf(scores[2].split("x")[1]);
            }
            displayAlert("Highscore","Highscore:\n" +
                    "Grade: " + grade + "\n" +
                    "This grade was given after getting back to the village in " + time + " seconds\n" +
                    "in a " + rows + "x" + cols + " maze.");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Updates the highscore in the highscore file in case it broke the current record.
     * @param time the time (in seconds) that took the user to solve the maze
     */
    private void updateHighscore(int time){
        try {
            BufferedReader br = new BufferedReader(new FileReader("highscore"));
            BufferedWriter bw;
            String line;
            Maze temp = viewModel.getBoard();
            if((line = br.readLine()) == null){
                bw = new BufferedWriter(new FileWriter("highscore"));
                bw.flush();
                bw.write("Grade: 0\n");
                bw.flush();
                bw.write("Time: 0 seconds\n");
                bw.flush();
                bw.write("Dimensions: 0x0");
                bw.flush();
                bw.close();
            }
            else{
                int highestGrade = Integer.valueOf(line.split(" ")[1]);
                int checkedGrade = calculateGrade(time);
                if(checkedGrade > highestGrade){
                    bw = new BufferedWriter(new FileWriter("highscore"));
                    bw.flush();
                    bw.write("Grade: " + checkedGrade + "\n");
                    bw.flush();
                    bw.write("Time: " + time + " seconds\n");
                    bw.flush();
                    bw.write("Dimensions: " + temp.getRows() + "x" + temp.getCols());
                    bw.flush();
                    bw.close();
                }
            }
            br.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Calculates the grade of a finished game.
     * @param time the time (in seconds) that took the user to solve the maze
     * @return the calculated grade
     */
    private int calculateGrade(int time){
        Maze temp = viewModel.getBoard();
        int rows = temp.getRows(), cols = temp.getCols();
        if(time == 0)
            time++;
        return (rows*cols)*10/time;
    }

}
