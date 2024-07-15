package com.sergeineretin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.Writer;
import com.sergeineretin.dao.CurrencyDao;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.exceptions.CurrencyException;
import com.sergeineretin.exceptions.DatabaseException;
import com.sergeineretin.services.CurrencyService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
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
            String code = getFormFields(req);
            CurrencyDto currency = service.findByCode(code);
            writer.write(resp, currency);
        } catch (CurrencyException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private String getFormFields(HttpServletRequest req) {
        String codeString = req.getPathInfo().substring(1);
        Pattern pattern = Pattern.compile("^[A-Z]{3}$");
        Matcher matcher = pattern.matcher(codeString);
        if (matcher.find()) {
            return codeString;
        } else {
            throw new RuntimeException("Currency code is invalid");
        }
    }
}
