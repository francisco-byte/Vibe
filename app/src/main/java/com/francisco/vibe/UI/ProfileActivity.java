package com.francisco.vibe.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.francisco.vibe.Data.AuthActivity;
import com.francisco.vibe.R;
import com.francisco.vibe.Data.SessionManager;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView avatar = findViewById(R.id.imgAvatar);
        TextView username = findViewById(R.id.tvUsername);
        MaterialButton logout = findViewById(R.id.btnLogout);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            v.performHapticFeedback(
                    android.view.HapticFeedbackConstants.VIRTUAL_KEY
            );
            onBackPressed();
        });


        username.setText(SessionManager.getUsername(this));

        logout.setOnClickListener(v -> {
            SessionManager.logout(this);
            startActivity(new Intent(this, AuthActivity.class));
            finishAffinity();
        });
    }
}

