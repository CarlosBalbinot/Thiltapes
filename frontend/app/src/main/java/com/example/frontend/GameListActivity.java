package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameListActivity extends AppCompatActivity {

    private RecyclerView rvGames;
    private GameAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        rvGames = findViewById(R.id.rvGames);
        rvGames.setLayoutManager(new LinearLayoutManager(this));

        loadGames();
    }

    private void loadGames() {
        ApiClient.getApiService().getGames().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiRes = response.body();
                    if (apiRes.isSuccess()) {
                        Gson gson = new Gson();
                        String json = gson.toJson(apiRes.getData());
                        List<Map<String, Object>> games = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>(){}.getType());
                        
                        adapter = new GameAdapter(games != null ? games : new ArrayList<>());
                        rvGames.setAdapter(adapter);
                    } else {
                        Toast.makeText(GameListActivity.this, apiRes.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(GameListActivity.this, "Erro ao carregar mundos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {
        private List<Map<String, Object>> games;

        public GameAdapter(List<Map<String, Object>> games) {
            this.games = games;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> game = games.get(position);
            holder.tvName.setText(String.valueOf(game.getOrDefault("name", "Mundo Desconhecido")));
            holder.tvDesc.setText(String.valueOf(game.getOrDefault("description", "Sem descrição disponível")));

            holder.itemView.setOnClickListener(v -> {
                String gameId = String.valueOf(game.get("id"));
                Intent intent = new Intent(GameListActivity.this, MainActivity.class);
                intent.putExtra("GAME_ID", gameId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return games.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.gameName);
                tvDesc = itemView.findViewById(R.id.gameDescription);
            }
        }
    }
}