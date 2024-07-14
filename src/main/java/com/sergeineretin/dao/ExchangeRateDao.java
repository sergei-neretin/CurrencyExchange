package com.sergeineretin.dao;

import com.sergeineretin.*;
import com.sergeineretin.exceptions.CurrencyException;
import com.sergeineretin.exceptions.DatabaseException;
import com.sergeineretin.exceptions.ExchangeRateException;
import com.sergeineretin.model.Currency;
import com.sergeineretin.model.ExchangeRate;
import com.sergeineretin.model.NullExchangeRate;

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
        String sql = "SELECT er.ID, er.Rate, c.ID AS BaseID, c.FullName AS BaseFullName, c.Code AS BaseCode, c.Sign AS BaseSign, c2.*,\n" +
                "c2.ID AS TargetID, c2.FullName AS TargetFullName, c2.Code AS TargetCode, c2.Sign AS TargetSign\n" +
                "from ExchangeRates er \n" +
                "INNER JOIN Currencies c ON er.BaseCurrencyId = c.ID \n" +
                "INNER JOIN Currencies c2 ON er.TargetCurrencyId = c2.ID;";

        try (Connection conn = C3p0DataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<ExchangeRate> result = new LinkedList<>();
            while (rs.next()){
                ExchangeRate exchangeRate = convertResultSet(rs);
                result.add(exchangeRate);
            }
            rs.close();
            return result;
        } catch (Exception e) {
            throw new DatabaseException("Database is unavailable", e);
        }
    }

    public ExchangeRate findByName(String baseCode, String targetCode){
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
                "    Currencies c ON er.BaseCurrencyID = c.ID AND c.Code = ?\n" +
                "INNER JOIN \n" +
                "    Currencies c2 ON er.TargetCurrencyID = c2.ID AND c2.Code = ?;\n";
        try(Connection conn = C3p0DataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, baseCode);
            pstmt.setString(2, targetCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return convertResultSet(rs);
            } else {
                return new NullExchangeRate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database is unavailable", e);
        }
    }

    public void create(ExchangeRate exchangeRate) {
        String sql = "INSERT OR ABORT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)\n" +
                "VALUES ((SELECT ID FROM Currencies WHERE Code = ?), \n" +
                "        (SELECT ID FROM Currencies WHERE Code = ?), \n" +
                "        ?);";

        try(Connection conn = C3p0DataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, exchangeRate.getBaseCurrency().getCode());
            pstmt.setString(2, exchangeRate.getTargetCurrency().getCode());
            pstmt.setBigDecimal(3, exchangeRate.getRate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw new ExchangeRateException("A currency pair with this code already exists", new Throwable());
            } else if(e.getMessage().contains("SQLITE_CONSTRAINT_TRIGGER")) {
                throw new CurrencyException("One (or both) currencies from a currency pair do not exist in the database", new Throwable());
            } else {
                throw new DatabaseException("Database is unavailable", e);
            }
        }
    }

    public void update(ExchangeRate exchangeRate){
        String sql = "UPDATE ExchangeRates SET Rate = ? WHERE\n" +
                "BaseCurrencyId = (SELECT ID from Currencies c WHERE c.Code = ?)\n" +
                "AND TargetCurrencyId = (SELECT ID from Currencies c2 WHERE c2.Code = ?);";
        try(Connection conn = C3p0DataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, exchangeRate.getRate());
            pstmt.setString(2, exchangeRate.getBaseCurrency().getCode());
            pstmt.setString(3, exchangeRate.getTargetCurrency().getCode());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Database is unavailable", e);
        }
    }

    public ExchangeRate convertResultSet(ResultSet rs) throws SQLException {
        Currency baseCurrency = new Currency(
                rs.getLong("BaseID"),
                rs.getString("BaseCode"),
                rs.getString("BaseFullName"),
                rs.getString("BaseSign"));
        Currency targetCurrency = new Currency(
                rs.getLong("TargetID"),
                rs.getString("TargetCode"),
                rs.getString("TargetFullName"),
                rs.getString("TargetSign"));

        return ExchangeRate.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .rate(rs.getBigDecimal("Rate"))
                .ID(rs.getInt("ID"))
                .build();
    }
}
