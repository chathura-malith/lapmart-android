package com.metkring.lapmart.api; // 👈 ඔයාගේ package නම දෙන්න

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface EmailJSApi {
    @Headers({
            "Content-Type: application/json",
            "origin: http://localhost" // 👈 මේ පේළිය අනිවාර්යයෙන්ම එකතු කරන්න
    })
    @POST("api/v1.0/email/send")
    Call<Void> sendEmail(@Body JsonObject payload);
}