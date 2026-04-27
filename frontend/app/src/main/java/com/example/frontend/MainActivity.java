package com.example.frontend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.frontend.api.ApiClient;
import com.example.frontend.api.ApiResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DrawerLayout drawerLayout;
    private TextView statusText;
    private View statusIndicator;
    private String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        gameId = getIntent().getStringExtra("GAME_ID");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        statusText = findViewById(R.id.statusText);
        statusIndicator = findViewById(R.id.statusIndicator);
        
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
            } else if (id == R.id.nav_games) {
                startActivity(new Intent(this, GameListActivity.class));
            } else if (id == R.id.nav_logout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        // Estilo Dark Mode para o mapa (Opcional, mas combina com seu app)
        // mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        checkLocationPermission();
        
        if (gameId != null) {
            loadThiltapes();
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        moveToCurrentLocation();
    }

    private void moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                }
            });
        }
    }

    private void loadThiltapes() {
        updateStatusUI("BUSCANDO THILTAPES...", android.R.color.holo_blue_light);
        ApiClient.getApiService().getGameCards(gameId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse res = response.body();
                    if (res.isSuccess()) {
                        renderMarkers(res.getData());
                        updateStatusUI("MUNDO ATIVO", android.R.color.holo_green_dark);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                updateStatusUI("ERRO DE REDE", android.R.color.holo_red_dark);
            }
        });
    }

    private void renderMarkers(Object data) {
        if (data == null || mMap == null) return;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(data);
            List<Map<String, Object>> thiltapes = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>(){}.getType());
            
            for (Map<String, Object> t : thiltapes) {
                double lat = Double.parseDouble(String.valueOf(t.get("latitude")));
                double lng = Double.parseDouble(String.valueOf(t.get("longitude")));
                String name = String.valueOf(t.get("name"));

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro markers: " + e.getMessage());
        }
    }

    private void logout() {
        getSharedPreferences("thiltapes_prefs", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateStatusUI(String message, int colorRes) {
        if (statusText != null) statusText.setText(message);
        if (statusIndicator != null) statusIndicator.setBackgroundColor(getColor(colorRes));
    }
}
