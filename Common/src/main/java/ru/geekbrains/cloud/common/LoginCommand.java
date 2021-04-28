package ru.geekbrains.cloud.common;

public class LoginCommand extends Commands {
    private  String login;
    private  String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public LoginCommand(String login, String password) {
        this.login = login;
        this.password = password;
        this.type = CommandType.LOGIN;
    }
}
