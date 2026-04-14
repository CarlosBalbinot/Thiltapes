package com.example.frontend;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.frontend.api.*;
import com.google.android.material.button.MaterialButton;
import retrofit2.*;

public class GameActivity extends AppCompatActivity {

    private TextView tvGameTitle, tvCharInfo;
    private String gameId, gameName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameId = getIntent().getStringExtra("GAME_ID");
        gameName = getIntent().getStringExtra("GAME_NAME");

        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvCharInfo = findViewById(R.id.tvCharInfo);
        MaterialButton btnExit = findViewById(R.id.btnExit);
        MaterialButton btnAction = findViewById(R.id.btnAction);

        tvGameTitle.setText(gameName);
        
        // Simulação de info do personagem
        tvCharInfo.setText("Explorador (Nível 1)");

        btnExit.setOnClickListener(v -> finish());
        
        btnAction.setOnClickListener(v -> {
            Toast.makeText(this, "Você interagiu com o mundo de " + gameName, Toast.LENGTH_SHORT).show();
        });

        loadGameData();
    }

    private void loadGameData() {
        ApiClient.getApiService().getGame(gameId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Aqui você processaria os dados do mapa, NPCs, etc.
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Silencioso ou log
            }
        });
    }
}