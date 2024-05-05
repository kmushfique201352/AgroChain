package com.example.agrochain;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomerDashboardActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private LinearLayout layoutPosts;
    private String userID;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        userID = getIntent().getStringExtra("userID");
        if (userID != null) {
            listenForNotifications(userID);
            Toast.makeText(this, "User:"+userID, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
        }

        firestore = FirebaseFirestore.getInstance();
        layoutPosts = findViewById(R.id.layoutPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this::loadPosts);
        loadPosts();
    }

    private void loadPosts() {
        firestore.collectionGroup("POST")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutPosts.removeAllViews();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(CustomerDashboardActivity.this, "No posts available.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                            DocumentReference userRef = postSnapshot.getReference().getParent().getParent();
                            if (userRef != null) {
                                String userIDFar=userRef.getId();
                                userRef.get().addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        addPostView(postSnapshot,userIDFar);
                                    }
                                });
                            }
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CustomerDashboardActivity.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }


    private void listenForNotifications(String userId) {
        FirebaseFirestore.getInstance()
                .collection("CUS")
                .document(userId)
                .collection("NOTIFICATION")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Firestore", "Listen failed.", e);
                            return;
                        }

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            if (doc.exists()) {

                                showNotificationPopup(doc);
                            }
                        }
                    }
                });
    }

    private void showNotificationPopup(DocumentSnapshot document) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Notification Details");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 50, 50, 50);

            String imageUrl = document.getString("image");
            ImageView imageView = new ImageView(this);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .override(600)
                        .centerCrop()
                        .into(imageView);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));
                layout.addView(imageView);
            }

            addTextView(layout, "Address: ", document.getString("address"));
            addTextView(layout, "Location: ", document.getString("location"));
            addTextView(layout, "Price: ", safeDoubleToString(document, "price"));
            addTextView(layout, "Quantity: ", safeLongToString(document, "quantity"));
            addTextView(layout, "Total Cost: ", document.getString("totalCost"));

            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, id) -> {
                dialog.dismiss();
                deleteNotification(document);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("PopupError", "Error showing notification popup", e);
            Toast.makeText(this, "Failed to display notification details.", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteNotification(DocumentSnapshot document) {
        if (document.getReference() != null) {
            document.getReference().delete()
                    .addOnSuccessListener(aVoid -> Log.d("DeleteNotification", "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w("DeleteNotification", "Error deleting document", e));
        }
    }
    private String safeDoubleToString(DocumentSnapshot document, String field) {
        try {
            Double value = document.getDouble(field);
            return value != null ? String.format("%.2f", value) : "N/A";
        } catch (RuntimeException ex) {
            Log.e("Firestore Conversion", "Field '" + field + "' is not a Double.", ex);
            return "Invalid data";
        }
    }


    private String safeLongToString(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value != null ? String.valueOf(value) : "N/A";
    }

    private void addTextView(LinearLayout layout, String prefix, String text) {
        TextView textView = new TextView(this);
        textView.setText(prefix + (text != null ? text : "Not available"));
        layout.addView(textView);
    }





    private void addPostView(QueryDocumentSnapshot postSnapshot,String userIDFar) {
        View postView = LayoutInflater.from(this).inflate(R.layout.item_post_customer, layoutPosts, false);
        TextView detailsView = postView.findViewById(R.id.postDetails);
        ImageView imageView = postView.findViewById(R.id.postImage);
        Button buyButton = postView.findViewById(R.id.buyButton);

        String postDetails = "com.example.agrochain.Product: " + postSnapshot.getString("product") + "\n" +
                "Price: " + postSnapshot.getString("price");
        detailsView.setText(postDetails);

        Glide.with(this).load(postSnapshot.getString("imageUrl")).into(imageView);

        buyButton.setOnClickListener(v -> {
            showPurchaseOptions(postSnapshot.getId(),userIDFar);
        });

        layoutPosts.addView(postView);
    }

    private void showPurchaseOptions(String postId, String userIDFar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.popup_order_options, null);
        builder.setView(dialogView);

        ImageView productImageView = dialogView.findViewById(R.id.productImageView);
        TextView productNameTextView = dialogView.findViewById(R.id.productNameTextView);
        TextView priceTextView = dialogView.findViewById(R.id.priceTextView);
        TextView releaseDateTextView = dialogView.findViewById(R.id.releaseDateTextView);
        TextView noteTextView = dialogView.findViewById(R.id.noteTextView);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirmPurchase = dialogView.findViewById(R.id.buttonConfirmPurchase);

        AlertDialog dialog = builder.create();

        FirebaseFirestore.getInstance().collection("FAR").document(userIDFar)
                .get().addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.exists()) {
                        String userName=userSnapshot.getString("name");
                        String userAddress=userSnapshot.getString("division")+", "+userSnapshot.getString("district")+","+userSnapshot.getString("upazila");
                        String phoneNumber=userSnapshot.getString("phone");

                        userSnapshot.getReference().collection("POST").document(postId)
                                .get().addOnSuccessListener(documentSnapshot -> {
                                    if(documentSnapshot.exists()){
                                        String imageUrl = documentSnapshot.getString("imageUrl");
                                        String product = documentSnapshot.getString("product");
                                        String price = documentSnapshot.getString("price");
                                        String releaseDate = documentSnapshot.getString("releaseDate");
                                        String note = documentSnapshot.getString("note");

                                        Glide.with(this).load(imageUrl).into(productImageView);
                                        productNameTextView.setText(product);
                                        priceTextView.setText("Price: " + price);
                                        releaseDateTextView.setText("Release Date: " + releaseDate);
                                        noteTextView.setText("Note: " + note);

                                        buttonConfirmPurchase.setOnClickListener(v -> {
                                            Intent intent = new Intent(CustomerDashboardActivity.this, PurchaseActivity.class);
                //                          Far
                                            intent.putExtra("imageUrl", imageUrl);
                                            intent.putExtra("price", price);
                                            intent.putExtra("quantity", documentSnapshot.getString("quantity"));
                                            intent.putExtra("userName", userName);
                                            intent.putExtra("userAddress", userAddress);
                                            intent.putExtra("phoneNumber", phoneNumber);
                                            intent.putExtra("userIDFar", userIDFar);
                //                          Cus
                                            intent.putExtra("userID", userID);
                                            intent.putExtra("userType", "CUS");

                                            startActivity(intent);
                                            dialog.dismiss();
                                        });
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Product details not found.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load product details.", Toast.LENGTH_SHORT).show();
                });

        buttonCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Purchase canceled.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }



    private void sendNotificationToFarmer(String postId, boolean isHomeDelivery, String address, int quantity, Date pickUpDate, String userID) {
        String customerUserID = getIntent().getStringExtra("userID");

        FirebaseFirestore pro = FirebaseFirestore.getInstance();
        DocumentReference productRef = pro.collection("FAR").document(userID).collection("POST").document(postId);

        FirebaseFirestore.getInstance().collection("CUS").document(customerUserID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String customerName = documentSnapshot.getString("name");
                        String phoneNumber = documentSnapshot.getString("phone");

                        productRef.get().addOnSuccessListener(productSnapshot -> {
                            if (productSnapshot.exists()) {
                                String productName = productSnapshot.getString("name");

                                Map<String, Object> notificationData = new HashMap<>();
                                notificationData.put("type", isHomeDelivery ? "Home Delivery" : "Pick Up");
                                notificationData.put("address", address);
                                notificationData.put("status", "pending");
                                notificationData.put("phone", phoneNumber);
//                                notificationData.put("weight", weight);
                                notificationData.put("quantity", quantity+" KG");
                                if (!isHomeDelivery && pickUpDate != null) {
                                    notificationData.put("pickUpDate", pickUpDate);
                                }
                                notificationData.put("productName", productName);
                                notificationData.put("customerName", customerName);
                                notificationData.put("timestamp", new Timestamp(new Date()));

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("FAR").document(userID).collection("NOTIFICATION").add(notificationData)
                                        .addOnSuccessListener(documentReference -> Toast.makeText(CustomerDashboardActivity.this, "Order placed successfully.", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(CustomerDashboardActivity.this, "Order failed to place.", Toast.LENGTH_SHORT).show();
                                            Log.e("NotificationActivity", "Order placement failed", e);
                                        });
                            } else {
                                Toast.makeText(this, "Product details not found.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Failed to fetch customer details.", Toast.LENGTH_SHORT).show();
                        Log.e("NotificationActivity", "No document for customer details found");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching customer details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("NotificationActivity", "Error fetching customer details", e);
                });
    }



}
