package com.clothingstore.clothingstoreapp.model;

import java.util.List;

public final class ExportFormat {

    public static final String CSV = "CSV";
    public static final String TXT = "TXT";

    private ExportFormat() {
    }

    public static List<String> values() {
        return List.of(CSV, TXT);
    }
}

