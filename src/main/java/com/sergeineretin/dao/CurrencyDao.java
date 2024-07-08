package com.sergeineretin.dao;

import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.Utils;
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

    public Currency findByName(String Code) {
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM Currencies WHERE Code = '" + Code + "';";
            ResultSet rs = stmt.executeQuery(sql);
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
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM Currencies WHERE ID = " + id + ";";
            ResultSet rs = stmt.executeQuery(sql);
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
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM Currencies;";
            ResultSet rs = stmt.executeQuery(sql);
            List<Currency> result = new LinkedList<>();
            while (rs.next()) {
                Currency currencyDto = CurrencyService.convertResultSet(rs);
                result.add(currencyDto);
            }
            System.out.println(result);
            rs.close();
            return result;
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public void create(Currency currencyDto) {
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO Currencies(FullName, Code, Sign) VALUES " +
                    "('" + currencyDto.getFullName() + "', '" + currencyDto.getCode() + "', '" + currencyDto.getSign() + "');";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new CurrencyException("Currency with this code already exists", new Throwable());
            }
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }
}
