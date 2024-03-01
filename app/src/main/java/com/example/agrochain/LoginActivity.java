package com.example.agrochain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
            if (task.isSuccessful() && task.getResult() != null) {
                String storedPassword = task.getResult().getString("password");
                String userType = task.getResult().getString("userType");

                if (password.equals(storedPassword)) {
                    redirectToDashboard(userType);
                }
//                else {
//                    Toast.makeText(LoginActivity.this, "Error: Incorrect password. Expected userID: " + userID + ", password: " + storedPassword, Toast.LENGTH_LONG).show();
//                }
            } else {
                Toast.makeText(LoginActivity.this, "Error: User not found", Toast.LENGTH_LONG).show();
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
            default:
                return "users";
        }
    }

    private void redirectToDashboard(String userType) {
        Intent intent;
        switch (userType) {
            case "Farmer":
                intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
                break;
            case "Wholesaler":
                intent = new Intent(LoginActivity.this, WholesalerDashboardActivity.class);
                break;
            case "Government Authority":
                intent = new Intent(LoginActivity.this, WholesalerDashboardActivity.class);
                break;
            case "Retailer":
                intent = new Intent(LoginActivity.this, WholesalerDashboardActivity.class);
                break;
            case "Customer":
                intent = new Intent(LoginActivity.this, WholesalerDashboardActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}
