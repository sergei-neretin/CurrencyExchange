package com.sergeineretin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeineretin.C3p0DataSource;
import com.sergeineretin.dao.CurrencyDao;
import com.sergeineretin.dao.ExchangeRateDao;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        CurrencyDao currencyDao = new CurrencyDao();
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
        context.setAttribute("currencyDao", currencyDao);
        context.setAttribute("exchangeRateDao", exchangeRateDao);

        ObjectMapper mapper = new ObjectMapper();
        context.setAttribute("mapper", mapper);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        C3p0DataSource.close();
    }
}
