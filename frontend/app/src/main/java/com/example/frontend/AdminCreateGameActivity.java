package com.example.frontend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.ApiResponse;
import com.example.frontend.api.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCreateGameActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextInputEditText etNomeJogo, etNumeroCartas, etRaio;
    private TextView tvLocalizacao;
    private FusedLocationProviderClient fusedLocationClient;
    private Double currentLatitude;
    private Double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_game);

        TokenManager.init(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        btnObterLocalizacao.setOnClickListener(v -> fetchCurrentLocation());
        btnCriarJogo.setOnClickListener(v -> criarJogo());

        fetchCurrentLocation();
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
        if (currentLatitude == null || currentLongitude == null) {
            Toast.makeText(this, "Obtenha a localização antes de criar o jogo", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = etNomeJogo.getText() != null ? etNomeJogo.getText().toString().trim() : "";
        String numeroCartas = etNumeroCartas.getText() != null ? etNumeroCartas.getText().toString().trim() : "";
        String raio = etRaio.getText() != null ? etRaio.getText().toString().trim() : "";

        if (nome.isEmpty() || numeroCartas.isEmpty() || raio.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer cards = Integer.parseInt(numeroCartas);
        Integer radius = Integer.parseInt(raio);

        Map<String, Object> data = new HashMap<>();
        data.put("name", nome);
        data.put("admin_id", TokenManager.getInstance().getUserId());
        data.put("number_of_cards", cards);
        data.put("radius", radius);

        Map<String, Object> currentLocation = new HashMap<>();
        currentLocation.put("lat", currentLatitude);
        currentLocation.put("lng", currentLongitude);
        data.put("current_location", currentLocation);

        ApiClient.getApiService().createGame(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCreateGameActivity.this, "Jogo criado!", Toast.LENGTH_SHORT).show();
                    etNomeJogo.setText("");
                    etNumeroCartas.setText("");
                    etRaio.setText("");
                    fetchCurrentLocation();
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

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                tvLocalizacao.setText("📍 Localização indisponível no momento");
                Toast.makeText(this, "Não foi possível obter a localização atual", Toast.LENGTH_SHORT).show();
                return;
            }

            updateLocation(location);
        });
    }

    private void updateLocation(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        tvLocalizacao.setText(String.format("📍 Localização: %.6f, %.6f", currentLatitude, currentLongitude));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                tvLocalizacao.setText("📍 Permissão de localização negada");
                Toast.makeText(this, "Permita a localização para criar o jogo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
