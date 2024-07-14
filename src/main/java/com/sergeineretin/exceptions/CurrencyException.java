package com.sergeineretin.exceptions;

public class CurrencyException extends RuntimeException {
    public CurrencyException(String errorMessage) {
        super(errorMessage);
    }
}
