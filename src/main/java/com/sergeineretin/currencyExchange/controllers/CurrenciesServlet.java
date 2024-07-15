package com.sergeineretin.currencyExchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.currencyExchange.Writer;
import com.sergeineretin.currencyExchange.dao.CurrencyDao;
import com.sergeineretin.currencyExchange.dto.CurrencyDto;
import com.sergeineretin.currencyExchange.exceptions.CurrencyException;
import com.sergeineretin.currencyExchange.exceptions.DatabaseException;
import com.sergeineretin.currencyExchange.services.CurrencyService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyService service;
    private Writer writer;
    @Override
    public void init(ServletConfig config) throws ServletException {
        CurrencyDao currencyDao = (CurrencyDao) config.getServletContext().getAttribute("currencyDao");
        service = new CurrencyService(currencyDao);
        ObjectMapper mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
        writer = new Writer(mapper);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyDto> currencies = service.findAll();
            writer.write(resp, currencies);
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CurrencyDto currencyDto = getCurrencyDto(req);
            currencyDto = service.create(currencyDto);
            writer.write(resp, currencyDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (CurrencyException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private CurrencyDto getCurrencyDto(HttpServletRequest req) {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        if (ServletUtils.isStringValid(name) && ServletUtils.validateCode(code) && ServletUtils.isStringValid(sign)) {
            return CurrencyDto.builder()
                    .name(name)
                    .code(code)
                    .sign(sign)
                    .build();
        } else {
            throw new IllegalArgumentException("invalid form fields");
        }
    }
}
