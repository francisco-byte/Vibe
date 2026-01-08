package com.francisco.vibe.Data;

import android.content.Context;

public class AuthViewModel {

    private final UserDatabase db;

    /**
     * Construtor do AuthViewModel.
     * Inicializa a ligação à base de dados de utilizadores.
     */
    public AuthViewModel(Context c) {
        db = new UserDatabase(c);
    }

    /**
     * Realiza o processo de autenticação do utilizador,
     * validando as credenciais através da base de dados.
     */
    public boolean login(String user, String pass) {
        return db.login(user, pass);
    }

    /**
     * Realiza o registo de um novo utilizador na aplicação,
     * guardando os seus dados na base de dados caso ainda não exista.
     */
    public boolean register(String user, String pass) {
        return db.register(user, pass);
    }
}
