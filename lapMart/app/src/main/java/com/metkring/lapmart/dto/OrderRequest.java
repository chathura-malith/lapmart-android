package com.metkring.lapmart.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String orderId;
    private String customerName;
    private String customerEmail;
    private Double totalAmount;
    private List<OrderItemDto> items;

}
