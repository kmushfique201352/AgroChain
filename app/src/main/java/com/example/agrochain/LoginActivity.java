package com.example.agrochain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUserID, editTextPassword;
    private Button buttonLogin;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserID = findViewById(R.id.editTextUserId);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String userID = editTextUserID.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        String prefix = userID.split("-")[0];

        String collectionPath = mapPrefixToCollection(prefix);

        db.collection(collectionPath).document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String storedPassword = document.getString("password");
                    if (storedPassword != null && storedPassword.equals(password)) {
                        String userType = document.getString("userType");
                        saveUserIDToPrefs(userID);
                        redirectToDashboard(userType, userID);
                    } else {
                        Log.d("Login", "Failed login attempt for userID: " + userID + " with password: " + password);
                        Toast.makeText(LoginActivity.this, "Incorrect password.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("Firestore Error", "Error fetching user details", task.getException());
                Toast.makeText(LoginActivity.this, "Login failed. Please try again later.", Toast.LENGTH_LONG).show();
            }
        });
    }


    private String mapPrefixToCollection(String prefix) {
        switch (prefix) {
            case "FAR":
                return "FAR";
            case "GOV":
                return "GOV";
            case "WHO":
                return "WHO";
            case "CUS":
                return "CUS";
            default:
                return "users";
        }
    }

    private void redirectToDashboard(String userType, String userID) {
        Intent intent=null;
        switch (userType) {
            case "Farmer":
                intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
                break;
            case "Wholesaler":
                intent = new Intent(LoginActivity.this, WholesalerDashboardActivity.class);
                break;
            case "Government Authority":
                intent = new Intent(LoginActivity.this, GovernmentDashboardActivity.class);
                break;
            case "Retailer":
                intent = new Intent(LoginActivity.this, RetailerDashboardActivity.class);
                break;
            case "Customer":
                intent = new Intent(LoginActivity.this, CustomerDashboardActivity.class);
                break;
            default:
                return;
        }
        if (intent != null) {
            intent.putExtra("userID", userID);
            startActivity(intent);
        } else {
            Toast.makeText(LoginActivity.this, "Unknown user type: " + userType, Toast.LENGTH_LONG).show();
        }
        startActivity(intent);
    }
    private void saveUserIDToPrefs(String userID) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserID", userID);
        editor.apply();
    }

}
