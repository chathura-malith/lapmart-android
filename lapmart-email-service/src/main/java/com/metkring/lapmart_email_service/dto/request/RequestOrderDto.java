package com.metkring.lapmart_email_service.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestOrderDto {
    private String orderId;
    private String customerName;
    private String customerEmail;
    private Double totalAmount;
    private List<RequestOrderItemDto> items;
}
