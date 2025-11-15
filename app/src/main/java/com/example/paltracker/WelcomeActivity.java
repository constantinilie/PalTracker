package com.example.paltracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2500;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        LinearLayout logoContainer = findViewById(R.id.logoContainer);
        TextView titleText = findViewById(R.id.titleText);
        TextView subtitleText = findViewById(R.id.subtitleText);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoContainer.startAnimation(fadeIn);
        titleText.startAnimation(fadeIn);
        subtitleText.startAnimation(fadeIn);

        mAuth = FirebaseAuth.getInstance();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUser();
        }, SPLASH_DURATION);
    }

    private void checkUser() {
        if (mAuth.getCurrentUser() != null) {
            // Utilizatorul este deja logat
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Utilizator nou / delogat → trimite-l la login
            Intent intent = new Intent(WelcomeActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
