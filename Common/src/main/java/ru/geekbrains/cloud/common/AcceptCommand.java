package ru.geekbrains.cloud.common;

public class AcceptCommand extends Commands{
    private String path;

    public String getPath() {
        return path;
    }

    public AcceptCommand(String pathString) {
        this.path = pathString;
        this.type = CommandType.ACCEPT;
    }
}
