package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.frontend.api.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Map;
import retrofit2.*;

public class CharacterActivity extends AppCompatActivity {

    private LinearLayout layoutSelection, layoutCreation;
    private TextView txtCharName;
    private TextInputEditText etCharName;
    private Spinner spinnerClass;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        userId = getSharedPreferences("APP", MODE_PRIVATE).getString("USER_ID", "");

        layoutSelection = findViewById(R.id.layoutSelection);
        layoutCreation = findViewById(R.id.layoutCreation);
        txtCharName = findViewById(R.id.txtCharName);
        etCharName = findViewById(R.id.etCharName);
        spinnerClass = findViewById(R.id.spinnerClass);
        MaterialButton btnPlay = findViewById(R.id.btnPlay);
        MaterialButton btnCreateCharacter = findViewById(R.id.btnCreateCharacter);

        // Configurar Spinner de Classes
        String[] classes = {"Guerreiro", "Mago", "Arqueiro", "Ladino"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);

        checkExistingCharacter();

        btnCreateCharacter.setOnClickListener(v -> createCharacter());
        btnPlay.setOnClickListener(v -> {
            startActivity(new Intent(CharacterActivity.this, MainActivity.class));
            finish();
        });
    }

    private void checkExistingCharacter() {
        // Simulação de verificação. Em um app real, chamaria a API.
        // Se a API retornar que não tem personagem, mostra layoutCreation.
        // Se retornar que tem, mostra layoutSelection.
        
        boolean hasCharacter = false; // Mudar para true para testar a tela de seleção

        if (hasCharacter) {
            layoutSelection.setVisibility(View.VISIBLE);
            layoutCreation.setVisibility(View.GONE);
            txtCharName.setText("Seu Herói Nível 1");
        } else {
            layoutSelection.setVisibility(View.GONE);
            layoutCreation.setVisibility(View.VISIBLE);
        }
    }

    private void createCharacter() {
        String name = etCharName.getText().toString();
        String charClass = spinnerClass.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Escolha um nome", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("name", name);
        data.put("class", charClass);

        // Chamada fictícia para a API (precisaria estar no ApiService)
        // ApiClient.getApiService().createCharacter(data).enqueue(...)

        Toast.makeText(this, "Personagem " + name + " criado!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}