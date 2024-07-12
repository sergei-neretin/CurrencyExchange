package com.sergeineretin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.Writer;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeRateDto;
import com.sergeineretin.services.ExchangeRateService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;
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
            String[] codes = getFormFields(req);
            ExchangeRateDto exchangeRate = service.findByName(codes[0], codes[1]);
            writer.write(resp, exchangeRate);
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DatabaseUnavailableException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String rateString = getParameterMap(req).get("rate");
            BigDecimal rate = new BigDecimal(rateString);
            String[] codes = getFormFields(req);

            CurrencyDto baseCurrency = CurrencyDto.builder().code(codes[0]).build();
            CurrencyDto targetCurrency = CurrencyDto.builder().code(codes[1]).build();
            ExchangeRateDto exchangeRate = ExchangeRateDto.builder()
                    .baseCurrency(baseCurrency)
                    .targetCurrency(targetCurrency)
                    .rate(rate)
                    .build();

            ExchangeRateDto result  = service.update(exchangeRate);
            writer.write(resp, result);

        } catch (DatabaseUnavailableException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private String[] getFormFields(HttpServletRequest req) {
        String codesString = req.getPathInfo().substring(1);
        Pattern pattern = Pattern.compile("^[A-Z]{6}$");
        Matcher matcher = pattern.matcher(codesString);
        if (matcher.find()) {
            String baseCode = codesString.substring(0, 3);
            String targetCode = codesString.substring(3);
            return new String[] { baseCode, targetCode };
        } else {
            throw new RuntimeException("invalid form fields");
        }
    }
    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        BufferedReader br = null;
        Map<String, String> dataMap = null;
        try {
            InputStreamReader reader = new InputStreamReader(request.getInputStream());
            br = new BufferedReader(reader);
            String data = br.readLine();
            dataMap = Splitter.on('&')
                    .trimResults()
                    .withKeyValueSeparator(
                            Splitter.on('=')
                                    .limit(2)
                                    .trimResults())
                    .split(data);
            return dataMap;
        } catch (IOException ex) {
            log.error(ex.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }
            }
        }
        return dataMap;
    }
}
