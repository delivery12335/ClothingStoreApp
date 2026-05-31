package com.clothingstore.clothingstoreapp.enums;

import java.util.List;

public final class ReportType {

    public static final String CATEGORY_SUMMARY = "CATEGORY_SUMMARY";
    public static final String PRICE_STATISTICS = "PRICE_STATISTICS";
    public static final String STOCK_STATUS = "STOCK_STATUS";

    private ReportType() {
    }

    public static List<String> values() {
        return List.of(CATEGORY_SUMMARY, PRICE_STATISTICS, STOCK_STATUS);
    }

    public static String displayName(String reportType) {
        if (CATEGORY_SUMMARY.equals(reportType)) {
            return "Сводка по категориям";
        }
        if (PRICE_STATISTICS.equals(reportType)) {
            return "Статистика цен";
        }
        if (STOCK_STATUS.equals(reportType)) {
            return "Статусы запасов";
        }
        return reportType;
    }
}

