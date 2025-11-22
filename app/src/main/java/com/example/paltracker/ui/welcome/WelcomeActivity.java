package com.example.paltracker.ui.welcome;

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

import com.example.paltracker.R;
import com.example.paltracker.ui.login.LogInActivity;
import com.example.paltracker.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();

        startAnimations();
        delayAndNavigate();
    }

    private void startAnimations() {
        LinearLayout logoContainer = findViewById(R.id.logoContainer);
        TextView titleText = findViewById(R.id.titleText);
        TextView subtitleText = findViewById(R.id.subtitleText);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        logoContainer.startAnimation(fadeIn);
        titleText.startAnimation(fadeIn);
        subtitleText.startAnimation(fadeIn);
    }

    private void delayAndNavigate() {
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUser, SPLASH_DURATION);
    }

    private void checkUser() {
        if (mAuth.getCurrentUser() != null) {
            navigateTo(MainActivity.class);
        } else {
            navigateTo(LogInActivity.class);
        }
    }

    private void navigateTo(Class<?> target) {
        startActivity(new Intent(WelcomeActivity.this, target));
        finish();
    }
}
