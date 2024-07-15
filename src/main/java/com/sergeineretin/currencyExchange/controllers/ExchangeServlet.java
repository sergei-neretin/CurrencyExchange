package com.sergeineretin.currencyExchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.currencyExchange.Writer;
import com.sergeineretin.currencyExchange.dao.ExchangeRateDao;
import com.sergeineretin.currencyExchange.dto.CurrencyDto;
import com.sergeineretin.currencyExchange.dto.ExchangeDto;
import com.sergeineretin.currencyExchange.exceptions.ExchangeRateException;
import com.sergeineretin.currencyExchange.services.ExchangeService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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

    public static ExchangeDto getExchange(HttpServletRequest req) {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountString = req.getParameter("amount");
        if (ServletUtils.validateCode(baseCurrencyCode) && ServletUtils.validateCode(targetCurrencyCode) && ServletUtils.isValidBigDecimal(amountString)) {
            BigDecimal amount = new BigDecimal(amountString);
            CurrencyDto baseCurrency = CurrencyDto.builder().code(baseCurrencyCode).build();
            CurrencyDto targetCurrency = CurrencyDto.builder().code(targetCurrencyCode).build();
            return ExchangeDto.builder()
                    .baseCurrency(baseCurrency)
                    .targetCurrency(targetCurrency)
                    .amount(amount)
                    .build();
        } else {
            throw new IllegalArgumentException("invalid form fields");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ExchangeDto exchange = getExchange(req);
            ExchangeDto result = service.get(exchange);
            writer.write(resp, result);
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
