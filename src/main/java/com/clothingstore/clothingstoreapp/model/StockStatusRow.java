package com.clothingstore.clothingstoreapp.model;

public class StockStatusRow extends ReportRow {
    private final String stockStatus;
    private final int count;

    public StockStatusRow(String stockStatus, int count) {
        this.stockStatus = stockStatus;
        this.count = count;
    }

    @Override
    public String toTxtLine() {
        return String.format("Статус: %s | Количество товаров: %d", stockStatus, count);
    }

    @Override
    public String toCsvLine() {
        return String.format("%s;%d", csvSafe(stockStatus), count);
    }
}

