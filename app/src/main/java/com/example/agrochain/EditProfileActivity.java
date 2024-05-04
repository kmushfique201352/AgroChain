package com.example.agrochain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
//FAR-OLpkvxe
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText editTextName, editTextAge, editTextPhone;
    private EditText editTextDivision, editTextDistrict, editTextUpazila, editTextNID;
    private Spinner spinnerGender;
    private Button buttonUpdateProfile, buttonDeleteProfile;
    private Button btnLogout;
    private String userID;
    private ImageView imgEditProfilePicture;
    private Uri selectedImageUri = null;
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                imgEditProfilePicture.setImageURI(uri);
                selectedImageUri = uri;
            });

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
        imgEditProfilePicture = findViewById(R.id.imgEditProfilePicture);

        imgEditProfilePicture.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        if (userID != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
        }
        buttonUpdateProfile.setOnClickListener(v -> {
            uploadNewProfilePicture();
        });
        buttonDeleteProfile.setOnClickListener(v -> deleteUserProfile());

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }


    private void updateUserProfile(String imageUrl) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", editTextName.getText().toString());
        userUpdates.put("age", editTextAge.getText().toString());
        userUpdates.put("phone", editTextPhone.getText().toString());
        userUpdates.put("gender", spinnerGender.getSelectedItem().toString());
        userUpdates.put("division", editTextDivision.getText().toString());
        userUpdates.put("district", editTextDistrict.getText().toString());
        userUpdates.put("upazila", editTextUpazila.getText().toString());
        userUpdates.put("NID", editTextNID.getText().toString());
        if (imageUrl != null) {
            userUpdates.put("profilePictureUrl", imageUrl);
        }

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

                    String imageUrl = document.getString("profilePictureUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).apply(new RequestOptions().override(1024, 768)).into(imgEditProfilePicture);
                    }
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

    private void uploadNewProfilePicture() {
        if (selectedImageUri != null) {
            String fileName = "profile_" + userID + ".jpg";
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + fileName);

            Log.d("EditProfileActivity", "Uploading new profile picture: " + selectedImageUri.toString());

            fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Log.d("EditProfileActivity", "New profile picture uploaded. Image URL: " + imageUrl);
                    updateUserProfile(imageUrl);
                });
            }).addOnFailureListener(e -> {
                Log.e("EditProfileActivity", "Upload failed: " + e.getMessage(), e);
                Toast.makeText(EditProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.d("EditProfileActivity", "No new profile picture selected. Updating profile without changing the image.");
            updateUserProfile(null);
        }
    }

}
