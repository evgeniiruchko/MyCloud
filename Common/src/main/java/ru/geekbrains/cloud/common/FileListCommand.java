package ru.geekbrains.cloud.common;

import java.nio.file.Path;
import java.util.Deque;

public class FileListCommand extends Commands{
    private Deque<Path> fileList;

    public Deque<Path> getFileList() {
        return fileList;
    }

    public FileListCommand(Deque<Path> fileList) {
        this.fileList = fileList;
        this.fileList.pollFirst();
        this.fileList.pollFirst();

        this.type = CommandType.FILE_LIST;
    }
}
