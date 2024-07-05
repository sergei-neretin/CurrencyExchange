package com.sergeineretin;

public class DatabaseUnavailableException extends RuntimeException {
    public DatabaseUnavailableException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
