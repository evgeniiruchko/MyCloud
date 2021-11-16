package ru.geekbrains.cloud.common;

public class UploadRequest extends Commands{
    private String fileName;

    public String getPath() {
        return fileName;
    }

    public UploadRequest(String path) {
        this.fileName = path;
        this.type = CommandType.UPLOAD_REQUEST;
    }
}
