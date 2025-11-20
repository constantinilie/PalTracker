package com.example.paltracker;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FirestoreUserManager {

    private FirebaseFirestore db;

    public FirestoreUserManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveUserInFirestore(FirebaseUser firebaseUser ) {

        String uid = firebaseUser.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("displayName", firebaseUser.getDisplayName());
        userData.put("email", firebaseUser.getEmail());
        if (firebaseUser.getPhotoUrl() != null) {
            userData.put("photoUrl", firebaseUser.getPhotoUrl().toString());
        }
        userData.put("lastLogin", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(a -> Log.d("FIRESTORE", "Last login updated"))
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error: ", e));
    }
}

