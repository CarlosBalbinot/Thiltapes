package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.ApiResponse;
import com.example.frontend.api.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCreateGameActivity extends AppCompatActivity {

    private TextInputEditText etNomeJogo, etNumeroCartas, etRaio;
    private TextView tvLocalizacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_game);

        // Inicializar Views
        etNomeJogo = findViewById(R.id.etNomeJogo);
        etNumeroCartas = findViewById(R.id.etNumeroCartas);
        etRaio = findViewById(R.id.etRaio);
        tvLocalizacao = findViewById(R.id.tvLocalizacao);
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnObterLocalizacao = findViewById(R.id.btnObterLocalizacao);
        MaterialButton btnCriarJogo = findViewById(R.id.btnCriarJogo);

        // Vincular o clique do botão personalizado ao Logout
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> logout());
        }

        btnCriarJogo.setOnClickListener(v -> criarJogo());
    }

    private void logout() {
        TokenManager.getInstance().clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sessão de Admin encerrada", Toast.LENGTH_SHORT).show();
    }

    private void criarJogo() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", etNomeJogo.getText().toString());
        data.put("num_cards", etNumeroCartas.getText().toString());
        data.put("radius", etRaio.getText().toString());

        ApiClient.getApiService().createGame(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCreateGameActivity.this, "Jogo criado!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminCreateGameActivity.this, "Erro ao criar jogo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(AdminCreateGameActivity.this, "Erro de rede", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
