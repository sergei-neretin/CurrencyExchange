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

public class CurrencyServlet extends HttpServlet {
    CurrencyService service;
    public CurrencyServlet() {
        service = new CurrencyService();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            try {
                String code = pathInfo.substring(1);
                CurrencyDto currency = service.findByName(code);
                Utils.write(resp, currency);
            } catch (CurrencyException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (DatabaseUnavailableException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency code is missing from the address");
        }
    }
}
