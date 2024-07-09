package com.framework.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private Connection connection;

    public DatabaseConnector(String url, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
    }

    public Connection getConnection() {
        return connection;
    }
}
