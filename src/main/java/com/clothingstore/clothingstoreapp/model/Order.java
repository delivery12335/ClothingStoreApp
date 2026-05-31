package com.clothingstore.clothingstoreapp.model;

import com.clothingstore.clothingstoreapp.enums.OrderStatus;

public class Order extends BaseEntity {
    private int userId;
    private String orderDate;
    private double totalAmount;
    private OrderStatus status;

    public Order(int id, int userId, String orderDate, double totalAmount, OrderStatus status) {
        super(id);
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

