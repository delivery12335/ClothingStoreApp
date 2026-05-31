package com.clothingstore.clothingstoreapp.model;

import java.util.Locale;

public class ProductExportRow extends ReportRow {
    private final Product product;

    public ProductExportRow(Product product) {
        this.product = product;
    }

    @Override
    public String toTxtLine() {
        return String.format(
                "ID=%d | %s | Бренд=%s | Цвет=%s | Размер=%s | Цена=%.2f | Остаток=%d",
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getColor(),
                product.getSize(),
                product.getPrice(),
                product.getQuantity()
        );
    }

    @Override
    public String toCsvLine() {
        return String.format(
                Locale.US,
                "%d;%s;%s;%s;%s;%.2f;%d",
                product.getId(),
                csvSafe(product.getName()),
                csvSafe(product.getBrand()),
                csvSafe(product.getColor()),
                csvSafe(product.getSize()),
                product.getPrice(),
                product.getQuantity()
        );
    }
}

