package com.sergeineretin.currencyExchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.currencyExchange.Writer;
import com.sergeineretin.currencyExchange.dao.ExchangeRateDao;
import com.sergeineretin.currencyExchange.dto.CurrencyDto;
import com.sergeineretin.currencyExchange.dto.ExchangeRateDto;
import com.sergeineretin.currencyExchange.exceptions.DatabaseException;
import com.sergeineretin.currencyExchange.exceptions.ExchangeRateException;
import com.sergeineretin.currencyExchange.services.ExchangeRateService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private ExchangeRateService service;
    private Writer writer;
    @Override
    public void init(ServletConfig config) throws ServletException {
        ExchangeRateDao exchangeRateDao = (ExchangeRateDao) config.getServletContext().getAttribute("exchangeRateDao");
        service = new ExchangeRateService(exchangeRateDao);

        ObjectMapper mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
        writer = new Writer(mapper);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] codes = getCodes(req);
            ExchangeRateDto exchangeRate = service.findByCodes(codes[0], codes[1]);
            writer.write(resp, exchangeRate);
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ExchangeRateDto exchangeRate = getExchangeRate(req);
            ExchangeRateDto result  = service.update(exchangeRate);
            writer.write(resp, result);
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private ExchangeRateDto getExchangeRate(HttpServletRequest req) {
        BigDecimal rate = getRate(req);
        String[] codes = getCodes(req);

        CurrencyDto baseCurrency = CurrencyDto.builder().code(codes[0]).build();
        CurrencyDto targetCurrency = CurrencyDto.builder().code(codes[1]).build();
        return ExchangeRateDto.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .rate(rate)
                .build();
    }

    private BigDecimal getRate(HttpServletRequest req) {
        String rateString = ServletUtils.getParameterMap(req).get("rate");
        if (ServletUtils.isValidBigDecimal(rateString)) {
            return new BigDecimal(rateString);
        } else {
            throw new IllegalArgumentException("invalid rate");
        }
    }

    private String[] getCodes(HttpServletRequest req) {
        String codesString = req.getPathInfo().substring(1);
        Pattern pattern = Pattern.compile(ServletUtils.CODES_REGEX);
        Matcher matcher = pattern.matcher(codesString);
        if (matcher.find()) {
            String baseCode = codesString.substring(0, 3);
            String targetCode = codesString.substring(3);
            return new String[] { baseCode, targetCode };
        } else {
            throw new IllegalArgumentException("invalid codes");
        }
    }
}
