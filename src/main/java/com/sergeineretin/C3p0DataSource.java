package com.sergeineretin;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class C3p0DataSource {
    private static final ComboPooledDataSource cpds = new ComboPooledDataSource();

    static {
        try {
            cpds.setDriverClass("org.sqlite.JDBC");
            cpds.setJdbcUrl("jdbc:sqlite::resource:CurrencyExchange.db");
        } catch (PropertyVetoException e) {
            throw new RuntimeException("Failed to set database driver", e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to set JDBC URL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return cpds.getConnection();
    }

    public static void close() {
        cpds.close();
        try {
            DriverManager.deregisterDriver(DriverManager.getDriver("jdbc:sqlite::resource:CurrencyExchange.db"));
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    private C3p0DataSource(){}
}
