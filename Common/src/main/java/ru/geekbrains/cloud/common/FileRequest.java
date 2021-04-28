package ru.geekbrains.cloud.common;

public class FileRequest extends Commands{
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public FileRequest(String fileName) {
        this.fileName = fileName;
        this.type = CommandType.FILE_REQUEST;
    }
}
