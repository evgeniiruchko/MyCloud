package ru.geekbrains.cloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.geekbrains.cloud.common.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private int id;
    private final static String DEFAULT_CLOUD_FOLDER = "srv/cloudStorage/";
    private final String CLIENT_FOLDER_PREFIX = id + "/";
    private byte[] data = new byte[FileCommand.PART_SIZE];

    public ClientHandler(int id) {
        this.id = id;
    }

    //Читаем сообщение
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        try {
            Commands command;
            if (msg == null) return;
            handleMessage(ctx, (Commands) msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handleMessage(ChannelHandlerContext ctx, Commands command) throws IOException {
        if (command.isType(Commands.CommandType.FILE_REQUEST)) {
            FileRequest fileRequest = (FileRequest) command;
            Path path = Paths.get(fileRequest.getFileName());
            FileCommand fileCommand = new FileCommand(path);
            sendFile(ctx, fileCommand, path);
        } else if (command.isType(Commands.CommandType.UPLOAD_REQUEST)) {
            UploadRequest uploadRequest = (UploadRequest) command;
            ctx.writeAndFlush(new AcceptCommand(uploadRequest.getPath()));
        } else if (command.isType(Commands.CommandType.FILE)) {
            FileCommand fileCommand = (FileCommand) command;
            receiveFile(fileCommand);
        } else if (command.isType(Commands.CommandType.DELETE_REQUEST)) {
            DeleteRequest deleteRequest = (DeleteRequest) command;
            deleteFile(deleteRequest);
        }
    }

    private void deleteFile(DeleteRequest deleteRequest) throws IOException {
        Path path = Paths.get(deleteRequest.getFilename());
        if (Files.exists(path)) {
            Files.delete(path);
            System.out.println("файл удалён");
        }
    }

    private void receiveFile(FileCommand fileCommand) throws IOException {
        Path path = Paths.get(DEFAULT_CLOUD_FOLDER + CLIENT_FOLDER_PREFIX + fileCommand.getRootPath());
        Path folderForFile = path.subpath(0, path.getNameCount() - 1);
        if (!Files.exists(folderForFile))
            Files.createDirectories(folderForFile);
        if (Files.isDirectory(path)) {
            Files.createDirectories(path);
        }
        else {
            if (fileCommand.getNumberPart() == 1)
                Files.write(path, fileCommand.getData(), StandardOpenOption.CREATE);
            else
                Files.write(path, fileCommand.getData(), StandardOpenOption.APPEND);
        }

        if (fileCommand.getNumberPart() == fileCommand.getCountParts())
            System.out.println(fileCommand.getPath() + " упешно закачен");
    }

    private void sendFile(ChannelHandlerContext ctx, FileCommand fileCommand, Path path) throws IOException {
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                /* https://habr.com/ru/post/437694/
                   Метод walkFileTree() позволяет обойти дерево файлов и поддиректорий передаваемого ему в качестве параметра элемента Path… */
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
                        sendFile(ctx, fileCommand, file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                InputStream inputStream = Files.newInputStream(path);
                int size;
                while (inputStream.available() > 0) {
                    size = inputStream.read(data);
                    if (size == FileCommand.PART_SIZE) {
                        fileCommand.setData(data);
                        ctx.writeAndFlush(fileCommand);
                    } else {
                        byte[] lastData = new byte[size];
                        System.arraycopy(data, 0, lastData, 0, size);
                        fileCommand.setData(lastData);
                        ctx.writeAndFlush(fileCommand);
                    }
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
