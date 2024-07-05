package com.sergeineretin.dao;

import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.Utils;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeRateDto;
import com.sergeineretin.services.ExchangeRateService;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExchangeRateDao {

    private final CurrencyDao currencyDao;
    public ExchangeRateDao() {
        currencyDao = new CurrencyDao();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public List<ExchangeRateDto> read() {
        try (Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM ExchangeRates;";
            ResultSet rs = stmt.executeQuery(sql);
            List<ExchangeRateDto> result = new LinkedList<>();
            if (rs != null) {
                do {
                    ExchangeRateDto exchangeRateDto = ExchangeRateService.convertResultSet(rs);
                    result.add(exchangeRateDto);
                } while (rs.next());
                rs.close();
                return result;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public ExchangeRateDto read(ExchangeRateDto exchangeRateDto){
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();

            Statement stmtBase = conn.createStatement();
            Statement stmtTarget = conn.createStatement();
            ResultSet rsCurrencyBase = stmtBase.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeRateDto.getBaseCurrency().getCode() + "';");
            ResultSet rsCurrencyTarget = stmtTarget.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeRateDto.getTargetCurrency().getCode() + "';");

            int idBase = rsCurrencyBase.getInt("ID");
            int idTarget = rsCurrencyTarget.getInt("ID");
            ResultSet rs;
            if ((rs = stmt.executeQuery("SELECT * FROM ExchangeRates where BaseCurrencyID = " + idBase +
                    " and TargetCurrencyID = " + idTarget + ";")).next() && rsCurrencyBase.next() && rsCurrencyTarget.next()) {

                CurrencyDto base = CurrencyDto.builder()
                        .id(rsCurrencyBase.getLong("ID"))
                        .code(rsCurrencyBase.getString("Code"))
                        .fullName(rsCurrencyBase.getString("FullName"))
                        .sign(rsCurrencyBase.getString("Sign"))
                        .build();

                CurrencyDto target = CurrencyDto.builder()
                        .id(rsCurrencyTarget.getLong("ID"))
                        .code(rsCurrencyTarget.getString("Code"))
                        .fullName(rsCurrencyTarget.getString("FullName"))
                        .sign(rsCurrencyTarget.getString("Sign"))
                        .build();

                ExchangeRateDto build = ExchangeRateDto.builder()
                        .ID(rs.getInt("ID"))
                        .baseCurrency(base)
                        .targetCurrency(target)
                        .rate(rs.getBigDecimal("Rate"))
                        .build();
                rs.close();
                return build;

            } else {
                throw new ExchangeRateException("Exchange rate for the pair was not found", new Throwable());
            }
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public ExchangeRateDto create(ExchangeRateDto exchangeRateDto) {


        try {
            Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl());
            Statement stmt = conn.createStatement();
            Statement stmtBase = conn.createStatement();
            Statement stmtTarget = conn.createStatement();
            ResultSet rsCurrencyBase = stmtBase.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeRateDto.getBaseCurrency().getCode() + "';");
            ResultSet rsCurrencyTarget = stmtTarget.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeRateDto.getTargetCurrency().getCode() + "';");
            int idBase = rsCurrencyBase.getInt("ID");
            int idTarget = rsCurrencyTarget.getInt("ID");

            Statement stmtCheckIfExist = conn.createStatement();
            ResultSet rsCheckIfExist = stmtCheckIfExist.executeQuery("SELECT * FROM ExchangeRates where BaseCurrencyID = " + idBase +
                    " and TargetCurrencyID = " + idTarget + ";");
            if (!rsCurrencyBase.next() || !rsCurrencyTarget.next()) {
                throw new CurrencyException("One (or both) currencies from a currency pair do not exist in the database", new Throwable());
            }else if(rsCheckIfExist.next()) {
                throw new ExchangeRateException("A currency pair with this code already exists", new Throwable());
            } else {
                String sql = "INSERT INTO ExchangeRates(BaseCurrencyID, TargetCurrencyID, Rate) VALUES " +
                        "('" + idBase + "', '" + idTarget + "', " + exchangeRateDto.getRate() + ");";
                stmt.executeUpdate(sql);
                return read(exchangeRateDto);
            }

        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public ExchangeRateDto update(ExchangeRateDto exchangeRateDto){
        try {
            Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl());
            Statement stmt = conn.createStatement();
            Statement stmtBase = conn.createStatement();
            Statement stmtTarget = conn.createStatement();
            ResultSet rsCurrencyBase = stmtBase.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeRateDto.getBaseCurrency().getCode() + "';");
            ResultSet rsCurrencyTarget = stmtTarget.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeRateDto.getTargetCurrency().getCode() + "';");
            int idBase = rsCurrencyBase.getInt("ID");
            int idTarget = rsCurrencyTarget.getInt("ID");
            Statement stmtCheckIfExist = conn.createStatement();
            ResultSet rsCheckIfExist = stmtCheckIfExist.executeQuery("SELECT * FROM ExchangeRates where BaseCurrencyID = " + idBase +
                    " and TargetCurrencyID = " + idTarget + ";");

            if(!rsCurrencyBase.next() || !rsCurrencyTarget.next() || !rsCheckIfExist.next()) {
                throw new ExchangeRateException("Currency pair is absent in the database", new Throwable());
            } else {
                String sql = "UPDATE ExchangeRates SET Rate = " + exchangeRateDto.getRate() + " WHERE BaseCurrencyID = " + idBase +
                        " AND TargetCurrencyID = " + idTarget + ";";
                stmt.executeUpdate(sql);
                return read(exchangeRateDto);
            }
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }
}
