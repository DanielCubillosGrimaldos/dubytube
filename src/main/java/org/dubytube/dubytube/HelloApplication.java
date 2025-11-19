package org.dubytube.dubytube;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    public static Scene createScene(String fxmlName, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("/view/" + fxmlName)
        );
        Scene scene = new Scene(loader.load(), width, height);
        scene.getStylesheets().add(
                HelloApplication.class.getResource("/styles/app.css").toExternalForm()
        );
        return scene;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/view/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.getStylesheets().add(
                HelloApplication.class.getResource("/styles/app.css").toExternalForm()
        );
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }
}
