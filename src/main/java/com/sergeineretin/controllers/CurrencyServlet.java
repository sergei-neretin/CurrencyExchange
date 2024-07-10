package com.sergeineretin.controllers;
import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.Utils;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.services.CurrencyService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyServlet extends HttpServlet {
    CurrencyService service;
    public CurrencyServlet() {
        service = new CurrencyService();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String code = getFormFields(req);
            CurrencyDto currency = service.findByName(code);
            Utils.write(resp, currency);
        } catch (CurrencyException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DatabaseUnavailableException e) {
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
