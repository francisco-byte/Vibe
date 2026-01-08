package com.francisco.vibe.Data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "vibe_session";
    private static final String KEY_USERNAME = "username";

    /**
     * Obtém a instância de SharedPreferences utilizada
     * para armazenar os dados da sessão do utilizador.
     */
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Guarda os dados da sessão do utilizador após um login bem-sucedido,
     * permitindo manter o utilizador autenticado.
     */
    public static void login(Context c, String username) {
        prefs(c).edit()
                .putString(KEY_USERNAME, username)
                .apply();
    }

    /**
     * Verifica se existe uma sessão ativa,
     * ou seja, se o utilizador se encontra autenticado.
     */
    public static boolean isLoggedIn(Context c) {
        return getUsername(c) != null;
    }

    /**
     * Obtém o nome de utilizador associado à sessão atual.
     */
    public static String getUsername(Context c) {
        return prefs(c).getString(KEY_USERNAME, null);
    }

    /**
     * Termina a sessão do utilizador,
     * removendo todos os dados guardados localmente.
     */
    public static void logout(Context c) {
        prefs(c).edit().clear().apply();
    }
}
