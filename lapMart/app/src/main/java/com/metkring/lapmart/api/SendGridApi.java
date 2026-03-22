package com.metkring.lapmart.api; // ඔයාගේ package name එකට ගැලපෙන්න වෙනස් කරන්න

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SendGridApi {
    @POST("v3/mail/send")
    Call<Void> sendEmail(
            @Header("Authorization") String authHeader,
            @Body JsonObject payload
    );
}