package com.clothingstore.clothingstoreapp.model;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerOrder {
    private final int id;
    private final LocalDateTime createdAt;
    private final String customerName;
    private final String phone;
    private final String address;
    private final String paymentMethod;
    private final String comment;
    private final double total;
    private final List<String> items;

    public CustomerOrder(int id, LocalDateTime createdAt, String customerName, String phone,
                         String address, String paymentMethod, String comment, double total,
                         List<String> items) {
        this.id = id;
        this.createdAt = createdAt;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.comment = comment;
        this.total = total;
        this.items = List.copyOf(items);
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getComment() {
        return comment;
    }

    public double getTotal() {
        return total;
    }

    public List<String> getItems() {
        return items;
    }
}
