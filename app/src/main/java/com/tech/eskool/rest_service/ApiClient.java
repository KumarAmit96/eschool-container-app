package com.tech.eskool.rest_service;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://student.eskooltech.com/api/";

    public static Retrofit retrofit = null;

    public static Retrofit getClient() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(30, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(30, TimeUnit.MINUTES) // write timeout
                .readTimeout(30, TimeUnit.MINUTES); // read timeout
        httpClient.addInterceptor(new Interceptor() {
            @NonNull
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request originalRequest = chain.request();

                Request request = originalRequest.newBuilder()
                        .header("Content-Type", "application/json; charset=utf-8")
                        .method(originalRequest.method(), originalRequest.body())
                        .build();

                return chain.proceed(request);
            }
        });
        OkHttpClient client = httpClient.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                .client(client)
                .build();

        return retrofit;
    }


}
