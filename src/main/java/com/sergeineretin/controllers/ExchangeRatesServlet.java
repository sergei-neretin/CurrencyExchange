package com.sergeineretin.controllers;

import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.Utils;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeRateDto;
import com.sergeineretin.services.ExchangeRateService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService service;
    public ExchangeRatesServlet() {
        service = new ExchangeRateService();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateDto> exchangeRates = service.findAll();
            Utils.write(resp, exchangeRates);
        } catch (DatabaseUnavailableException e) {
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
                Utils.write(resp, exchangeRate);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } catch (DatabaseUnavailableException e) {
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
