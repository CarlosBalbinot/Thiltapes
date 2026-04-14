package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frontend.api.*;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.*;

public class GameListActivity extends AppCompatActivity {

    private RecyclerView rvGames;
    private GameAdapter adapter;
    private List<Map<String, Object>> gameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        rvGames = findViewById(R.id.rvGames);
        MaterialButton btnRefresh = findViewById(R.id.btnRefresh);

        rvGames.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter(gameList, game -> {
            // Ao clicar, vai para a tela do jogo
            Intent intent = new Intent(GameListActivity.this, GameActivity.class);
            intent.putExtra("GAME_ID", String.valueOf(game.get("id")));
            intent.putExtra("GAME_NAME", String.valueOf(game.get("name")));
            startActivity(intent);
        });
        rvGames.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> fetchGames());
        fetchGames();
    }

    private void fetchGames() {
        ApiClient.getApiService().getGames().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().getData();
                    if (data != null) {
                        gameList.clear();
                        gameList.addAll(data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(GameListActivity.this, "Erro ao carregar jogos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}