package com.clothingstore.clothingstoreapp.service;

import com.clothingstore.clothingstoreapp.model.ExportFormat;
import com.clothingstore.clothingstoreapp.model.Product;
import com.clothingstore.clothingstoreapp.model.ProductExportRow;
import com.clothingstore.clothingstoreapp.model.ReportRow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExportService {
    public void exportRows(List<? extends ReportRow> rows, String format, Path filePath) throws IOException {
        Exporter exporter = resolveExporter(format);
        exporter.export(rows, filePath);
    }

    public void exportProducts(List<Product> products, String format, Path filePath) throws IOException {
        List<ProductExportRow> rows = new ArrayList<>();
        for (Product product : products) {
            rows.add(new ProductExportRow(product));
        }
        exportRows(rows, format, filePath);
    }

    private Exporter resolveExporter(String format) {
        if (format == null) {
            return new TxtExporter();
        }

        if (ExportFormat.CSV.equals(format)) {
            return new CsvExporter();
        }
        return new TxtExporter();
    }
}

