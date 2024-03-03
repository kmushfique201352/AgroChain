package com.example.agrochain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class FarmerDashboardActivity extends AppCompatActivity {

    private ImageView imgFarmerProfile;
    private FirebaseFirestore db;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        imgFarmerProfile = findViewById(R.id.imgFarmerProfile);
        db = FirebaseFirestore.getInstance();

        userID = getIntent().getStringExtra("userID");

        if (userID != null) {
            imgFarmerProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FarmerDashboardActivity.this, EditProfileActivity.class);
                intent.putExtra("userID", userID);
                Log.d("LoginActivity", "Passing userID: " + userID);

                startActivity(intent);
            });
            Toast.makeText(this, "User:"+userID, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
        }
    }
}
