package com.example.paltracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class RegisterActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    private FirestoreUserManager userManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userManager=new FirestoreUserManager();
        mAuth = FirebaseAuth.getInstance();
        LinearLayout signUpButton = findViewById(R.id.signUpBtn);
        EditText email = findViewById(R.id.registerEmailInput);
        EditText user = findViewById(R.id.registerUsernameInput);
        EditText password = findViewById(R.id.registerPasswordInput);

        signUpButton.setOnClickListener(v -> registerUser(email.getText().toString().trim(),user.getText().toString().trim(),password.getText().toString().trim()));



    }

    private void registerUser(String email, String name, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {


                                        userManager.saveUserInFirestore(user);

                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        Toast.makeText(this,
                                "Eroare la înregistrare: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
