package com.example.frontend;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.ApiService;
import com.example.frontend.api.ApiResponse;
import com.example.frontend.api.AppConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MainActivity - Activity principal
 * Exibe o status da API em tempo real
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private TextView statusText;
    private TextView detailsText;
    private TextView urlText;
    private TextView environmentText;
    private View statusIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar views
        statusText = findViewById(R.id.statusText);
        detailsText = findViewById(R.id.sample_text);
        urlText = findViewById(R.id.urlText);
        environmentText = findViewById(R.id.environmentText);
        statusIndicator = findViewById(R.id.statusIndicator);

        // Exibir configuração
        urlText.setText(AppConfig.API_BASE_URL);
        String env = android.os.Build.VERSION.SDK_INT >= 23
            ? (getApplicationContext().getApplicationInfo().flags & 2) == 0 ? "release" : "debug"
            : "unknown";
        environmentText.setText(env);

        // Testar conexão com backend
        testBackendConnection();
    }

    /**
     * Testa conexão com o backend
     */
    private void testBackendConnection() {
        updateStatusUI("Conectando...", R.drawable.status_indicator_loading);
        
        ApiService apiService = ApiClient.getApiService();
        apiService.healthCheck().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    handleApiResponse(apiResponse);
                } else {
                    handleApiError("HTTP " + response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                handleConnectionFailure(t);
            }
        });
    }

    /**
     * Processa resposta bem-sucedida da API
     */
    private void handleApiResponse(ApiResponse apiResponse) {
        if (apiResponse.isSuccess()) {
            // Sucesso
            updateStatusUI("CONECTADO", R.drawable.status_indicator_success);
            
            // Formatar resposta para exibição
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonResponse = gson.toJson(apiResponse);
            
            String details = "✅ Status: " + apiResponse.getMessage() + "\n\n" +
                            "📍 Endpoint: GET /api/health\n" +
                            "⏱️ Status HTTP: 200 OK\n\n" +
                            "📦 Resposta:\n" + jsonResponse;
            
            detailsText.setText(details);
            statusText.setTextColor(getColor(android.R.color.holo_green_dark));
            
            Log.i(TAG, "Conexão bem-sucedida: " + apiResponse.getMessage());
        } else {
            // Erro da API (mesmo que HTTP 200)
            handleApiError(apiResponse.getError(), apiResponse.getMessage());
        }
    }

    /**
     * Processa erro na resposta HTTP
     */
    private void handleApiError(String errorType, String errorMessage) {
        updateStatusUI("ERRO", R.drawable.status_indicator_error);
        
        String details = "❌ Erro: " + errorType + "\n\n" +
                        "📍 Endpoint: GET /api/health\n" +
                        "ℹ️ Mensagem: " + errorMessage + "\n\n" +
                        "Possíveis causas:\n" +
                        "• Backend retornou erro\n" +
                        "• Dados inválidos\n" +
                        "• Versão de API incompatível";
        
        detailsText.setText(details);
        statusText.setTextColor(getColor(android.R.color.holo_red_dark));
        
        Log.e(TAG, "Erro da API: " + errorType + " - " + errorMessage);
    }

    /**
     * Processa falha de conexão
     */
    private void handleConnectionFailure(Throwable t) {
        updateStatusUI("DESCONECTADO", R.drawable.status_indicator_error);
        
        String details = "❌ Falha de Conexão\n\n" +
                        "📍 Endpoint: GET /api/health\n" +
                        "🔗 URL: " + AppConfig.API_BASE_URL + "\n" +
                        "⚠️ Erro: " + t.getMessage() + "\n\n" +
                        "Verifique:\n" +
                        "✓ Backend está rodando? (npm run dev)\n" +
                        "✓ URL em AppConfig.java está correta?\n" +
                        "✓ Firewall permite conexão?\n" +
                        "✓ Emulador pode chegar ao PC (10.0.2.2)?";
        
        detailsText.setText(details);
        statusText.setTextColor(getColor(android.R.color.holo_red_dark));
        
        Log.e(TAG, "Falha de conexão: " + t.getMessage(), t);
    }

    /**
     * Atualiza a UI com novo status
     */
    private void updateStatusUI(String statusMessage, int indicatorDrawable) {
        statusText.setText(statusMessage);
        statusIndicator.setBackground(getDrawable(indicatorDrawable));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-testar ao voltar para a activity
        testBackendConnection();
    }
}