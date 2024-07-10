package com.sergeineretin.controllers;

import com.google.common.base.Splitter;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.Utils;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeRateDto;
import com.sergeineretin.services.ExchangeRateService;
import jakarta.servlet.ServletException;
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
public class ExchangeRateServlet extends HttpServlet {
    ExchangeRateService service;
    public ExchangeRateServlet() {
        this.service = new ExchangeRateService();
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
            Utils.write(resp, exchangeRate);
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
            Utils.write(resp, result);

        } catch (DatabaseUnavailableException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (ExchangeRateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required form field is missing");
        }
    }

    private String[] getFormFields(HttpServletRequest req) {
        String codesString = req.getPathInfo().substring(1);
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(codesString);
        int matches = 0;
        while (matcher.find()) {
            matches++;
        }
        if (matches == 6) {
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
            //log(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    //Logger.getLogger(Utils.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
        return dataMap;
    }
}
