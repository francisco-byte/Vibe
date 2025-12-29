package com.francisco.vibe.Data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "vibe_session";
    private static final String KEY_USERNAME = "username";

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void login(Context c, String username) {
        prefs(c).edit()
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public static boolean isLoggedIn(Context c) {
        return getUsername(c) != null;
    }

    public static String getUsername(Context c) {
        return prefs(c).getString(KEY_USERNAME, null);
    }

    public static void logout(Context c) {
        prefs(c).edit().clear().apply();
    }
}
