package com.example.agrochain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Request_Donate extends AppCompatActivity {
    private EditText editTextName, editTextQuantity, editTextNote;
    private ImageView imageViewSelect;
    private Uri imageUri;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_donate);

        imageViewSelect = findViewById(R.id.imageViewSelect);
        editTextName = findViewById(R.id.editTextName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextNote = findViewById(R.id.editTextNote);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userID = prefs.getString("UserID", null);

        imageViewSelect.setOnClickListener(v -> selectImage());
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageViewSelect);
        }
    }

    public void submitRequest(View view) {
        if (imageUri == null || editTextName.getText().toString().isEmpty() || editTextQuantity.getText().toString().isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = storageRef.child("images/" + imageUri.getLastPathSegment());
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            saveRequest(imageUrl);
        })).addOnFailureListener(e -> Toast.makeText(Request_Donate.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveRequest(String imageUrl) {
        Map<String, Object> request = new HashMap<>();
        request.put("image", imageUrl);
        request.put("name", editTextName.getText().toString());
        request.put("quantity", editTextQuantity.getText().toString());
        request.put("note", editTextNote.getText().toString());

        if (userID != null) {
            FirebaseFirestore.getInstance()
                    .collection("GOV")
                    .document(userID)
                    .collection("REQUEST")
                    .add(request)
                    .addOnSuccessListener(documentReference -> Toast.makeText(Request_Donate.this, "Request submitted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(Request_Donate.this, "Failed to submit request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
        }
    }
}
