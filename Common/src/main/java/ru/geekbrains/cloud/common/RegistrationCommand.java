package ru.geekbrains.cloud.common;

public class RegistrationCommand extends Commands{

    private String password;

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    private String login;

    public RegistrationCommand(String login, String password) {
        this.login = login;
        this.password = password;
        this.type = CommandType.REGISTRATION;
    }
}
