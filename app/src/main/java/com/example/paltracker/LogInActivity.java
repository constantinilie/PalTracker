package com.example.paltracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
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

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LogInActivity";
    private FirebaseAuth mAuth;                 // exact ca în exemplul tău
    private CredentialManager credentialManager;
    private FirebaseFirestore db;
    private FirestoreUserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_windows); // vezi layout-ul de mai jos

        // Initialize Firebase Auth (exact cum ai cerut)
        mAuth = FirebaseAuth.getInstance();

        // Credential Manager
        credentialManager = CredentialManager.create(this);

        db = FirebaseFirestore.getInstance();

        userManager=new FirestoreUserManager();

        // Buton simplu care pornește login-ul
        LinearLayout signInButton = findViewById(R.id.btnGoogleCustom);
        signInButton.setOnClickListener(v -> startGoogleSignIn());
        TextView signUpButton = findViewById(R.id.signUp);
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        EditText emailInput = findViewById(R.id.loginEmailInput);
        TextView emailWarning = findViewById(R.id.loginEmailWarning);

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();

                if(email.isEmpty()){
                    emailWarning.setVisibility(View.GONE);
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailWarning.setVisibility(View.VISIBLE);

                } else {
                    emailWarning.setVisibility(View.GONE);
                }
            }
        });
    }

    private void LogIn(String email, String password)
    {
        mAuth = FirebaseAuth.getInstance();
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            updateLastLogin(user);
                            Toast.makeText(this, "Autentificare reușită!", Toast.LENGTH_SHORT).show();

                            // mergi în MainActivity
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }

                    } else {
                        Toast.makeText(this,
                                "Autentificare eșuată: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

    }


    private void startGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // fix cum ai dat
                .setServerClientId(getBaseContext().getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Apelăm varianta async (obligatoriu pentru credentials:1.2.x)
        credentialManager.getCredentialAsync(
                /* context */ this,
                /* request */ request,
                /* cancellation */ new CancellationSignal(),
                /* executor */ ContextCompat.getMainExecutor(this),
                /* callback */ new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        Credential credential = response.getCredential();

                        if (credential instanceof CustomCredential) {
                            CustomCredential customCredential = (CustomCredential) credential;
                            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                                GoogleIdTokenCredential googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(customCredential.getData());
                                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
                            } else {
                                Log.w(TAG, "Credential is not of type Google ID!");
                            }
                        } else {
                            Log.w(TAG, "Received non-CustomCredential");
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "getCredentialAsync error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                        Toast.makeText(LogInActivity.this, "Sign-in canceled or failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Trimitem ID token-ul la Firebase
    private void firebaseAuthWithGoogle(@NonNull String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null){
                            userManager.saveUserInFirestore(user);

                        }else{
                            updateUI(null);//updateUI(user);
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    // EXACT cum ai cerut: verificăm Utilizatorul la pornire
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // Poți personaliza UI-ul tău aici
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Salut, " + user.getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LogInActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // user null sau sign-in eșuat
        }
    }



    private void updateLastLogin(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", FieldValue.serverTimestamp());

        db.collection("users")
                .document(user.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(a -> Log.d("FIRESTORE", "Last login updated"))
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error: ", e));
    }
}