package com.clothingstore.clothingstoreapp.model;

import java.util.Locale;

public class PriceStatisticsRow extends ReportRow {
    private final double minPrice;
    private final double maxPrice;
    private final double avgPrice;

    public PriceStatisticsRow(double minPrice, double maxPrice, double avgPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.avgPrice = avgPrice;
    }

    @Override
    public String toTxtLine() {
        return String.format("Мин. цена: %.2f | Макс. цена: %.2f | Средняя цена: %.2f", minPrice, maxPrice, avgPrice);
    }

    @Override
    public String toCsvLine() {
        return String.format(Locale.US, "%.2f;%.2f;%.2f", minPrice, maxPrice, avgPrice);
    }
}

