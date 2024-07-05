package com.sergeineretin;

public class CurrencyException extends RuntimeException {
    public CurrencyException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
