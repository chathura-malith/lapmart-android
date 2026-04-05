package com.metkring.lapmart_email_service.controller;

import com.metkring.lapmart_email_service.dto.request.RequestOrderDto;
import com.metkring.lapmart_email_service.service.EmailService;
import com.metkring.lapmart_email_service.util.StandardResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-invoice")
    public ResponseEntity<StandardResponseDto> sendInvoice(@RequestBody RequestOrderDto requestOrderDto) {
        try {
            emailService.sendInvoiceEmail(requestOrderDto);
            return new ResponseEntity<>(
                    new StandardResponseDto(200, "Invoice email sent successfully!", null),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new StandardResponseDto(500, "Error sending email: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}