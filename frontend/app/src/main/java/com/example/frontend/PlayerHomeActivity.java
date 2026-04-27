package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.ApiResponse;
import com.example.frontend.api.TokenManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_home);

        MaterialButton btnEntrarPartida = findViewById(R.id.btnEntrarPartida);
        MaterialButton btnVerInventario = findViewById(R.id.btnVerInventario);

        btnEntrarPartida.setOnClickListener(v -> {
            startActivity(new Intent(PlayerHomeActivity.this, GameListActivity.class));
        });

        btnVerInventario.setOnClickListener(v -> {
            carregarInventario();
        });
    }

    private void carregarInventario() {
        String playerId = TokenManager.getInstance().getUserId();

        ApiClient.getApiService().getMyInventory(playerId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PlayerHomeActivity.this,
                            "Inventário verificado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    // Vai para a tela do inventário após o sucesso
                    startActivity(new Intent(PlayerHomeActivity.this, InventoryActivity.class));
                } else {
                    Toast.makeText(PlayerHomeActivity.this,
                            "Erro ao carregar inventário.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(PlayerHomeActivity.this,
                        "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}