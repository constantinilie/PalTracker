package com.example.paltracker.data.auth;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthRepository {

    private final FirebaseAuth auth;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        callback.onSuccess(auth.getCurrentUser());
                    else
                        callback.onFailure(task.getException().getMessage());
                });
    }

    public void loginWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        callback.onSuccess(auth.getCurrentUser());
                    else
                        callback.onFailure(task.getException().getMessage());
                });
    }




    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}
