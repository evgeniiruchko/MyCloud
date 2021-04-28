package ru.geekbrains.cloud.server;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static Statement statement;
    private static Logger logger = LogManager.getLogger();

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:Server/src/main/resources/database.db");
            statement = connection.createStatement();
            logger.info("успешное подключение к базе данных");
        } catch (Exception e) {
            logger.error("Ошибка подключения к базе данных");
            e.printStackTrace();
        }
    }


    public static void disconnect() {
        try {
            connection.close();
            logger.info("Успешное отключение от базы данных");
        } catch (SQLException e) {
            logger.error("ошибка отключения от базы данных");
            e.printStackTrace();
        }
    }

    public static int getIdByLoginAndPassword(String login, String encryptedPassword) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM users WHERE login = ? AND password = ?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, encryptedPassword);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.warn("Неудачная попытка авторизации пользователя " + login);
        return -1;
    }

    public static boolean tryToRegister(String login, String password) {
        try {
            statement.executeUpdate("INSERT INTO users (login, password) VALUES ('" + login + "','" + DigestUtils.md5Hex(password) + "')");
            logger.info("Пользователь " + login + " успешно зарегистрирован");
            return true;
        } catch (SQLException e) {
            logger.warn("Неудачная попытка регимтрации " + login);
            return false;
        }
    }
    //переделать когда дойду до реализации вызова метода
    public static boolean deleteFile(int userId, int elementId, String type){
        try {
            String query;
            if (type.equals("FILE")) {
                query = "DELETE FROM files WHERE ? AND ?;";
            } else if (type.equals("CATALOG")){
                query = "DELETE FROM files WHERE ? AND ?;";
            } else {
                logger.error("Не правильно указан параметр для удаления");
                return false;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, elementId);
            ResultSet result = preparedStatement.executeQuery();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

}
