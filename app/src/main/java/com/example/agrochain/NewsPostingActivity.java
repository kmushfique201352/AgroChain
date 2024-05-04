package com.example.agrochain;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewsPostingActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private EditText titleEditText, contentEditText;
    private EditText link;
    private ImageView imagePreview;
    private Button uploadImageButton, postButton;
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private LinearLayout newsPostsContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String currentEditingDocumentId = null;
    private String existingImageUrl = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_posting);

        newsPostsContainer = findViewById(R.id.newsPostsContainer);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        firestore = FirebaseFirestore.getInstance();

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        link = findViewById(R.id.linkEditText);
        imagePreview = findViewById(R.id.imagePreview);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        postButton = findViewById(R.id.postButton);

        progressDialog = new ProgressDialog(this);

        ActivityResultLauncher<Intent> imagePickerResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            Glide.with(NewsPostingActivity.this).load(imageUri).into(imagePreview);
                        }
                    }
                });

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerResultLauncher.launch(intent);
        });

        postButton.setOnClickListener(v -> {
            if (!titleEditText.getText().toString().isEmpty() && !contentEditText.getText().toString().isEmpty() && !link.getText().toString().isEmpty()) {
                if (currentEditingDocumentId != null && imageUri == null) {
                    updateNewsPost(currentEditingDocumentId, titleEditText.getText().toString(), contentEditText.getText().toString(), link.getText().toString(), null);
                } else if (imageUri != null) {
                    uploadImage(currentEditingDocumentId);
                } else {
                    Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });



        swipeRefreshLayout.setOnRefreshListener(this::loadNews);
        loadNews();

    }
    private void uploadImage(@Nullable String documentId) {
        if (imageUri != null) {
            progressDialog.setMessage("Uploading...");
            progressDialog.show();
            String imagePath = "news_images/" + (documentId == null ? System.currentTimeMillis() : documentId) + ".jpg";
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(imagePath);

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        postOrUpdateNews(documentId, titleEditText.getText().toString(), contentEditText.getText().toString(), link.getText().toString(), imageUrl);
                        imageUri = null;
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewsPostingActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    });
        } else if (documentId != null && existingImageUrl != null) {
            postOrUpdateNews(documentId, titleEditText.getText().toString(), contentEditText.getText().toString(), link.getText().toString(), existingImageUrl);
        } else {
            Toast.makeText(this, "Please select an image for the new post.", Toast.LENGTH_SHORT).show();
        }
    }






    private void postOrUpdateNews(@Nullable String documentId, String title, String content, String link, String imageUrl) {
        Map<String, Object> news = new HashMap<>();
        news.put("title", title);
        news.put("content", content);
        news.put("link", link);
        news.put("imageUrl", imageUrl);
        news.put("date", new Timestamp(new Date()));

        if (documentId == null) {
            firestore.collection("GOV").document(getUserID()).collection("NEWS")
                    .add(news)
                    .addOnSuccessListener(documentReference -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "News posted successfully", Toast.LENGTH_SHORT).show();
                        resetEditingState();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error posting news: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            firestore.collection("GOV").document(getUserID()).collection("NEWS").document(documentId)
                    .set(news)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "News updated successfully", Toast.LENGTH_SHORT).show();
                        resetEditingState();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateNewsPost(String documentId, String title, String content, String link, @Nullable String imageUrl) {
        Map<String, Object> news = new HashMap<>();
        news.put("title", title);
        news.put("content", content);
        news.put("link", link);
        news.put("imageUrl", imageUrl != null ? imageUrl : existingImageUrl);
        news.put("date", new Timestamp(new Date()));

        firestore.collection("GOV").document(getUserID()).collection("NEWS").document(documentId)
                .set(news)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Post updated successfully.", Toast.LENGTH_SHORT).show();
                    resetEditingState();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void loadNews() {
        swipeRefreshLayout.setRefreshing(true);
        firestore.collectionGroup("NEWS")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        newsPostsContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            addNewsToLayout(document);
                        }
                    } else {
                        Toast.makeText(NewsPostingActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void addNewsToLayout(QueryDocumentSnapshot document) {
        View newsView = LayoutInflater.from(this).inflate(R.layout.news_item_layout, newsPostsContainer, false);

        ImageView imageView = newsView.findViewById(R.id.imageViewNews);
        TextView textViewTitle = newsView.findViewById(R.id.textViewTitle);
        TextView textViewContent = newsView.findViewById(R.id.textViewContent);
        Button editButton = newsView.findViewById(R.id.editButton);
        Button deleteButton = newsView.findViewById(R.id.deleteButton);

        textViewTitle.setText(document.getString("title"));
        textViewContent.setText(document.getString("content"));
        Glide.with(this).load(document.getString("imageUrl")).into(imageView);

        editButton.setOnClickListener(v -> editNewsPost(document));
        deleteButton.setOnClickListener(v -> deleteNewsPost(document.getId()));

        newsPostsContainer.addView(newsView);
    }
    private void deleteNewsPost(String documentId) {
        firestore.collection("GOV").document(getUserID()).collection("NEWS").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(NewsPostingActivity.this, "Post deleted successfully.", Toast.LENGTH_SHORT).show();
                    loadNews();
                })
                .addOnFailureListener(e -> Toast.makeText(NewsPostingActivity.this, "Failed to delete post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void editNewsPost(QueryDocumentSnapshot document) {
        currentEditingDocumentId = document.getId();

        existingImageUrl = document.getString("imageUrl");
        Glide.with(this).load(existingImageUrl).into(imagePreview);

        titleEditText.setText(document.getString("title"));
        contentEditText.setText(document.getString("content"));
        link.setText(document.getString("link"));

        postButton.setText("Update Post");
    }




    private void deleteAndRepostNews(String documentId, String title, String content, String link) {
        firestore.collection("GOV").document(getUserID()).collection("NEWS").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(NewsPostingActivity.this, "Original post deleted, posting updated news.", Toast.LENGTH_SHORT).show();
                    uploadImage(null);
                })
                .addOnFailureListener(e -> Toast.makeText(NewsPostingActivity.this, "Failed to delete original post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void resetEditingState() {
        currentEditingDocumentId = null;
        postButton.setText("Post News");
        titleEditText.setText("");
        contentEditText.setText("");
        link.setText("");
        imagePreview.setImageDrawable(null);
    }


    private String getUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }
}
