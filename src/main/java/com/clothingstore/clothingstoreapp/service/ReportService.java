package com.clothingstore.clothingstoreapp.service;

import com.clothingstore.clothingstoreapp.db.DatabaseConnection;
import com.clothingstore.clothingstoreapp.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    public List<ReportRow> generateReport(String reportType) {
        if (reportType == null) {
            return List.of();
        }

        return switch (reportType) {
            case ReportType.CATEGORY_SUMMARY -> generateCategorySummaryReport();
            case ReportType.PRICE_STATISTICS -> generatePriceStatisticsReport();
            case ReportType.STOCK_STATUS -> generateStockStatusReport();
            default -> List.of();
        };
    }

    public List<ReportRow> generateCategorySummaryReport() {
        List<ReportRow> rows = new ArrayList<>();
        String sql = """
                SELECT c.Name AS CategoryName,
                       COUNT(p.ProductID) AS ProductCount,
                       AVG(p.Price) AS AvgPrice
                FROM Categories c
                LEFT JOIN Products p ON p.CategoryID = c.CategoryID
                GROUP BY c.CategoryID, c.Name
                ORDER BY c.Name
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                rows.add(new CategorySummaryRow(
                        rs.getString("CategoryName"),
                        rs.getInt("ProductCount"),
                        rs.getDouble("AvgPrice")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<ReportRow> generatePriceStatisticsReport() {
        List<ReportRow> rows = new ArrayList<>();
        String sql = "SELECT MIN(Price) AS MinPrice, MAX(Price) AS MaxPrice, AVG(Price) AS AvgPrice FROM Products";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                rows.add(new PriceStatisticsRow(
                        rs.getDouble("MinPrice"),
                        rs.getDouble("MaxPrice"),
                        rs.getDouble("AvgPrice")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<ReportRow> generateStockStatusReport() {
        List<ReportRow> rows = new ArrayList<>();
        String sql = """
                SELECT CASE
                           WHEN Quantity = 0 THEN 'Нет на складе'
                           WHEN Quantity <= 10 THEN 'Мало'
                           WHEN Quantity <= 30 THEN 'Средний запас'
                           ELSE 'Высокий запас'
                       END AS StockStatus,
                       COUNT(*) AS ProductCount
                FROM Products
                GROUP BY StockStatus
                ORDER BY ProductCount DESC
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                rows.add(new StockStatusRow(
                        rs.getString("StockStatus"),
                        rs.getInt("ProductCount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}

