package com.clothingstore.clothingstoreapp.service;

import com.clothingstore.clothingstoreapp.model.ReportRow;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvExporter implements Exporter {
    private static final Charset EXCEL_CYRILLIC_CHARSET = Charset.forName("windows-1251");

    @Override
    public void export(List<? extends ReportRow> rows, Path filePath) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("sep=;").append(System.lineSeparator());
        for (ReportRow row : rows) {
            csv.append(row.toCsvLine()).append(System.lineSeparator());
        }
        Files.writeString(filePath, csv.toString(), EXCEL_CYRILLIC_CHARSET);
    }
}

