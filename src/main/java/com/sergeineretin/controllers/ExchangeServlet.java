package com.sergeineretin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.exceptions.ExchangeRateException;
import com.sergeineretin.Writer;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeDto;
import com.sergeineretin.services.ExchangeService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    ExchangeService service;
    private Writer writer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ExchangeRateDao exchangeRateDao = (ExchangeRateDao) config.getServletContext().getAttribute("exchangeRateDao");
        service = new ExchangeService(exchangeRateDao);

        ObjectMapper mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
        writer = new Writer(mapper);
    }
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
            ExchangeDto result = service.get(exchange);
            writer.write(resp, result);
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
