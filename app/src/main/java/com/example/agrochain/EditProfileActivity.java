package com.example.agrochain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText editTextName, editTextAge, editTextPhone;
    private EditText editTextDivision, editTextDistrict, editTextUpazila, editTextNID;
    private Spinner spinnerGender;
    private Button buttonUpdateProfile, buttonDeleteProfile;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userID = getIntent().getStringExtra("userID");
        Log.d("EditProfileActivity", "Received userID: " + userID);


        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextDivision = findViewById(R.id.editTextDivision);
        editTextDistrict = findViewById(R.id.editTextDistrict);
        editTextUpazila = findViewById(R.id.editTextUpazila);
        editTextNID = findViewById(R.id.editTextNID);
        spinnerGender = findViewById(R.id.spinnerGender);
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);
        buttonDeleteProfile = findViewById(R.id.buttonDeleteProfile);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        if (userID != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
        }
        buttonUpdateProfile.setOnClickListener(v -> updateUserProfile());
        buttonDeleteProfile.setOnClickListener(v -> deleteUserProfile());
    }

    private void updateUserProfile() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", editTextName.getText().toString());
        userUpdates.put("age", editTextAge.getText().toString());
        userUpdates.put("phone", editTextPhone.getText().toString());
        // Add other fields
        userUpdates.put("gender", spinnerGender.getSelectedItem().toString());
        userUpdates.put("division", editTextDivision.getText().toString());
        userUpdates.put("district", editTextDistrict.getText().toString());
        userUpdates.put("upazila", editTextUpazila.getText().toString());
        userUpdates.put("NID", editTextNID.getText().toString());

        db.collection("FAR").document(userID).update(userUpdates).addOnSuccessListener(aVoid -> {
            Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void deleteUserProfile() {
        db.collection("FAR").document(userID).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(EditProfileActivity.this, "Profile Deleted Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditProfileActivity.this, FarmerDashboardActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to delete profile", Toast.LENGTH_SHORT).show());
    }

    private void loadUserData() {
        db.collection("FAR").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {

                    editTextName.setText(document.getString("name"));
                    editTextAge.setText(document.getString("age"));
                    editTextPhone.setText(document.getString("phone"));
                    editTextDivision.setText(document.getString("division"));
                    editTextDistrict.setText(document.getString("district"));
                    editTextUpazila.setText(document.getString("upazila"));
                    editTextNID.setText(document.getString("NID"));

                    setSpinnerSelection(spinnerGender, document.getString("gender"));

                } else {
                    Toast.makeText(EditProfileActivity.this, "No such user", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        if (value != null) {
            int position = adapter.getPosition(value);
            spinner.setSelection(position);
        }
    }
}
