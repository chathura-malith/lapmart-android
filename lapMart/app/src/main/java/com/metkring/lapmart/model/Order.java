package com.metkring.lapmart.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Order {

    private String orderId;
    private String userId;
    private double totalAmount;
    private String status;
    private Timestamp timestamp; // Firebase වලින් එන වෙලාව ගන්නේ මේකෙන්
    private Address shippingAddress;
    private Address billingAddress;
    private List<Map<String, Object>> items; // Cart එකේ තිබුණ බඩු ටික

    // 🔴 1. හිස් Constructor එක (Firestore එකට දත්ත කියවන්න මේක අනිවාර්යයි)
    public Order() {
    }

    // 2. පරාමිතීන් සහිත Constructor එක
    public Order(String orderId, String userId, double totalAmount, String status,
                 Timestamp timestamp, Address shippingAddress, Address billingAddress,
                 List<Map<String, Object>> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.timestamp = timestamp;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.items = items;
    }

    // 3. Getters and Setters (දත්ත ගන්නයි, වෙනස් කරන්නයි)
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }
}