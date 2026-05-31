package com.clothingstore.clothingstoreapp.enums;

public enum OrderStatus {
    PROCESSING("Обрабатывается"),
    SHIPPED("Отправлен"),
    DELIVERED("Доставлен"),
    CANCELED("Отменён");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static OrderStatus fromDbValue(String value) {
        if (value == null) {
            return PROCESSING;
        }
        for (OrderStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value.trim())
                    || status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return PROCESSING;
    }
}


