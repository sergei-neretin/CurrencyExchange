package com.sergeineretin.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
