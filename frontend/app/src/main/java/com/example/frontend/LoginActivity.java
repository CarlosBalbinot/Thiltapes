package com.example.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
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
    private RadioGroup rgUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        rgUserType = findViewById(R.id.rgUserType);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoToRegister = findViewById(R.id.btnGoToRegister);

        TokenManager.init(this);

        btnLogin.setOnClickListener(v -> login());
        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void login() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username.getText().toString());
        data.put("password", password.getText().toString());

        // Pega o papel selecionado
        boolean isAdmin = rgUserType.getCheckedRadioButtonId() == R.id.rbAdmin;

        ApiClient.getApiService().login(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        String userId = response.body().getUserId();
                        String token = response.body().getToken();

                        TokenManager.getInstance().saveToken(token);
                        TokenManager.getInstance().saveUserId(userId);

                        // Salvar o papel do usuário
                        SharedPreferences prefs = getSharedPreferences("thiltapes_prefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("is_admin", isAdmin).apply();

                        Toast.makeText(LoginActivity.this,
                                "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();

                        // Redirecionar conforme papel
                        if (isAdmin) {
                            startActivity(new Intent(LoginActivity.this, AdminCreateGameActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, PlayerHomeActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Falha no login: " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Erro na resposta do servidor.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}