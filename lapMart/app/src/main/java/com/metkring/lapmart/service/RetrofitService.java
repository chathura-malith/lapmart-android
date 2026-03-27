package com.metkring.lapmart.service;
import com.metkring.lapmart.api.EmailApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitService {
    private static final String BASE_URL = "http://192.168.8.199:8080/";
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
