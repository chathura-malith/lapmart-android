package com.metkring.lapmart.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private String orderId;
    private String userId;
    private double totalAmount;
    private String status;
    private Timestamp timestamp;
    private Address shippingAddress;
    private Address billingAddress;
    private List<Map<String, Object>> items;
}