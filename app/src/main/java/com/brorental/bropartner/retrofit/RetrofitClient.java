package com.brorental.bropartner.retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit INSTANCE = null;

    public static Retrofit getInstance() {
        if(INSTANCE == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(180, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .readTimeout(180, TimeUnit.SECONDS)
                    .build();

            INSTANCE = new Retrofit.Builder()
                    .baseUrl("https://api.ghar.com")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return INSTANCE;
    }
}
