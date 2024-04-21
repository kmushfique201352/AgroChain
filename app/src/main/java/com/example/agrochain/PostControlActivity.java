package com.example.agrochain;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class PostControlActivity extends AppCompatActivity {

    private ImageView imgHome, imgProfile;
    private Button btnCreatePost;
    private String userID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_control);

        btnCreatePost=findViewById(R.id.btnCreatePost);
        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostControlActivity.this, CreatePostActivity.class);
                intent.putExtra("UserID",userID);
                startActivity(intent);
            }
        });

        imgHome = findViewById(R.id.imgHome);
        imgProfile = findViewById(R.id.imgFarmerProfile);

        userID = getUserID();
        if (userID != null && !userID.isEmpty()) {
            Toast.makeText(this, "UserID: " + userID, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutPostControl);
        swipeRefreshLayout.setOnRefreshListener(this::loadPosts);

        loadPosts();

        imgHome.setOnClickListener(v -> navigateToFarmerDashboardActivity());

        imgProfile.setOnClickListener(v -> navigateToEditProfileActivity(userID));

    }
    private void loadPosts() {
        String userID = getUserID();
        if (userID == null || userID.trim().isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            Log.e("PostControlActivity", "Error: User ID is missing.");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("FAR").document(userID).collection("POST")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LinearLayout postsLayout =   findViewById(R.id.layoutPostsPostControl);
                    postsLayout.removeAllViews(); // Clear previous posts
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        View postView = LayoutInflater.from(PostControlActivity.this).inflate(R.layout.self_item_post, postsLayout, false);

                        TextView tvPostName = postView.findViewById(R.id.tvPostName);
                        TextView tvPostProduct = postView.findViewById(R.id.tvPostProduct);
                        TextView tvPostPrice = postView.findViewById(R.id.tvPostPrice);
                        TextView tvPostQuantity = postView.findViewById(R.id.tvPostQuantity);
                        TextView tvPostReleaseDate = postView.findViewById(R.id.tvPostReleaseDate);
                        TextView tvPostNote = postView.findViewById(R.id.tvPostNote);
                        ImageView imgPostImage = postView.findViewById(R.id.imgPostImage);
                        Button btnEditPost = postView.findViewById(R.id.btnEditPost);
                        Button btnDeletePost = postView.findViewById(R.id.btnDeletePost);

                        tvPostName.setText(document.getString("name"));
                        tvPostProduct.setText(document.getString("product"));
                        tvPostPrice.setText(document.getString("price"));
                        tvPostQuantity.setText(document.getString("quantity"));
                        tvPostReleaseDate.setText(document.getString("releaseDate"));
                        tvPostNote.setText(document.getString("note"));
                        Glide.with(PostControlActivity.this).load(document.getString("imageUrl")).into(imgPostImage);

                        btnEditPost.setOnClickListener(v -> {
                            Intent editIntent = new Intent(PostControlActivity.this, EditPostActivity.class);
                            editIntent.putExtra("postID", document.getId());
                            editIntent.putExtra("userID", userID);
                            startActivity(editIntent);
                        });

                        btnDeletePost.setOnClickListener(v -> {
                            document.getReference().delete().addOnSuccessListener(aVoid -> {
                                Toast.makeText(PostControlActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                loadPosts();
                            }).addOnFailureListener(e -> Toast.makeText(PostControlActivity.this, "Error deleting post", Toast.LENGTH_SHORT).show());
                        });

                        postsLayout.addView(postView);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(PostControlActivity.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void navigateToFarmerDashboardActivity() {
        Intent intent = new Intent(PostControlActivity.this, FarmerDashboardActivity.class);
        intent.putExtra("userID",userID);
        startActivity(intent);
    }

    private void navigateToEditProfileActivity(String userID) {
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(PostControlActivity.this, EditProfileActivity.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    private String getUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }
}