package com.example.frontend.api;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import android.util.Log;

/**
 * Interceptor OkHttp customizado
 * Adiciona headers padrão em todas as requisições
 * Pronto para adicionar Token JWT depois (sem refação)
 */
public class ApiInterceptor implements Interceptor {
    private static final String TAG = "ApiInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Construir request com headers padrão
        Request.Builder requestBuilder = originalRequest.newBuilder()
            // Headers padrão
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", AppConfig.USER_AGENT)
            .header("X-API-Version", AppConfig.API_VERSION);

        // Adicionando o Token JWT nas requisições:
        try {
            String token = TokenManager.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }
        } catch (IllegalStateException e) {
            // Ignorar erro se o TokenManager não tiver sido instanciado a tempo (ex: inicio do app)
        }

        Request newRequest = requestBuilder.build();

        // Log da requisição (apenas em debug)
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Request: " + newRequest.method() + " " + newRequest.url());
        }

        // Executar requisição
        Response response = chain.proceed(newRequest);

        // Log da resposta (apenas em debug)
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Response: " + response.code() + " " + newRequest.url());
        }

        return response;
    }
}
