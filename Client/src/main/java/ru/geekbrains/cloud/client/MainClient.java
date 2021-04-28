package ru.geekbrains.cloud.client;

public class MainClient {
    public static void main(String[] args) {
        new Client("localhost", 9000).start();
    }
}
