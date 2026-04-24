package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.ApiResponse;
import com.example.frontend.api.ApiService;
import com.example.frontend.api.AppConfig;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MainActivity - Activity principal com Mapa e Menu Lateral
 */
public class MainActivity extends AppCompatActivity {
    
    private DrawerLayout drawerLayout;
    private TextView statusText;
    private View statusIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar Drawer e Navigation View
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageButton btnMenu = findViewById(R.id.btnMenu);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
            } else if (id == R.id.nav_games) {
                startActivity(new Intent(this, GameListActivity.class));
            } else if (id == R.id.nav_logout) {
                getSharedPreferences("APP", MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Inicializar outras views
        statusText = findViewById(R.id.statusText);
        statusIndicator = findViewById(R.id.statusIndicator);
        TextView urlText = findViewById(R.id.urlText);

        // Exibir configuração
        if (urlText != null) {
            urlText.setText(AppConfig.API_BASE_URL);
        }

        // Testar conexão com backend
        testBackendConnection();

        // Lógica do Botão Voltar moderna
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void testBackendConnection() {
        ApiService apiService = ApiClient.getApiService();
        apiService.healthCheck().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleApiResponse(response.body());
                } else {
                    updateStatusUI("ERRO", android.R.color.holo_red_dark);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                updateStatusUI("OFFLINE", android.R.color.holo_red_dark);
            }
        });
    }

    private void handleApiResponse(ApiResponse apiResponse) {
        if (apiResponse.isSuccess()) {
            updateStatusUI("CONECTADO", android.R.color.holo_green_dark);
        } else {
            updateStatusUI("ERRO API", android.R.color.holo_orange_dark);
        }
    }

    private void updateStatusUI(String message, int colorRes) {
        if (statusText != null) statusText.setText(message);
        if (statusIndicator != null) statusIndicator.setBackgroundColor(getColor(colorRes));
    }

    @Override
    protected void onResume() {
        super.onResume();
        testBackendConnection();
    }
}