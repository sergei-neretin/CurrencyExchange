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
import java.util.List;

public class CurrenciesServlet extends HttpServlet {
    private final CurrencyService service = new CurrencyService();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyDto> currencies = service.findAll();
            Utils.write(resp, currencies);
        } catch (DatabaseUnavailableException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        if (name != null && code != null && sign != null) {
            try {
                CurrencyDto currencyDto = CurrencyDto.builder()
                        .fullName(name)
                        .code(code)
                        .sign(sign)
                        .build();
                currencyDto = service.create(currencyDto);
                Utils.write(resp, currencyDto);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } catch (CurrencyException e) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
            } catch (DatabaseUnavailableException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required form field is missing");
        }
    }
}
