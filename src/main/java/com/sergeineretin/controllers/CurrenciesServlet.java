package com.sergeineretin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.dao.CurrencyDao;
import com.sergeineretin.dto.CurrencyDto;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CurrenciesServlet extends HttpServlet {
    CurrencyDao currencyDao = new CurrencyDao();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyDto> currencies = currencyDao.findAll();
            ObjectMapper objectMapper = new ObjectMapper();
            String result = objectMapper.writeValueAsString(currencies);
            PrintWriter writer = resp.getWriter();
            writer.println(result);

        } catch (DatabaseUnavailableException e) {
            resp.sendError(500, e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        if (name == null || code == null || sign == null) {
            resp.sendError(400, "Required form field is missing");
        } else {
            try {
                CurrencyDto currencyDto = CurrencyDto.builder()
                        .fullName(name)
                        .code(code)
                        .sign(sign)
                        .build();

                CurrencyDto currency = currencyDao.create(currencyDto);
                ObjectMapper objectMapper = new ObjectMapper();
                String result = objectMapper.writeValueAsString(currency);
                PrintWriter writer = resp.getWriter();
                writer.println(result);
                resp.setStatus(201);
            } catch (CurrencyException e) {
                resp.sendError(409, e.getMessage());
            } catch (DatabaseUnavailableException e) {
                resp.sendError(500, e.getMessage());
            }
        }
    }
}
