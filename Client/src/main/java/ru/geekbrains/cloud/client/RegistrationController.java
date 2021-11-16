package ru.geekbrains.cloud.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ru.geekbrains.cloud.common.LoginCommand;
import ru.geekbrains.cloud.common.RegistrationCommand;

public class RegistrationController {

    @FXML
    VBox regWindow;
    @FXML
    TextField regLoginField, regPassField, regPassFieldConfirm;

    public void Registration(ActionEvent actionEvent) {
        if (!Client.checkConnection()) {
            Client.start();
        }
        if (regPassField.getText().isEmpty() || regPassFieldConfirm.getText().isEmpty() || regLoginField.getText().isEmpty())
        {
            System.out.println("не все поля заполнены");
        } else if (regPassField.getText().equals(regPassFieldConfirm.getText())) {
            Client.sendCommand(new RegistrationCommand(regLoginField.getText(), regPassField.getText()));
        } else {
            System.out.println("Пароли не совпадают");
        }
    }
}
