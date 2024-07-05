package com.sergeineretin.dao;

import com.sergeineretin.CurrencyException;
import com.sergeineretin.DatabaseUnavailableException;
import com.sergeineretin.Utils;
import com.sergeineretin.dto.CurrencyDto;
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

    public CurrencyDto findByName(String Code) {
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM Currencies WHERE Code = " + "'" + Code + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                CurrencyDto build = CurrencyService.convertResultSet(rs);
                rs.close();
                return build;
            } else {
                throw new CurrencyException("Currency not found", new Throwable());
            }
        } catch (SQLException e) {
            throw new DatabaseUnavailableException(e.getMessage(), e);
        }
    }
    public CurrencyDto findById(Long id) {
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM Currencies WHERE ID = " + id + ";";
            ResultSet rs = stmt.executeQuery(sql);
            CurrencyDto result = CurrencyService.convertResultSet(rs);
            rs.close();
            return result;
        } catch (SQLException e) {
            throw new DatabaseUnavailableException(e.getMessage(), e);
        }
    }
    public List<CurrencyDto> findAll() {
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM Currencies;";
            ResultSet rs = stmt.executeQuery(sql);
            List<CurrencyDto> result = new LinkedList<>();
            do {
                CurrencyDto currencyDto = CurrencyService.convertResultSet(rs);
                result.add(currencyDto);
            } while (rs.next());
            rs.close();
            return result;
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }

    public CurrencyDto create(CurrencyDto currencyDto) {
        try(Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl())) {
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO Currencies(FullName, Code, Sign) VALUES " +
                    "('" + currencyDto.getFullName() + "', '" + currencyDto.getCode() + "', '" + currencyDto.getSign() + "');";
            stmt.executeUpdate(sql);
            return findByName(currencyDto.getCode());
        } catch (SQLException e) {
            if (e.getMessage().contains("Currencies_Code_IDX")) {
                throw new CurrencyException("Currency with this code already exists", new Throwable());
            }
            throw new DatabaseUnavailableException("Database is unavailable", e);
        }
    }
}
