package com.example.agrochain;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FarmerDashboardActivity extends AppCompatActivity {

    private ImageView imgFarmerProfile;
    private FirebaseFirestore db;
    private String userID;
    private ImageView fabCreatePost,imgHome;
    private LinearLayout layoutPosts;
    private FirebaseFirestore firestore;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
            }
        });

        layoutPosts = findViewById(R.id.layoutPosts);
        firestore = FirebaseFirestore.getInstance();

        loadPosts();

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
        fabCreatePost=findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FarmerDashboardActivity.this, PostControlActivity.class);
//                intent.putExtra("UserID",userID);
                startActivity(intent);
            }
        });
    }
    public void saveUserID(String userID) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserID", userID);
        editor.apply();
    }

    private void loadPosts() {
        firestore.collectionGroup("POST")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutPosts.removeAllViews();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(FarmerDashboardActivity.this, "No posts available.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                            DocumentReference userRef = postSnapshot.getReference().getParent().getParent();
                            if (userRef != null) {
                                userRef.get().addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        String userName = userSnapshot.getString("name");
                                        String userAddress = userSnapshot.getString("division") + ", "
                                                + userSnapshot.getString("district") + ", "
                                                + userSnapshot.getString("upazila");
                                        String postDetails = formatPostDetails(postSnapshot,userAddress);
                                        String imageUrl = postSnapshot.getString("imageUrl");
                                        String postDate = postSnapshot.getId();
                                        String userProfileImageUrl = userSnapshot.getString("profilePictureUrl");
                                        addPostView(userName, postDetails, imageUrl,postDate,userProfileImageUrl);
                                    }
                                });
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FarmerDashboardActivity.this, "Error loading posts.", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private String formatPostDetails(QueryDocumentSnapshot postSnapshot, String userAddress) {
        return "Product: " + postSnapshot.getString("product") + "\n" +
                "Price: " + postSnapshot.getString("price") + "\n" +
                "Quantity: " + postSnapshot.getString("quantity") + "\n" +
                "Release Date: " + postSnapshot.getString("releaseDate") + "\n" +
                "Address: " + userAddress + "\n" +
                "Note: " + postSnapshot.getString("note");
    }


    private void addPostView(String userName, String postDetails, String imageUrl, String postDate,String profilePictureUrl) {
        View postView = LayoutInflater.from(this).inflate(R.layout.item_post, layoutPosts, false);
        TextView userNameView = postView.findViewById(R.id.postUserName);
        TextView postDetailsView = postView.findViewById(R.id.postDetails);
        ImageView postImageView = postView.findViewById(R.id.postImage);
        TextView postDateView=postView.findViewById(R.id.postDate);
        de.hdodenhof.circleimageview.CircleImageView userImageView = postView.findViewById(R.id.postUserImage);

        userNameView.setText(userName);
        postDetailsView.setText(postDetails);

        Glide.with(this).load(imageUrl).into(postImageView);

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        try {
            Date date = originalFormat.parse(postDate);
            String formattedDate = targetFormat.format(date);
            postDateView.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            postDateView.setText(postDate);
        }


        postImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));
        postImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(imageUrl).into(postImageView);

        String userProfileImageUrl = profilePictureUrl;
        Glide.with(this).load(userProfileImageUrl).placeholder(R.drawable.default_profile).into(userImageView);
        layoutPosts.addView(postView);
    }


    private String getUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }
}
