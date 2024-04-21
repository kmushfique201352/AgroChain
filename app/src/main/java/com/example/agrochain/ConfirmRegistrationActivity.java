package com.example.agrochain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ConfirmRegistrationActivity extends AppCompatActivity {

    private TextView textViewUserId;
    private EditText editTextNID, editTextProductDetails, editTextSellSize,
            editTextAdminPosition, editTextSupervisor, editTextPassword, editTextConfirmPassword;
    private Button buttonCompleteRegistration;
    private FirebaseFirestore db;

    private Map<String, String> userData;
    private String userTypePrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_registration);
        db = FirebaseFirestore.getInstance();
        initializeViews();

        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("userData") != null) {
            userData = (HashMap<String, String>) intent.getSerializableExtra("userData");
            userTypePrefix = userData.get("userType").substring(0, 3).toUpperCase();
            setupViewsBasedOnUserType(userTypePrefix);
        }

        String userId = generateUserId(userTypePrefix);
        textViewUserId.setText("User ID: " + userId);

        buttonCompleteRegistration.setOnClickListener(view -> {
            if (validatePasswords()) {
                collectAdditionalData();
                saveUserDetails(userId);
            }
        });
    }

    private void initializeViews() {
        textViewUserId = findViewById(R.id.textViewUserId);
        editTextNID = findViewById(R.id.editTextNID);
        editTextProductDetails = findViewById(R.id.editTextProductDetails);
        editTextSellSize = findViewById(R.id.editTextSellSize);
        editTextAdminPosition = findViewById(R.id.editTextAdminPosition);
        editTextSupervisor = findViewById(R.id.editTextSupervisor);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonCompleteRegistration = findViewById(R.id.buttonCompleteRegistration);
    }
    private void setupViewsBasedOnUserType(String userType) {
        editTextNID.setVisibility(View.GONE);
        editTextProductDetails.setVisibility(View.GONE);
        editTextSellSize.setVisibility(View.GONE);
        editTextAdminPosition.setVisibility(View.GONE);
        editTextSupervisor.setVisibility(View.GONE);

        switch (userType) {
            case "FAR":
                editTextNID.setVisibility(View.VISIBLE);
                editTextProductDetails.setVisibility(View.VISIBLE);
                break;
            case "WHO":
            case "RET":
                editTextNID.setVisibility(View.VISIBLE);
                editTextProductDetails.setVisibility(View.VISIBLE);
                editTextSellSize.setVisibility(View.VISIBLE);
                break;
            case "GOV":
                editTextNID.setVisibility(View.VISIBLE);
                editTextAdminPosition.setVisibility(View.VISIBLE);
                editTextSupervisor.setVisibility(View.VISIBLE);
                break;
            case "CUS":
                break;
            default:
                Toast.makeText(this, "Unknown user type: " + userType, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private String generateUserId(String userTypePrefix) {
        Random random = new Random();
        String randomChars = random.ints(48, 122)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(7)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return userTypePrefix + "-" + randomChars;
    }

    private boolean validatePasswords() {
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void collectAdditionalData() {
        userData.put("password", editTextPassword.getText().toString());
        if (editTextNID.getVisibility() == View.VISIBLE) {
            userData.put("NID", editTextNID.getText().toString());
        }
        if (editTextProductDetails.getVisibility() == View.VISIBLE) {
            userData.put("productDetails", editTextProductDetails.getText().toString());
        }
        if (editTextSellSize.getVisibility() == View.VISIBLE) {
            userData.put("sellSize", editTextSellSize.getText().toString());
        }
        if (editTextAdminPosition.getVisibility() == View.VISIBLE) {
            userData.put("adminPosition", editTextAdminPosition.getText().toString());
        }
        if (editTextSupervisor.getVisibility() == View.VISIBLE) {
            userData.put("supervisor", editTextSupervisor.getText().toString());
        }
        userData.put("profilePictureUrl", "https://example.com/path/to/default/profile/picture.png");
    }

//    private void saveUserDetails(String userId) {
//        String collectionPath = userTypePrefix;
//
//        db.collection(collectionPath).document(userId)
//                .set(userData)
//                .addOnSuccessListener(aVoid -> Toast.makeText(ConfirmRegistrationActivity.this, "User Details Saved Successfully", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(ConfirmRegistrationActivity.this, "Failed to Save User Details", Toast.LENGTH_SHORT).show());
//    }
    private void saveUserDetails(final String userId) {
        String collectionPath = userTypePrefix;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("user_profile_images/" + userId + ".jpg");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                userData.put("profilePictureUrl", imageUrl);

                db.collection(collectionPath).document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ConfirmRegistrationActivity.this, "User Details Saved Successfully with Profile Image", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ConfirmRegistrationActivity.this, "Failed to Save User Details", Toast.LENGTH_SHORT).show());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(ConfirmRegistrationActivity.this, "Failed to upload default profile image", Toast.LENGTH_SHORT).show();
        });
    }

}
