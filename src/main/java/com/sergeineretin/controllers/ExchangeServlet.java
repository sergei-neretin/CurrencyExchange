package com.sergeineretin.controllers;

import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.Utils;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeDto;
import com.sergeineretin.services.ExchangeService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.sql.SQLException;

public class ExchangeServlet extends HttpServlet {
    ExchangeService service;
    public ExchangeServlet() {
        service = new ExchangeService();
    }
    @SneakyThrows
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        BigDecimal amount = new BigDecimal(req.getParameter("amount"));

        System.out.println("[pweklf3orkgwe");
        CurrencyDto baseCurrency = CurrencyDto.builder().code(baseCurrencyCode).build();
        CurrencyDto targetCurrency = CurrencyDto.builder().code(targetCurrencyCode).build();
        ExchangeDto exchange = ExchangeDto.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .amount(amount)
                .build();

        try {
            ExchangeDto result = service.get(exchange);
            Utils.write(resp, result);
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
