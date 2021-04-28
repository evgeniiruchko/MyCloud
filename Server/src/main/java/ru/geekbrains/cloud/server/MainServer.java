package ru.geekbrains.cloud.server;

public class MainServer {
    public static void main(String[] args) throws InterruptedException {
        new Server(9000).start();
    }
}
