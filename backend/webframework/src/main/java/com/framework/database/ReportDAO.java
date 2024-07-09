package com.framework.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportDAO {
    private final Connection connection;

    public ReportDAO(Connection connection) {
        this.connection = connection;
    }

    public void saveReport(String data) throws SQLException {
        String query = "INSERT INTO reports (data) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, data);
            statement.executeUpdate();
        }
    }

    public String getAllReports() throws SQLException {
        StringBuilder reports = new StringBuilder();
        String query = "SELECT * FROM reports";
        try (PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.append("ID: ").append(resultSet.getInt("id"))
                       .append(", Data: ").append(resultSet.getString("data"))
                       .append(", Created At: ").append(resultSet.getTimestamp("created_at"))
                       .append("\n");
            }
        }
        return reports.toString();
    }
}
