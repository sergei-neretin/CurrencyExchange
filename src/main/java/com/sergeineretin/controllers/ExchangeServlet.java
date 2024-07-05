package com.sergeineretin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.dao.ExchangeDao;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeDto;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ExchangeServlet extends HttpServlet {
    ExchangeDao exchangeDao = new ExchangeDao();

    @SneakyThrows
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        BigDecimal amount = new BigDecimal(req.getParameter("amount"));

        CurrencyDto baseCurrency = CurrencyDto.builder().code(baseCurrencyCode).build();
        CurrencyDto targetCurrency = CurrencyDto.builder().code(targetCurrencyCode).build();

        ExchangeDto exchange = ExchangeDto.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .amount(amount)
                .build();

        try {
            ExchangeDto result = exchangeDao.read(exchange);
            ObjectMapper objectMapper = new ObjectMapper();
            String string = objectMapper.writeValueAsString(result);
            PrintWriter writer = resp.getWriter();
            writer.println(string);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
