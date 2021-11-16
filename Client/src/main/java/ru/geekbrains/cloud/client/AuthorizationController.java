package ru.geekbrains.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.geekbrains.cloud.common.Commands;
import ru.geekbrains.cloud.common.LoginCommand;


import java.io.IOException;
import java.util.Optional;

public class AuthorizationController {

    @FXML
    VBox window;
    @FXML
    TextField loginField, passField;

    public void login(ActionEvent actionEvent) {
        if (!Client.checkConnection()) {
            Client.start();
        }
        waitAuth();
        Client.sendCommand(new LoginCommand(loginField.getText(), passField.getText()));
        passField.setText("");
    }

    private void waitAuth() {
        new Thread(() -> {
           try {
               Commands command;
               while (true){
                   command = Client.readObject();
                   if (command != null) {
                       if (command.isType(Commands.CommandType.AUTH_FAIL)) {
                           showError("Ошибка авторизации", "Неверное имя пользователя или пароль").get();  //почему-то вываливается исключение, что открываю не в ом потоке
                       } else if (command.isType(Commands.CommandType.AUTH_OK)) {
                           openProgram();
                       }
                   }
               }
           } catch (IOException | ClassNotFoundException e) {
               e.printStackTrace();
           }
        }).start();
    }

    private void openProgram() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> {
                try {
                    Parent main = FXMLLoader.load(getClass().getResource("/Main.FXML"));
                    ((Stage) window.getScene().getWindow()).setScene(new Scene(main));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void registration(ActionEvent actionEvent) {
        openRegistration();
    }

    private void openRegistration() {
            Platform.runLater(() -> {
                try {
                    Parent main = FXMLLoader.load(getClass().getResource("/Registration.FXML"));
                    ((Stage) window.getScene().getWindow()).setScene(new Scene(main));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    public void exit(ActionEvent actionEvent) {
        if (askForExit("Выход","Вы действительно хотите выйти из программы?").get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    private Optional<ButtonType> askForExit(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(text);

        return alert.showAndWait();
    }

    private Optional<ButtonType> showError(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(header);
        alert.setContentText(text);
        return alert.showAndWait();
    }
}
