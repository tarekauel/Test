package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

public class Main extends Application {
	
	

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
	
		// FXML-Datei laden
		Parent root = FXMLLoader.load(getClass().getResource("test.fxml"));
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(this.getClass()
				.getResource("application.css").toExternalForm());

		
		primaryStage.setTitle("Beispielanwendung");
		
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		
		primaryStage.show();
		
	}
	

}
