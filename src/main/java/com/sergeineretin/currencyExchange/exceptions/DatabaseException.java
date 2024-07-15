package com.sergeineretin.currencyExchange.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String errorMessage) {
        super(errorMessage);
    }
}
