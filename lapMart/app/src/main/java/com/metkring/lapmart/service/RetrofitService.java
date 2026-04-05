package com.metkring.lapmart.service;
import com.metkring.lapmart.api.EmailApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitService {
    private static final String BASE_URL = "http://13.229.95.130:8080/";
    private static Retrofit retrofit = null;

    public static EmailApi getEmailApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(EmailApi.class);
    }
}
