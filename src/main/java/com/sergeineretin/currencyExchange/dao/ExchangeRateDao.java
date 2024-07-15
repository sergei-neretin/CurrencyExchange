package com.sergeineretin.currencyExchange.dao;

import com.sergeineretin.currencyExchange.C3p0DataSource;
import com.sergeineretin.currencyExchange.exceptions.CurrencyException;
import com.sergeineretin.currencyExchange.exceptions.DatabaseException;
import com.sergeineretin.currencyExchange.exceptions.ExchangeRateException;
import com.sergeineretin.currencyExchange.model.Currency;
import com.sergeineretin.currencyExchange.model.ExchangeRate;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {
    public List<ExchangeRate> findAll() {
        String sql = Statements.EXCHANGE_RATE_SELECT_ALL;

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
            throw new DatabaseException("Database is unavailable");
        }
    }

    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) {
        String sql = Statements.EXCHANGE_RATE_SELECT_BY_CODE;
        try(Connection conn = C3p0DataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, baseCode);
            pstmt.setString(2, targetCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(convertResultSet(rs));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database is unavailable");
        }
    }

    public void create(ExchangeRate exchangeRate) {
        String sql = Statements.EXCHANGE_RATE_CREATE;

        try(Connection conn = C3p0DataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, exchangeRate.getBaseCurrency().getCode());
            pstmt.setString(2, exchangeRate.getTargetCurrency().getCode());
            pstmt.setBigDecimal(3, exchangeRate.getRate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw new ExchangeRateException("A currency pair with this code already exists");
            } else if(e.getMessage().contains("SQLITE_CONSTRAINT_TRIGGER")) {
                throw new CurrencyException("One (or both) currencies from a currency pair do not exist in the database");
            } else {
                throw new DatabaseException("Database is unavailable");
            }
        }
    }

    public Optional<ExchangeRate> update(ExchangeRate exchangeRate){
        try(Connection conn = C3p0DataSource.getConnection()) {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            if (isRecordExist(conn, exchangeRate)) {
                updateRecord(conn, exchangeRate);
                Optional<ExchangeRate> result = findRecord(conn, exchangeRate);
                conn.commit();
                return result;
            } else {
                conn.rollback();
                throw new ExchangeRateException("Currency pair is absent in the database");
            }
        } catch (SQLException e) {
            try (Connection conn = C3p0DataSource.getConnection()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Failed to rollback transaction", rollbackEx);
            }
            throw new DatabaseException("Database is unavailable");
        }
    }

    private Optional<ExchangeRate> findRecord(Connection conn, ExchangeRate exchangeRate) throws SQLException{
        try(PreparedStatement pstmt = conn.prepareStatement(Statements.EXCHANGE_RATE_SELECT_BY_CODE)) {
            pstmt.setString(1, exchangeRate.getBaseCurrency().getCode());
            pstmt.setString(2, exchangeRate.getTargetCurrency().getCode());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(convertResultSet(rs));
            } else {
                return Optional.empty();
            }
        }
    }

    private void updateRecord(Connection conn, ExchangeRate exchangeRate) throws SQLException {
        try(PreparedStatement pstmt = conn.prepareStatement(Statements.EXCHANGE_RATE_UPDATE)) {
            pstmt.setBigDecimal(1, exchangeRate.getRate());
            pstmt.setString(2, exchangeRate.getBaseCurrency().getCode());
            pstmt.setString(3, exchangeRate.getTargetCurrency().getCode());
            pstmt.executeUpdate();
        }
    }

    private boolean isRecordExist(Connection conn, ExchangeRate exchangeRate) throws SQLException{
        try(PreparedStatement pstmt = conn.prepareStatement(Statements.EXCHANGE_RATE_SELECT_BY_CODE)) {
            pstmt.setString(1, exchangeRate.getBaseCurrency().getCode());
            pstmt.setString(2, exchangeRate.getTargetCurrency().getCode());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
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
                .id(rs.getInt("ID"))
                .build();
    }
}
