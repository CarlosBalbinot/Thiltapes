package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

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

        ApiClient.getApiService().getMyInventory(playerId).enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PlayerHomeActivity.this,
                            "Inventário carregado! " + response.body().size() + " carta(s).",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlayerHomeActivity.this,
                            "Erro ao carregar inventário.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Object>> call, Throwable t) {
                Toast.makeText(PlayerHomeActivity.this,
                        "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}