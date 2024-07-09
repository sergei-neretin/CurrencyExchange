package com.sergeineretin;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public class C3p0DataSource {
    private static ComboPooledDataSource cpds = new ComboPooledDataSource();

    static {
        try {
            cpds.setDriverClass("org.sqlite.JDBC");
            String databasePath = Utils.getDatabasePath();
            cpds.setJdbcUrl("jdbc:sqlite:" + databasePath);
        } catch (PropertyVetoException e) {
            throw new RuntimeException("Failed to set database driver", e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to set JDBC URL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return cpds.getConnection();
    }

    private C3p0DataSource(){}
}
