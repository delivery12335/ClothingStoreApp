package com.clothingstore.clothingstoreapp.model;

import java.util.Locale;

public class CategorySummaryRow extends ReportRow {
    private final String category;
    private final int productCount;
    private final double avgPrice;

    public CategorySummaryRow(String category, int productCount, double avgPrice) {
        this.category = category;
        this.productCount = productCount;
        this.avgPrice = avgPrice;
    }

    @Override
    public String toTxtLine() {
        return String.format("Категория: %s | Товаров: %d | Средняя цена: %.2f", category, productCount, avgPrice);
    }

    @Override
    public String toCsvLine() {
        return String.format(Locale.US, "%s;%d;%.2f", csvSafe(category), productCount, avgPrice);
    }
}

