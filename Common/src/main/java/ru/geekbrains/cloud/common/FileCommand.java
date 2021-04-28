package ru.geekbrains.cloud.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileCommand extends Commands{
    public static final int PART_SIZE = 8096;
    private String fileName;
    private byte[] data;
    private int countParts;
    private int numberPart;

    public String getPath() {
        return fileName;
    }

    public String getRootPath() {
        Path path = Paths.get(this.fileName);
        path = path.subpath(2, path.getNameCount()); //получаем путь к файлу
        return path.toString();
    }

    public byte[] getData() {
        return data;
    }

    public int getNumberPart() {
        return numberPart;
    }

    public int getCountParts() {
        return countParts;
    }

    public void setData(byte[] data) {
        this.data = data;
        numberPart++;
    }

    public FileCommand(Path fileName) throws IOException {
        this.fileName = fileName.toString();
        data = new byte[PART_SIZE];
        numberPart = 0;
        countParts = (int) (Files.size(fileName) / PART_SIZE) + 1;
        type = CommandType.FILE;
    }
}
