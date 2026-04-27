package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.api.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameListActivity extends AppCompatActivity {

    private static final String TAG = "GameListActivity";
    private RecyclerView rvGames;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private GameAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        // Inicializar Views
        rvGames = findViewById(R.id.rvGames);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        rvGames.setLayoutManager(new LinearLayoutManager(this));
        
        loadGames();
    }

    private void loadGames() {
        showLoading(true);
        Log.d(TAG, "Iniciando busca de mundos no servidor...");

        ApiClient.getApiService().getGames().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> games = response.body();
                    updateUI(games);
                } else {
                    showError("Erro no servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                showError("Erro de conexão: Verifique seu servidor");
            }
        });
    }

    private void updateUI(List<Map<String, Object>> games) {
        if (games == null || games.isEmpty()) {
            tvEmptyState.setText("Nenhum mundo disponível");
            tvEmptyState.setVisibility(View.VISIBLE);
            rvGames.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvGames.setVisibility(View.VISIBLE);
            adapter = new GameAdapter(games);
            rvGames.setAdapter(adapter);
        }
    }

    private void showLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
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
            holder.tvDesc.setText("Status: " + game.getOrDefault("status", "Ativo"));

            holder.itemView.setOnClickListener(v -> {
                Object idObj = game.get("id");
                if (idObj == null) idObj = game.get("_id");
                
                Intent intent = new Intent(GameListActivity.this, MainActivity.class);
                intent.putExtra("GAME_ID", String.valueOf(idObj));
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
