package com.example.paltracker.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.paltracker.data.user.FirestoreUserManager;
import com.example.paltracker.R;
import com.example.paltracker.ui.login.LogInActivity;
import com.example.paltracker.ui.main.MainActivity;
import com.example.paltracker.util.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class RegisterActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    private FirestoreUserManager userManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_window);

        userManager=new FirestoreUserManager();
        mAuth = FirebaseAuth.getInstance();
        LinearLayout signUpButton = findViewById(R.id.signUpBtn);
        EditText emailInput = findViewById(R.id.registerEmailInput);
        EditText userInput = findViewById(R.id.registerUsernameInput);
        EditText passwordInput = findViewById(R.id.registerPasswordInput);

        signUpButton.setOnClickListener(v -> registerUser(
                emailInput.getText().toString().trim(),
                userInput.getText().toString().trim(),
                passwordInput.getText().toString().trim())
        );

    }

    private void registerUser(String email, String username, String password) {
        //Validari
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Introdu un username!", Toast.LENGTH_SHORT).show();
            return;
        }
        String loginValidation = ValidationUtils.validateLogin(email, password);
        if (loginValidation != null) {
            Toast.makeText(this, loginValidation, Toast.LENGTH_SHORT).show();
            return;
        }

        //Creeare cont Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Eroare la înregistrare: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    // Set displayName
                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {

                        // Salvăm în Firestore (opțional, user nelogat încă)
                        userManager.saveUserInFirestore(user);

                        // Trimitem emailul de verificare
                        user.sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(
                                                this,
                                                "Cont creat! Verifică emailul înainte să te loghezi.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        // Delogăm utilizatorul până verifică emailul
                                        mAuth.signOut();

                                        // Trimite userul la LogInActivity
                                        startActivity(new Intent(this, LogInActivity.class));
                                        finish();

                                    } else {
                                        Toast.makeText(
                                                this,
                                                "Nu am putut trimite emailul de verificare.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });
                    });
                });
    }
}
