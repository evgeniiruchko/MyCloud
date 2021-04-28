package ru.geekbrains.cloud.client;

import ru.geekbrains.cloud.common.Commands;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private static Socket socket;
    private final String ADDRESS;
    private final int PORT;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;

    public Client(String address, int port) {
        ADDRESS = address;
        PORT = port;
    }

    void start() {
        try{
            socket = new Socket(this.ADDRESS, this.PORT);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void stop() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean checkConnection() {
        return !socket.isClosed() && socket != null;
    }

    static boolean sendCommand (Commands msg) {
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    static Commands readObject() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        return (Commands) obj;
    }
}
