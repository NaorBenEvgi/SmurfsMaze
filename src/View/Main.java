package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MyView.fxml"));
        Stage stage = new Stage();
        String moviePath = new File("resources/Movie/smurfsEnterMaze.mp4").getAbsolutePath();
        MediaPlayer player = new MediaPlayer(new Media(new File(moviePath).toURI().toString()));
        MediaView mediaView = new MediaView(player);
        DoubleProperty movieHeight = mediaView.fitHeightProperty();
        DoubleProperty movieWidth = mediaView.fitWidthProperty();
        movieHeight.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
        movieWidth.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        mediaView.setPreserveRatio(true);
        stage.setScene(new Scene(new Group(mediaView), 1500, 1500));
        player.setAutoPlay(true);
        stage.setFullScreen(true);
        stage.setTitle("The Smurfs enter the maze");
        stage.show();

        PauseTransition endMovie = new PauseTransition(Duration.seconds(17));
        endMovie.setOnFinished(e -> {stage.close();
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setFullScreen(true);
            MyModel model = new MyModel();
            model.startServers();
            MyViewModel viewModel = new MyViewModel(model);
            model.addObserver(viewModel);
            primaryStage.setTitle("Help the Smurfs escape from Gargamel and get back to the village!");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Scene scene = null;
            try {
                scene = new Scene(fxmlLoader.load(getClass().getResource("MyView.fxml").openStream()),1500,900);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            scene.getStylesheets().add(getClass().getResource("./StyleSheet.css").toExternalForm());
            primaryStage.setScene(scene);
            MyViewController view = fxmlLoader.getController();
            view.setResizeEvent(scene);
            view.setViewModel(viewModel);
            viewModel.addObserver(view);
            primaryStage.setOnCloseRequest(event -> view.exitGame());
            primaryStage.show();});
        endMovie.play();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
