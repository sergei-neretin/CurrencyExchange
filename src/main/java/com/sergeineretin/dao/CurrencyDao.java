package com.sergeineretin.dao;

import com.sergeineretin.C3p0DataSource;
import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.model.Currency;
import com.sergeineretin.services.CurrencyService;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class CurrencyDao {

    public CurrencyDao() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Currency findByName(String code) {
        String sql = "SELECT * FROM Currencies WHERE Code = ?;";
        try(Connection conn = C3p0DataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Currency result = CurrencyService.convertResultSet(rs);
                rs.close();
                return result;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseUnavailableException(e.getMessage(), e);
        }
    }
    public Currency findById(Long id) {
        String sql = "SELECT * FROM Currencies WHERE ID = ?;";
        try(Connection conn = C3p0DataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Currency result = CurrencyService.convertResultSet(rs);
                rs.close();
                return result;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseUnavailableException(e.getMessage(), e);
        }
    }
    public List<Currency> findAll() {
        String sql = "SELECT * FROM Currencies;";
        try(Connection conn = C3p0DataSource.getConnection();
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<Currency> result = new LinkedList<>();
            while (rs.next()) {
                Currency currencyDto = CurrencyService.convertResultSet(rs);
                result.add(currencyDto);
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public void create(Currency currency) {
        String sql = "INSERT INTO Currencies(FullName, Code, Sign) VALUES ( ?, ?, ?);";
        try(Connection conn = C3p0DataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currency.getFullName());
            pstmt.setString(2, currency.getCode());
            pstmt.setString(3, currency.getSign());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new CurrencyException("Currency with this code already exists", new Throwable());
            }
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }
}
