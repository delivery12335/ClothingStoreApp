package com.clothingstore.clothingstoreapp.service;

import com.clothingstore.clothingstoreapp.model.ReportRow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Exporter {
    void export(List<? extends ReportRow> rows, Path filePath) throws IOException;
}

