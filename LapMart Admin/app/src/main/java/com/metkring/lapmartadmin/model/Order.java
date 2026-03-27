package com.metkring.lapmartadmin.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Order {

    private String orderId;
    private String userId;
    private Double totalAmount;
    private String status;
    private Timestamp timestamp;
    private Address shippingAddress;
    private Address billingAddress;
    private List<Map<String, Object>> items;

    public Order() {
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress; }

    public Address getBillingAddress() { return billingAddress; }
    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
}