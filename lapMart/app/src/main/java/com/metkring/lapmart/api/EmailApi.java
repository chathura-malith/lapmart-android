package com.metkring.lapmart.api;

import com.metkring.lapmart.dto.OrderRequest;
import com.metkring.lapmart.dto.StandardResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface EmailApi {

    @POST("api/v1/email/send-invoice")
    Call<StandardResponseDto> sendInvoiceEmail(@Body OrderRequest request);
}