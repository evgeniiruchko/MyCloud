package ru.geekbrains.cloud.common;

import java.io.Serializable;

public abstract class Commands implements Serializable {
    public enum CommandType {
        LOGIN,
        AUTH_OK,
        AUTH_FAIL,
        FILE,
        FILE_REQUEST,
        UPLOAD_REQUEST,
        ACCEPT,
        DECLINE,
        DELETE_REQUEST,
        FILE_LIST,
        FILE_LIST_REQUEST,
    }

    protected CommandType type;

    public CommandType getType() {
        return type;
    }

    public boolean isType (CommandType type) {
        return this.type.equals(type);
    }
}
