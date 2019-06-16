package Model;

import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import Server.Server;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.scene.input.KeyCode;
import Client.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;

public class MyModel extends Observable implements IModel {

    public Maze maze;
    private ArrayList<Position> solutionPath;
    private int rowIndex;
    private int colIndex;
    private Server mazeGenerationServer;
    private Server mazeSolvingServer;
    private ExecutorService threadPool = Executors.newCachedThreadPool();


    /**
     * constructor
     */
    public MyModel(){
        mazeGenerationServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        mazeSolvingServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        solutionPath = new ArrayList<Position>();
        maze = new Maze(0,0);
    }

    /**
     * Starts the maze generating and solving servers.
     */
    public void startServers(){
        mazeGenerationServer.start();
        mazeSolvingServer.start();
    }

    /**
     * Stops the maze generating and solving servers.
     */
    public void stopServers(){
        mazeGenerationServer.stop();
        mazeSolvingServer.stop();
    }

    /**
     * Sends given dimensions to the maze generating server.
     * @param rows the height of the maze
     * @param cols the width of the maze
     */
    @Override
    public void generateMaze(int rows, int cols) {
        threadPool.submit(() -> {
            mazeGeneratorClient(rows,cols);
            rowIndex = maze.getStartPosition().getRowIndex();
            colIndex = maze.getStartPosition().getColumnIndex();
            setChanged();
            notifyObservers();
        });
    }

    /**
     * Calls the maze solving server.
     */
    @Override
    public void solveMaze() {
        threadPool.submit(() -> {
            mazeSolverClient();
            setChanged();
            notifyObservers();
        });
    }

    /**
     * Calls the maze generating server from a client's request, and generates a maze.
     * @param rows the height of the maze
     * @param columns the width of the maze
     */
    private void mazeGeneratorClient (int rows, int columns){
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rows, columns};
                        toServer.writeObject(mazeDimensions); //send maze dimensions to server
                        toServer.flush();
                        byte[] compressedMaze = (byte[]) fromServer.readObject(); //read generated maze (compressed with MyCompressor) from server
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[rows*columns+12]; //allocating byte[] for the decompressed maze -
                        is.read(decompressedMaze); //Fill decompressedMaze with bytes
                        maze = new Maze(decompressedMaze);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calls the maze solving server from a client's request, and solves the maze.
     */
    private void mazeSolverClient(){
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        solutionPath = new ArrayList<Position>();
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        maze.setStart(rowIndex,colIndex);
                        toServer.writeObject(maze); //send maze to server
                        toServer.flush();
                        Solution mazeSolution = (Solution) fromServer.readObject(); //read generated maze (compressed with MyCompressor) from server
                        ArrayList<AState> mazeSolutionSteps = mazeSolution.getSolutionPath();
                        for (int i = 0; i < mazeSolutionSteps.size()-1; i++) {
                            AState astate = mazeSolutionSteps.get(i);
                            int row = ((MazeState)astate).getRow();
                            int col = ((MazeState)astate).getCol();
                            solutionPath.add(new Position(row,col));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter of the maze.
     * @return the maze
     */
    @Override
    public Maze getMaze() {
        return maze;
    }

    /**
     * Getters of the indexes of the character's position.
     * @return indexes of the character's position
     */
    @Override
    public int getCharacterPositionRow() {
        return rowIndex;
    }

    @Override
    public int getCharacterPositionColumn() {
        return colIndex;
    }

    /**
     * Moves the character in a given direction.
     * @param movement the pressed key that contains the direction of the movement
     */
    @Override
    public void moveCharacter(KeyCode movement) {
        switch (movement){
            //UP
            case UP:
                if(checkMovement(rowIndex-1,colIndex))
                    rowIndex--;
                break;

            case NUMPAD8:
                if(checkMovement(rowIndex-1,colIndex))
                    rowIndex--;
                break;

            //DOWN
            case DOWN:
                if(checkMovement(rowIndex+1,colIndex))
                    rowIndex++;
                break;

            case NUMPAD2:
                if(checkMovement(rowIndex+1,colIndex))
                    rowIndex++;
                break;

            //RIGHT
            case RIGHT:
                if(checkMovement(rowIndex,colIndex+1))
                    colIndex++;
                break;

            case NUMPAD6:
                if(checkMovement(rowIndex,colIndex+1))
                    colIndex++;
                break;

            //LEFT
            case LEFT:
                if(checkMovement(rowIndex,colIndex-1))
                    colIndex--;
                break;

            case NUMPAD4:
                if(checkMovement(rowIndex,colIndex-1))
                    colIndex--;
                break;

            //diagonals
            case NUMPAD7:
                if(checkMovement(rowIndex-1,colIndex-1)) {
                    colIndex--;
                    rowIndex--;
                }
                break;

            case NUMPAD9:
                if(checkMovement(rowIndex-1,colIndex+1)) {
                    colIndex++;
                    rowIndex--;
                }
                break;

            case NUMPAD1:
                if(checkMovement(rowIndex+1,colIndex-1)) {
                    colIndex--;
                    rowIndex++;
                }
                break;

            case NUMPAD3:
                if(checkMovement(rowIndex+1,colIndex+1)) {
                    colIndex++;
                    rowIndex++;
                }
                break;

            default:
                return;
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Checks if the position that the character wants to move to is legal (contains a wall or exceeds the dimension of the maze).
     * @param row the row index of the checked position
     * @param col the column index of the checked position
     * @return true if the character can move to the checked position in the maze, false otherwise
     */
    private boolean checkMovement(int row, int col){
        if(row >= maze.getRows() || col >= maze.getCols() || row < 0 || col < 0)
            return false;

        if(maze.getCellValue(row,col) == 1)
            return false;

        return true;
    }

    /**
     * Exits the game - shuts down the thread pool and stops the servers.
     */
    public void exitGame(){
        stopServers();
        threadPool.shutdown();
    }

    /**
     * Getters of the indexes of the position of the goal.
     * @return indexes of the position of the goal
     */
    public int getGoalRow(){
        return maze.getGoalPosition().getRowIndex();
    }

    public int getGoalColumn(){
        return maze.getGoalPosition().getColumnIndex();
    }

    /**
     * Getter of the maze's solution.
     * @return
     */
    public ArrayList<Position> getSolution(){
        return solutionPath;
    }

    /**
     * Saves a maze in a file.
     * @param compressedMaze the given file that contains the maze
     */
    @Override
    public void saveMaze(File compressedMaze) {
        try{
            FileOutputStream file = new FileOutputStream(compressedMaze);
            OutputStream output = new MyCompressorOutputStream(file);
            maze.setStart(rowIndex,colIndex);
            output.write(maze.toByteArray());
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a saves maze from a file.
     * @param compressedMaze the given file that contains the maze
     */
    @Override
    public void loadMaze(File compressedMaze) {
        byte[] compressed;
        int rows, cols;
        try{
            FileInputStream input = new FileInputStream("./savedMazes/" + compressedMaze.getName());
            rows = convertByteToInteger((byte)input.read(),(byte)input.read());
            cols = convertByteToInteger((byte)input.read(),(byte)input.read());
            input.close();
            input = new FileInputStream("./savedMazes/" + compressedMaze.getName());
            compressed = new byte[rows*cols+12];

            InputStream decompressor = new MyDecompressorInputStream(input);
            decompressor.read(compressed);
            decompressor.close();
            input.close();

            maze = new Maze(compressed);
            rowIndex = maze.getStartPosition().getRowIndex();
            colIndex = maze.getStartPosition().getColumnIndex();
            setChanged();
            notifyObservers();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Converts a number represented in two byte numbers into one integer.
     * @param mult the first byte
     * @param add the second byte
     * @return the value of the number as an integer
     */
    private int convertByteToInteger(byte mult, byte add) {
        int num;

        if(mult == 0 && add == 0)
            return 0;

        if (mult != 0 && add == 0) {
            num = mult*256;
        } else {
            if (mult == 0){
                if(add > 0)
                    num = add;
                else
                    num = add + 256;
            }
            else {
                if (add > 0)
                    num = mult * 256 + add;
                else
                    num = mult * 256 + add + 256;
            }
        }

        return num;
    }
}
