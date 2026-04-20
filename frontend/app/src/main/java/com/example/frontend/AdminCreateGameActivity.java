package com.example.frontend;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCreateGameActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private TextInputEditText etNomeJogo;
    private TextInputEditText etNumeroCartas;
    private TextInputEditText etRaio;
    private TextView tvLocalizacao;

    private double currentLat = 0;
    private double currentLng = 0;
    private boolean locationObtained = false;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_game);

        etNomeJogo = findViewById(R.id.etNomeJogo);
        etNumeroCartas = findViewById(R.id.etNumeroCartas);
        etRaio = findViewById(R.id.etRaio);
        tvLocalizacao = findViewById(R.id.tvLocalizacao);
        MaterialButton btnObterLocalizacao = findViewById(R.id.btnObterLocalizacao);
        MaterialButton btnCriarJogo = findViewById(R.id.btnCriarJogo);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnObterLocalizacao.setOnClickListener(v -> obterLocalizacao());
        btnCriarJogo.setOnClickListener(v -> criarJogo());
    }

    private void obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        tvLocalizacao.setText("📍 Obtendo localização...");

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        locationObtained = true;
                        tvLocalizacao.setText("📍 Localização obtida: " +
                                String.format("%.5f, %.5f", currentLat, currentLng));
                    } else {
                        tvLocalizacao.setText("📍 Não foi possível obter localização. Tente usar o Maps do Emulador.");
                        Toast.makeText(this, "Dica: No Emulador, clique nos (...) > Location > Set Location", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    tvLocalizacao.setText("📍 Erro ao buscar GPS.");
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacao();
        } else {
            Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show();
        }
    }

    private void criarJogo() {
        String nome = etNomeJogo.getText().toString().trim();
        String numeroCartasStr = etNumeroCartas.getText().toString().trim();
        String raioStr = etRaio.getText().toString().trim();

        if (nome.isEmpty()) {
            etNomeJogo.setError("Informe o nome do jogo");
            return;
        }
        if (numeroCartasStr.isEmpty()) {
            etNumeroCartas.setError("Informe o número de cartas");
            return;
        }
        if (raioStr.isEmpty()) {
            etRaio.setError("Informe o raio em metros");
            return;
        }
        if (!locationObtained) {
            Toast.makeText(this, "Obtenha a localização primeiro!", Toast.LENGTH_SHORT).show();
            return;
        }

        String adminId = TokenManager.getInstance().getUserId();

        Map<String, Object> currentLocation = new HashMap<>();
        currentLocation.put("lat", currentLat);
        currentLocation.put("lng", currentLng);

        Map<String, Object> data = new HashMap<>();
        data.put("name", nome);
        data.put("admin_id", adminId);
        data.put("number_of_cards", Integer.parseInt(numeroCartasStr));
        data.put("radius", Integer.parseInt(raioStr));
        data.put("current_location", currentLocation);

        ApiClient.getApiService().createGame(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(AdminCreateGameActivity.this,
                                "Jogo criado com sucesso!", Toast.LENGTH_SHORT).show();
                        etNomeJogo.setText("");
                        etNumeroCartas.setText("");
                        etRaio.setText("");
                        tvLocalizacao.setText("📍 Localização: aguardando GPS...");
                        locationObtained = false;
                    } else {
                        Toast.makeText(AdminCreateGameActivity.this,
                                "Erro: " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminCreateGameActivity.this,
                            "Erro na resposta do servidor.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(AdminCreateGameActivity.this,
                        "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}