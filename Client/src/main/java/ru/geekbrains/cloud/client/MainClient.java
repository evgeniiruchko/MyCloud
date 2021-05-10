package ru.geekbrains.cloud.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainClient extends Application {
    public static void main(String[] args) {
        new Client("localhost", 9000).start();
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
            Parent reg = FXMLLoader.load(getClass().getResource("/Authorization.fxml"));
            primaryStage.setTitle("Регистрация");
            primaryStage.setScene(new Scene(reg, 300, 300));
            primaryStage.show();
    }
}
