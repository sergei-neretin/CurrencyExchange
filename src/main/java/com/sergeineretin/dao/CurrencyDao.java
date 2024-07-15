package com.sergeineretin.dao;

import com.sergeineretin.C3p0DataSource;
import com.sergeineretin.exceptions.CurrencyException;
import com.sergeineretin.exceptions.DatabaseException;
import com.sergeineretin.model.Currency;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {
    public Optional<Currency> findByCode(String code) {
        String sql = Statements.CURRENCY_SELECT_BY_CODE;
        try(Connection conn = C3p0DataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Currency result = convertResultSet(rs);
                rs.close();
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
    public List<Currency> findAll() {
        String sql = Statements.CURRENCY_SELECT_ALL;
        try(Connection conn = C3p0DataSource.getConnection();
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<Currency> result = new LinkedList<>();
            while (rs.next()) {
                Currency currencyDto = convertResultSet(rs);
                result.add(currencyDto);
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Database is unavailable");
        }
    }

    public void create(Currency currency) {
        String sql = Statements.CURRENCY_CREATE;
        try(Connection conn = C3p0DataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currency.getFullName());
            pstmt.setString(2, currency.getCode());
            pstmt.setString(3, currency.getSign());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new CurrencyException("Currency with this code already exists");
            }
            throw new DatabaseException("Database is unavailable");
        }
    }

    public Currency convertResultSet(ResultSet rs) throws SQLException {
        return Currency.builder()
                .id(rs.getLong("Id"))
                .code(rs.getString("Code"))
                .fullName(rs.getString("FullName"))
                .sign(rs.getString("Sign"))
                .build();
    }
}
