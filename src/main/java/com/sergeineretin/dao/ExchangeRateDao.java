package com.sergeineretin.dao;

import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.Utils;
import com.sergeineretin.model.ExchangeRate;
import com.sergeineretin.services.ExchangeRateService;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class ExchangeRateDao {
    public ExchangeRateDao() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public List<ExchangeRate> findAll() {
        try (Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT er.ID, er.Rate, c.ID AS BaseID, c.FullName AS BaseFullName, c.Code AS BaseCode, c.Sign AS BaseSign, c2.*,\n" +
                    "c2.ID AS TargetID, c2.FullName AS TargetFullName, c2.Code AS TargetCode, c2.Sign AS TargetSign\n" +
                    "from ExchangeRates er \n" +
                    "INNER JOIN Currencies c ON er.BaseCurrencyId = c.ID \n" +
                    "INNER JOIN Currencies c2 ON er.TargetCurrencyId = c2.ID;";
            ResultSet rs = stmt.executeQuery(sql);
            List<ExchangeRate> result = new LinkedList<>();
            while (rs.next()){
                ExchangeRate exchangeRate = ExchangeRateService.convertResultSet(rs);
                result.add(exchangeRate);
            }
            rs.close();
            return result;
        } catch (Exception e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public ExchangeRate findByName(ExchangeRate exchangeRate){
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();

            String sql = "SELECT \n" +
                    "    er.ID, \n" +
                    "    er.Rate, \n" +
                    "    c.ID AS BaseID, \n" +
                    "    c.FullName AS BaseFullName, \n" +
                    "    c.Code AS BaseCode, \n" +
                    "    c.Sign AS BaseSign, \n" +
                    "    c2.ID AS TargetID, \n" +
                    "    c2.FullName AS TargetFullName, \n" +
                    "    c2.Code AS TargetCode, \n" +
                    "    c2.Sign AS TargetSign\n" +
                    "FROM \n" +
                    "    ExchangeRates er\n" +
                    "INNER JOIN \n" +
                    "    Currencies c ON er.BaseCurrencyID = c.ID AND c.Code = '" + exchangeRate.getBaseCurrency().getCode() +"'\n" +
                    "INNER JOIN \n" +
                    "    Currencies c2 ON er.TargetCurrencyID = c2.ID AND c2.Code = '" + exchangeRate.getTargetCurrency().getCode() + "';\n";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return ExchangeRateService.convertResultSet(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public void create(ExchangeRate exchangeRate) {
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "INSERT OR ABORT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)\n" +
                    "VALUES ((SELECT ID FROM Currencies WHERE Code = '"+ exchangeRate.getBaseCurrency().getCode()+"'), \n" +
                    "        (SELECT ID FROM Currencies WHERE Code = '"+ exchangeRate.getTargetCurrency().getCode()+"'), \n" +
                    "        0.99);";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw new ExchangeRateException("A currency pair with this code already exists", new Throwable());
            } else if(e.getMessage().contains("SQLITE_CONSTRAINT_TRIGGER")) {
                throw new CurrencyException("One (or both) currencies from a currency pair do not exist in the database", new Throwable());
            } else {
                throw new DatabaseUnavailableException("Database is unavailable", e);
            }
        }
    }

    public void update(ExchangeRate exchangeRateDto){
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE ExchangeRates SET Rate = " + exchangeRateDto.getRate() + " WHERE\n" +
                    "BaseCurrencyId = (SELECT ID from Currencies c WHERE c.Code = '" + exchangeRateDto.getBaseCurrency().getCode() + "')\n" +
                    "AND TargetCurrencyId = (SELECT ID from Currencies c2 WHERE c2.Code = '" + exchangeRateDto.getTargetCurrency().getCode() + "');");
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }
}
