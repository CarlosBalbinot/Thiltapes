package com.example.frontend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView rvInventory;
    private InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        rvInventory = findViewById(R.id.rvInventory);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        
        loadInventory();
    }

    private void loadInventory() {
        String playerId = getIntent().getStringExtra("playerId");
        ApiClient.getApiService().getMyInventory(playerId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiRes = response.body();
                    if (apiRes.isSuccess()) {
                        // Converter Object data para List de Mapas (ou um modelo específico se existisse)
                        Gson gson = new Gson();
                        String json = gson.toJson(apiRes.getData());
                        List<Map<String, Object>> items = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>(){}.getType());
                        
                        adapter = new InventoryAdapter(items != null ? items : new ArrayList<>());
                        rvInventory.setAdapter(adapter);
                    } else {
                        Toast.makeText(InventoryActivity.this, apiRes.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(InventoryActivity.this, "Erro ao carregar inventário", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {
        private List<Map<String, Object>> items;

        public InventoryAdapter(List<Map<String, Object>> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = items.get(position);
            
            // Supondo que o backend retorna os campos: name, image_url, location_name, collected_at
            holder.tvName.setText(String.valueOf(item.getOrDefault("name", "Carta Desconhecida")));
            holder.tvLocation.setText("📍 Local: " + item.getOrDefault("location_name", "Local desconhecido"));
            holder.tvTime.setText("🕒 " + item.getOrDefault("collected_at", "--/--/----"));
            
            String imageUrl = String.valueOf(item.get("image_url"));
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivCard);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCard;
            TextView tvName, tvLocation, tvTime;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCard = itemView.findViewById(R.id.cardImage);
                tvName = itemView.findViewById(R.id.cardName);
                tvLocation = itemView.findViewById(R.id.cardLocation);
                tvTime = itemView.findViewById(R.id.cardTime);
            }
        }
    }
}