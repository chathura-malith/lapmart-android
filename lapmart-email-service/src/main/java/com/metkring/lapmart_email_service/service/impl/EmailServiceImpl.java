package com.metkring.lapmart_email_service.service.impl;

import com.metkring.lapmart_email_service.dto.request.RequestOrderDto;
import com.metkring.lapmart_email_service.dto.request.RequestOrderItemDto;
import com.metkring.lapmart_email_service.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendInvoiceEmail(RequestOrderDto request) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(request.getCustomerEmail());
            helper.setSubject("LapMart - Your Order Invoice (#" + request.getOrderId() + ")");

            String htmlContent = buildHtmlEmail(request);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Invoice email successfully sent to: {}", request.getCustomerEmail());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", request.getCustomerEmail(), e);
            throw new RuntimeException("Failed to send email");
        }
    }

    private String buildHtmlEmail(RequestOrderDto request) {
        StringBuilder itemsHtml = new StringBuilder();

        for (RequestOrderItemDto item : request.getItems()) {
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 12px 8px; border-bottom: 1px solid #eeeeee;" +
                            " font-size: 14px; color: #333;'>").append(item.getProductName()).append("</td>")
                    .append("<td style='padding: 12px 8px; border-bottom: 1px solid #eeeeee;" +
                            " text-align: center; font-size: 14px; color: #333;'>").append(item.getQuantity())
                    .append("</td>")
                    .append("<td style='padding: 12px 8px; border-bottom: 1px solid #eeeeee;" +
                            " text-align: right; font-size: 14px; color: #333;'>Rs. ").append(String.format(
                                    "%,.2f", item.getPrice())).append("</td>")
                    .append("</tr>");
        }

        String totalFormatted = String.format("%,.2f", request.getTotalAmount());

        String htmlTemplate = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>LapMart Invoice</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial,
                 sans-serif; background-color: #f4f5f7;">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%"
                     style="background-color: #f4f5f7; padding: 20px 10px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" border="0" cellpadding="0" cellspacing="0"
                                 width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px;
                                  overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
                                    
                                    <tr>
                                        <td style="padding: 30px 20px; text-align: center; background-color: #D9003C;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 26px; letter-spacing:
                                             1.5px;">LAPMART</h1>
                                        </td>
                                    </tr>
                                    
                                    <tr>
                                        <td style="padding: 30px 20px;">
                                            <h2 style="color: #333333; margin-top: 0; font-size: 20px;">
                                            Thank you for your order!</h2>
                                            <p style="color: #555555; line-height: 1.6; font-size: 15px;">Hi <b>%s</b>,</p>
                                            <p style="color: #555555; line-height: 1.6; font-size: 15px;">
                                            We have received your order <b>#%s</b> and it is currently being processed.
                                             Here is your invoice summary:</p>

                                            <table role="presentation" border="0" cellpadding="0" cellspacing="0"
                                             width="100%%" style="margin-top: 25px; border-collapse: collapse;">
                                                <thead>
                                                    <tr style="background-color: #f8f9fa;">
                                                        <th style="padding: 12px 8px; text-align: left; font-size:
                                                         14px; color: #555; border-bottom: 2px solid #ddd;">Item</th>
                                                        <th style="padding: 12px 8px; text-align: center; font-size:
                                                         14px; color: #555; border-bottom: 2px solid #ddd;">Qty</th>
                                                        <th style="padding: 12px 8px; text-align: right; font-size:
                                                         14px; color: #555; border-bottom: 2px solid #ddd;">Price</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    %s
                                                </tbody>
                                                <tfoot>
                                                    <tr>
                                                        <td colspan="2" style="padding: 15px 8px; text-align:
                                                         right; font-weight: bold; font-size: 16px; color: #333;">
                                                         Total Amount:</td>
                                                        <td style="padding: 15px 8px; text-align: right;
                                                         font-weight: bold; font-size: 16px; color: #D9003C;">Rs. %s</td>
                                                    </tr>
                                                </tfoot>
                                            </table>
                                            
                                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">
                                            
                                            <p style="margin: 0; font-size: 12px; text-align: center; color: #999999;
                                             line-height: 1.5;">
                                                This is an automatically generated email. Please do not reply.<br>
                                                &copy; 2026 LapMart Sri Lanka. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;

        return String.format(htmlTemplate, request.getCustomerName(), request.getOrderId(), itemsHtml.toString(),
                totalFormatted);
    }

}
