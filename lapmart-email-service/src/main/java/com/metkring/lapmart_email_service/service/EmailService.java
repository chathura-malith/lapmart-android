package com.metkring.lapmart_email_service.service;

import com.metkring.lapmart_email_service.dto.request.RequestOrderDto;

public interface EmailService {
    void sendInvoiceEmail(RequestOrderDto requestOrderDto);
}