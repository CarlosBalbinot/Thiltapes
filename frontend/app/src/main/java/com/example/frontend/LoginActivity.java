package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.api.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.*;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText username;
    private TextInputEditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username.getText().toString());
        data.put("password", password.getText().toString());

        ApiClient.getApiService().login(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    if(response.body().isSuccess()){
                        String userId = String.valueOf(response.body().getData());

                        getSharedPreferences("APP", MODE_PRIVATE)
                                .edit()
                                .putString("USER_ID", userId)
                                .apply();

                        Toast.makeText(LoginActivity.this,
                                "Login OK", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish(); // Fecha a tela de login para não voltar nela ao apertar "voltar"
                    } else {
                        Toast.makeText(LoginActivity.this,
                                response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}