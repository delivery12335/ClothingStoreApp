package com.clothingstore.clothingstoreapp.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class ReportRow {
    public abstract String toTxtLine();

    public abstract String toCsvLine();

    protected String csvSafe(String value) {
        if (value == null) {
            return "";
        }
        String fixed = fixMojibake(value);
        return fixed.replace("\"", "\"\"").replace(";", " ");
    }

    private String fixMojibake(String value) {
        if (!looksLikeMojibake(value)) {
            return value;
        }
        return new String(value.getBytes(Charset.forName("windows-1251")), StandardCharsets.UTF_8);
    }

    private boolean looksLikeMojibake(String value) {
        return value.contains("Р°")
                || value.contains("Рµ")
                || value.contains("РЅ")
                || value.contains("Рѕ")
                || value.contains("Рё")
                || value.contains("СЃ")
                || value.contains("С‚")
                || value.contains("С‹")
                || value.contains("СЏ");
    }
}

