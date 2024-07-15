package com.sergeineretin.currencyExchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.currencyExchange.Writer;
import com.sergeineretin.currencyExchange.dao.ExchangeRateDao;
import com.sergeineretin.currencyExchange.dto.CurrencyDto;
import com.sergeineretin.currencyExchange.dto.ExchangeRateDto;
import com.sergeineretin.currencyExchange.exceptions.CurrencyException;
import com.sergeineretin.currencyExchange.exceptions.DatabaseException;
import com.sergeineretin.currencyExchange.exceptions.ExchangeRateException;
import com.sergeineretin.currencyExchange.services.ExchangeRateService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    ExchangeRateService service;
    private Writer writer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ExchangeRateDao exchangeRateDao = (ExchangeRateDao) config.getServletContext().getAttribute("exchangeRateDao");
        service = new ExchangeRateService(exchangeRateDao);

        ObjectMapper mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
        writer = new Writer(mapper);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateDto> exchangeRates = service.findAll();
            writer.write(resp, exchangeRates);
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateString = req.getParameter("rate");

        if (baseCurrencyCode != null && targetCurrencyCode != null && rateString != null) {
            try {
                BigDecimal rate = new BigDecimal(rateString);
                CurrencyDto baseCurrency = CurrencyDto.builder().code(baseCurrencyCode).build();
                CurrencyDto targetCurrency = CurrencyDto.builder().code(targetCurrencyCode).build();
                ExchangeRateDto exchangeRateDto = ExchangeRateDto.builder()
                        .baseCurrency(baseCurrency)
                        .targetCurrency(targetCurrency)
                        .rate(rate)
                        .build();
                ExchangeRateDto exchangeRate  = service.create(exchangeRateDto);
                writer.write(resp, exchangeRate);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } catch (DatabaseException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (CurrencyException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (ExchangeRateException e) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required form field is missing");
        }
    }
}
