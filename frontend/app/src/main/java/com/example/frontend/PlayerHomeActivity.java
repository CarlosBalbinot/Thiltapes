package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_home);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnEntrarPartida = findViewById(R.id.btnEntrarPartida);
        MaterialButton btnVerInventario = findViewById(R.id.btnVerInventario);

        // Botão de voltar como Log Out
        btnBack.setOnClickListener(v -> logout());

        btnEntrarPartida.setOnClickListener(v -> {
            startActivity(new Intent(PlayerHomeActivity.this, GameListActivity.class));
        });

        btnVerInventario.setOnClickListener(v -> {
            carregarInventario();
        });
    }

    private void logout() {
        TokenManager.getInstance().clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show();
    }

    private void carregarInventario() {
        String playerId = TokenManager.getInstance().getUserId();

        ApiClient.getApiService().getMyInventory(playerId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    startActivity(new Intent(PlayerHomeActivity.this, InventoryActivity.class));
                } else {
                    Toast.makeText(PlayerHomeActivity.this, "Erro ao carregar inventário.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(PlayerHomeActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
