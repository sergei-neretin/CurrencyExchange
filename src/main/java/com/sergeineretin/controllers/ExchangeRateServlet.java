package com.sergeineretin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeRateDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
public class ExchangeRateServlet extends HttpServlet {
    ExchangeRateDao exchangeRateDao = new ExchangeRateDao();

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
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            try {
                String string = pathInfo.substring(1);
                int mid = string.length() / 2;
                String[] codes = { string.substring(0, mid), string.substring(mid) };

                CurrencyDto baseCurrency = CurrencyDto.builder().code(codes[0]).build();
                CurrencyDto targetCurrency = CurrencyDto.builder().code(codes[1]).build();
                ExchangeRateDto exchangeRate = ExchangeRateDto.builder()
                        .baseCurrency(baseCurrency)
                        .targetCurrency(targetCurrency)
                        .build();
                exchangeRate = exchangeRateDao.read(exchangeRate);
                ObjectMapper objectMapper = new ObjectMapper();
                String result = objectMapper.writeValueAsString(exchangeRate);
                PrintWriter writer = resp.getWriter();
                writer.println(result);

            } catch (ExchangeRateException e) {
                resp.sendError(404, e.getMessage());
            }
        } else {
            resp.sendError(400, "Currency code is missing from the address");
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String rateString = getParameterMap(req).get("rate");

        if (rateString != null) {
            try {
                BigDecimal rate = new BigDecimal(rateString);
                String string = pathInfo.substring(1);
                int mid = string.length() / 2;
                String[] codes = { string.substring(0, mid), string.substring(mid) };
                CurrencyDto baseCurrency = CurrencyDto.builder().code(codes[0]).build();
                CurrencyDto targetCurrency = CurrencyDto.builder().code(codes[1]).build();
                ExchangeRateDto exchangeRate = ExchangeRateDto.builder()
                        .baseCurrency(baseCurrency)
                        .targetCurrency(targetCurrency)
                        .rate(rate)
                        .build();

                exchangeRate = exchangeRateDao.update(exchangeRate);
                ObjectMapper objectMapper = new ObjectMapper();
                String result = objectMapper.writeValueAsString(exchangeRate);
                PrintWriter writer = resp.getWriter();
                writer.println(result);

            } catch (DatabaseUnavailableException e) {
                resp.sendError(500, e.getMessage());
            } catch (ExchangeRateException e) {
                resp.sendError(404, e.getMessage());
            }
        } else {
            resp.sendError(400, "Required form field is missing");
        }
    }

    public static Map<String, String> getParameterMap(HttpServletRequest request) {

        BufferedReader br = null;
        Map<String, String> dataMap = null;

        try {

            InputStreamReader reader = new InputStreamReader(
                    request.getInputStream());
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
