package com.example.frontend.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.util.Log;
import com.example.frontend.BuildConfig;

/**
 * Cliente Retrofit Singleton
 * Centraliza a configuração do HTTP client e Retrofit
 * Uso:
 * ApiService service = ApiClient.getApiService();
 * service.healthCheck().enqueue(callback);
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    /**
     * Obter instância do Retrofit
     * Cria uma única vez e reutiliza
     */
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // Logging interceptor - ativo apenas em debug
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d(TAG, message);
            });
            loggingInterceptor.setLevel(
                BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE
            );

            // OkHttp client com interceptors e timeout
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new ApiInterceptor())              // Headers customizados
                .addInterceptor(loggingInterceptor)               // Logging
                .connectTimeout(AppConfig.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConfig.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AppConfig.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

            // Gson customizado
            Gson gson = new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .create();

            // Retrofit setup
            retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

            Log.i(TAG, "Retrofit inicializado com URL: " + AppConfig.API_BASE_URL);
        }

        return retrofit;
    }

    /**
     * Obter ApiService (interface Retrofit)
     * Cria uma única vez e reutiliza
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Reset do cliente (apenas para testes)
     */
    public static void reset() {
        retrofit = null;
        apiService = null;
    }
}
