package com.tech.eskool.rest_service;

import com.google.gson.GsonBuilder;
import com.tech.eskool.HttpClientProvider;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://student.eskooltech.com/api/";

    private static volatile Retrofit retrofit;

    /** Host of the school server, e.g. student.eskooltech.com. */
    public static String serverHost() {
        return android.net.Uri.parse(BASE_URL).getHost();
    }

    /**
     * One shared Retrofit instance. It uses {@link HttpClientProvider}'s client so every API call
     * (not only login) carries the WebView cookies / User-Agent that pass the server's firewall check,
     * and so a new connection pool isn't created for every request.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    OkHttpClient client = HttpClientProvider.get().newBuilder()
                            .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                                    .header("Content-Type", "application/json; charset=utf-8")
                                    .build()))
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }
}
