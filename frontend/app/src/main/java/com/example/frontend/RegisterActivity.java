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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText username, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.regUsername);
        email = findViewById(R.id.regEmail);
        password = findViewById(R.id.regPassword);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        MaterialButton btnBackToLogin = findViewById(R.id.btnBackToLogin);

        TokenManager.init(this);

        btnRegister.setOnClickListener(v -> register());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String user = username.getText().toString();
        String mail = email.getText().toString();
        String pass = password.getText().toString();

        if (user.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", user);
        data.put("email", mail);
        data.put("password", pass);
        data.put("role", "PLAYER"); // O backend exige que passe a ROLE (Ex: PLAYER, ADMIN)

        ApiClient.getApiService().register(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "Usuário criado com sucesso!", Toast.LENGTH_SHORT).show();
                        
                        // Após criar conta, devolve pra tela de login
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Falha ao criar: " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Erro na resposta do servidor.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}