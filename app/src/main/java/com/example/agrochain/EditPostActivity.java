package com.example.agrochain;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.agrochain.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {

    private EditText editPostName, editPostProduct, editPostPrice, editPostQuantity, editPostReleaseDate, editPostNote;
    private ImageView editPostImage;
    private Button btnSubmitEdit;
    private FirebaseFirestore db;
    private String newImageUrl = null;
    private String postID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        db = FirebaseFirestore.getInstance();

        editPostName = findViewById(R.id.editPostName);
        editPostProduct = findViewById(R.id.editPostProduct);
        editPostPrice = findViewById(R.id.editPostPrice);
        editPostQuantity = findViewById(R.id.editPostQuantity);
        editPostReleaseDate = findViewById(R.id.editPostReleaseDate);
        editPostNote = findViewById(R.id.editPostNote);
        editPostImage = findViewById(R.id.editPostImage);
        btnSubmitEdit = findViewById(R.id.btnSubmitEdit);

        String userID = getIntent().getStringExtra("userID");
        String postID = getIntent().getStringExtra("postID");

        if (postID != null && userID != null) {
            loadPostDetails(userID, postID);
        } else {
            Toast.makeText(this, "Error: Post ID or User ID not provided", Toast.LENGTH_SHORT).show();
        }

        btnSubmitEdit.setOnClickListener(view -> {
            updatePost(userID, postID);
        });

    }

    private void loadPostDetails(String userID, String postID) {
        if (userID == null || userID.trim().isEmpty() ||
                postID == null || postID.trim().isEmpty()) {
            Toast.makeText(this, "Error: Missing required information.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("FAR")
                .document(userID)
                .collection("POST")
                .document(postID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editPostName.setText(documentSnapshot.getString("name"));
                        editPostProduct.setText(documentSnapshot.getString("product"));
                        editPostPrice.setText(documentSnapshot.getString("price"));
                        editPostQuantity.setText(documentSnapshot.getString("quantity"));
                        editPostReleaseDate.setText(documentSnapshot.getString("releaseDate"));
                        editPostNote.setText(documentSnapshot.getString("note"));
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(EditPostActivity.this).load(imageUrl).into(editPostImage);
                        }
                    } else {
                        Toast.makeText(EditPostActivity.this, "Post not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditPostActivity.this, "Error loading post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    private void updatePost(String userID, String postID) {
        if (userID == null || userID.trim().isEmpty() || postID == null || postID.trim().isEmpty()) {
            Toast.makeText(this, "Error: Missing required information.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", editPostName.getText().toString());
        updatedData.put("product", editPostProduct.getText().toString());
        updatedData.put("price", editPostPrice.getText().toString());
        updatedData.put("quantity", editPostQuantity.getText().toString());
        updatedData.put("releaseDate", editPostReleaseDate.getText().toString());
        updatedData.put("note", editPostNote.getText().toString());
        if (newImageUrl != null && !newImageUrl.isEmpty()) {
            updatedData.put("imageUrl", newImageUrl);
        }

        db.collection("FAR").document(userID).collection("POST").document(postID)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPostActivity.this, "Post updated successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(EditPostActivity.this, "Error updating post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
