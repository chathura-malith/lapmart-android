package com.metkring.lapmart_email_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestOrderItemDto {
    private String productName;
    private Double price;
    private Integer quantity;
}