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

public class CurrencyServlet extends HttpServlet {
    CurrencyDao currencyDao = new CurrencyDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            try {
                String code = pathInfo.substring(1);
                CurrencyDto currency = currencyDao.findByName(code);
                ObjectMapper objectMapper = new ObjectMapper();
                String result = objectMapper.writeValueAsString(currency);
                PrintWriter writer = resp.getWriter();
                writer.println(result);
            } catch (CurrencyException e) {
                resp.sendError(404, e.getMessage());
            } catch (DatabaseUnavailableException e) {
                resp.sendError(500, e.getMessage());
            }
        } else {
            resp.sendError(400, "Currency code is missing from the address");
        }
    }


}
