package com.francisco.vibe.Data;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.francisco.vibe.Data.SessionManager;
import com.francisco.vibe.Main;
import com.francisco.vibe.R;

public class AuthActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirm;
    private View registerExtra;
    private boolean isRegister = false;

    private AuthViewModel viewModel;

    /**
     * Inicializa a Activity de autenticação.
     * Se o utilizador já estiver autenticado (sessão ativa), redireciona diretamente para a Main.
     * Caso contrário, configura a interface, inicializa o ViewModel e define os listeners dos botões.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, Main.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);

        viewModel = new AuthViewModel(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);
        registerExtra = findViewById(R.id.registerExtra);

        TextView switchMode = findViewById(R.id.switchMode);
        TextView actionBtn = findViewById(R.id.btnAction);

        switchMode.setOnClickListener(v -> {
            press(v);
            toggleMode(actionBtn);
        });

        actionBtn.setOnClickListener(v -> {
            press(v);
            handleAction();
        });
    }

    /**
     * Aplica uma animação de "press" (scale) e feedback háptico ao elemento clicado,
     * para melhorar a experiência de interação do utilizador.
     */
    private void press(View v) {
        v.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() ->
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                )
                .start();

        v.performHapticFeedback(
                android.view.HapticFeedbackConstants.VIRTUAL_KEY
        );
    }

    /**
     * Faz a transição de texto num TextView com animação de fade-out e fade-in,
     * atualizando o conteúdo de forma suave.
     */
    private void animateTextChange(TextView tv, String text) {
        tv.animate()
                .alpha(0f)
                .setDuration(80)
                .withEndAction(() -> {
                    tv.setText(text);
                    tv.animate().alpha(1f).setDuration(80).start();
                })
                .start();
    }

    /**
     * Alterna entre o modo "Login" e o modo "Registo".
     * No registo, mostra os campos extra (ex: confirmação de password) e altera os textos dos botões.
     * No login, esconde os campos extra e repõe os textos originais.
     */
    private void toggleMode(TextView actionBtn) {
        isRegister = !isRegister;

        if (isRegister) {
            registerExtra.setAlpha(0f);
            registerExtra.setVisibility(View.VISIBLE);
            registerExtra.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(200)
                    .start();

            animateTextChange(actionBtn, "Register");

            animateTextChange(findViewById(R.id.switchMode), "Already have an account? Log in");

        } else {
            registerExtra.animate()
                    .alpha(0f)
                    .translationY(-20)
                    .setDuration(150)
                    .withEndAction(() ->
                            registerExtra.setVisibility(View.GONE)
                    )
                    .start();

            actionBtn.setText("Log in");
            ((TextView) findViewById(R.id.switchMode))
                    .setText("Create an account");
        }
    }

    /**
     * Executa a ação principal consoante o modo atual:
     * - Login: valida campos e tenta autenticar o utilizador.
     * - Registo: valida campos, confirma password e tenta criar um novo utilizador.
     * Em caso de sucesso, guarda a sessão e redireciona para a Main.
     */
    private void handleAction() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            toast("Fill all fields");
            return;
        }

        boolean success;

        if (isRegister) {
            String confirm = etConfirm.getText().toString().trim();
            if (!pass.equals(confirm)) {
                toast("Passwords do not match");
                return;
            }

            success = viewModel.register(user, pass);
            if (!success) {
                toast("User already exists");
                return;
            }
        } else {
            success = viewModel.login(user, pass);
            if (!success) {
                toast("Invalid credentials");
                return;
            }
        }

        SessionManager.login(this, user);
        startActivity(new Intent(this, Main.class));
        finish();
    }

    /**
     * Mostra uma mensagem curta (Toast) ao utilizador.
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
