package com.clothingstore.clothingstoreapp.service;

import com.clothingstore.clothingstoreapp.model.ReportRow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TxtExporter implements Exporter {
    @Override
    public void export(List<? extends ReportRow> rows, Path filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        for (ReportRow row : rows) {
            lines.add(row.toTxtLine());
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }
}

