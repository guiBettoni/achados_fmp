package com.example.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.MainActivity;
import com.example.R;
import com.example.data.SessionManager;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        findViewById(R.id.btnGoogleLoginCard).setOnClickListener(v -> {
            sessionManager.createLoginSession(
                    "Guilherme Bettoni",
                    "guilherme@" + SessionManager.EMAIL_DOMAIN,
                    "Estudante da FMP • Matrícula [PREENCHER: matrícula]"
            );
            Toast.makeText(this, "Conectado com conta institucional FMP", Toast.LENGTH_SHORT).show();
            proceedToMain();
        });

        MaterialButton btnGuest = findViewById(R.id.btnGuestLogin);
        if (btnGuest != null) {
            btnGuest.setOnClickListener(v -> {
                sessionManager.createLoginSession(
                        "Visitante da FMP",
                        "visitante@" + SessionManager.EMAIL_DOMAIN,
                        "Comunidade / Visitante"
                );
                Toast.makeText(this, "Acessando como Visitante", Toast.LENGTH_SHORT).show();
                proceedToMain();
            });
        }

        MaterialButton btnSecretaria = findViewById(R.id.btnSecretariaLogin);
        if (btnSecretaria != null) {
            btnSecretaria.setOnClickListener(v -> {
                sessionManager.createLoginSession(
                        "Secretaria Acadêmica",
                        "secretaria@" + SessionManager.EMAIL_DOMAIN,
                        "SECRETARIA"
                );
                Toast.makeText(this, "Conectado como Secretaria Acadêmica", Toast.LENGTH_SHORT).show();
                proceedToMain();
            });
        }
    }

    private void proceedToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
