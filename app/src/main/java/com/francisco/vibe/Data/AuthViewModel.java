
package com.francisco.vibe.Data;

import android.content.Context;

public class AuthViewModel {

    private final UserDatabase db;

    public AuthViewModel(Context c) {
        db = new UserDatabase(c);
    }

    public boolean login(String user, String pass) {
        return db.login(user, pass);
    }

    public boolean register(String user, String pass) {
        return db.register(user, pass);
    }
}
