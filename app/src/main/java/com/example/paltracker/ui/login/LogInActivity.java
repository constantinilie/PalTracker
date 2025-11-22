package com.example.paltracker.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.paltracker.R;
import com.example.paltracker.data.auth.AuthRepository;
import com.example.paltracker.data.user.FirestoreUserManager;
import com.example.paltracker.ui.main.MainActivity;
import com.example.paltracker.ui.register.RegisterActivity;
import com.example.paltracker.util.ValidationUtils;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {

    private CredentialManager credentialManager;

    private AuthRepository authRepository;
    private FirestoreUserManager firestoreUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_windows);

        credentialManager = CredentialManager.create(this);
        authRepository = new AuthRepository();
        firestoreUserManager = new FirestoreUserManager();

        EditText email = findViewById(R.id.loginEmailInput);
        EditText password = findViewById(R.id.loginPasswordInput);

        LinearLayout googleBtn = findViewById(R.id.btnGoogleCustom);
        LinearLayout emailBtn = findViewById(R.id.signInBtn);
        TextView registerBtn = findViewById(R.id.signUp);

        // Google Sign-In
        googleBtn.setOnClickListener(v -> startGoogleSignIn());

        // Email login
        emailBtn.setOnClickListener(v -> {
            String err = ValidationUtils.validateLogin(
                    email.getText().toString().trim(),
                    password.getText().toString().trim()
            );
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                return;
            }

            loginWithEmail(email.getText().toString(), password.getText().toString());
        });

        // Register button
        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void loginWithEmail(String email, String password) {
        authRepository.loginWithEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                firestoreUserManager.updateLastLogin(user);
                if (!user.isEmailVerified()) {
                    Toast.makeText(LogInActivity.this,
                            "Emailul nu este verificat! Verifică emailul mai întâi.",
                            Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    return;
                }
                openMain();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LogInActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse response) {
                        Credential credential = response.getCredential();


                        if (credential instanceof CustomCredential) {
                            CustomCredential customCredential = (CustomCredential) credential;

                            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    .equals(customCredential.getType())) {

                                GoogleIdTokenCredential googleCred =
                                        GoogleIdTokenCredential.createFrom(customCredential.getData());

                                authRepository.loginWithGoogle(googleCred.getIdToken(),
                                        new AuthRepository.AuthCallback() {
                                            @Override
                                            public void onSuccess(FirebaseUser user) {
                                                firestoreUserManager.updateLastLogin(user);
                                                openMain();
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Toast.makeText(LogInActivity.this, error, Toast.LENGTH_LONG).show();
                                            }
                                        });

                            } else {
                                Toast.makeText(LogInActivity.this,
                                        "Credential invalid (nu este Google ID token)",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LogInActivity.this,
                                    "Tip de credential necunoscut",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Toast.makeText(LogInActivity.this,
                                "Eroare Google Sign-In: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) openMain();
    }
}
