package com.sergeineretin.exceptions;

public class ExchangeRateException extends RuntimeException {
    public ExchangeRateException(String errorMessage) {
        super(errorMessage);
    }
}
