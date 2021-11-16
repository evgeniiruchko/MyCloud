package ru.geekbrains.cloud.common;

public class DeleteRequest extends Commands{
    private String fileName;

    public String getFilename() {
        return fileName;
    }

    public DeleteRequest(String filename) {
        this.fileName = filename;
        this.type = CommandType.DELETE_REQUEST;
    }
}
