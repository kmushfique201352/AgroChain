package com.example.agrochain;

import static java.lang.Integer.parseInt;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.util.Date;

public class NotificationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userID;
    private LinearLayout notificationsLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationsLayout = findViewById(R.id.notificationsLayout);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        db = FirebaseFirestore.getInstance();

        userID = getIntent().getStringExtra("userID");
        Log.d("NotificationsActivity", "Received userID: " + userID);

        if (userID == null) {
            Log.e("NotificationsActivity", "UserID is null");
            finish();
        }

        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
        loadNotifications();
    }

    private void loadNotifications() {
        swipeRefreshLayout.setRefreshing(true);
        db.collection("FAR").document(userID).collection("NOTIFICATION")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    notificationsLayout.removeAllViews();
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        Log.d("NotificationActivity", "Fetched " + task.getResult().size() + " notifications");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("NotificationActivity", "Document: " + document.getId() + " => " + document.getData());
                            if (document.contains("message")) {
                                addGovernmentNotificationToView(document);
                            } else if (document.contains("status")) {
                                addCustomerNotificationToView(document);
                            }
                            else {
                                Log.d("NotificationActivity", "Unrecognized notification structure: " + document.getData());
                            }
                        }
                    } else {
                        Log.e("NotificationActivity", "Failed to fetch notifications: ", task.getException());
                        Toast.makeText(this, "Error loading notifications.", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }




    private void addGovernmentNotificationToView(QueryDocumentSnapshot document) {
        View notificationView = LayoutInflater.from(this).inflate(R.layout.notification_item, notificationsLayout, false);
        TextView title = notificationView.findViewById(R.id.notificationTitle);
        TextView message = notificationView.findViewById(R.id.notificationMessage);
        TextView date = notificationView.findViewById(R.id.notificationDate);

        title.setText("Government Notification");
        message.setText(document.getString("message"));
        Date timestamp = document.getDate("timestamp");
        date.setText(timestamp != null ? DateFormat.getDateInstance(DateFormat.SHORT).format(timestamp) : "Date not available");

        notificationsLayout.addView(notificationView);
    }

    private void addCustomerNotificationToView(QueryDocumentSnapshot document) {
        View notificationView = LayoutInflater.from(this).inflate(R.layout.notification_item_customer, notificationsLayout, false);
        TextView title = notificationView.findViewById(R.id.notificationTitleCustomer);
        TextView message = notificationView.findViewById(R.id.notificationMessageCustomer);
        TextView details = notificationView.findViewById(R.id.notificationDetailsCustomer);
        TextView weightView = notificationView.findViewById(R.id.notificationWeightCustomer);
        TextView customerNameView = notificationView.findViewById(R.id.notificationCustomerNameCustomer);
//        TextView productNameView = notificationView.findViewById(R.id.notificationProductNameCustomer);
        TextView quantityView = notificationView.findViewById(R.id.notificationQuantityCustomer);
        TextView pickUpDateView = notificationView.findViewById(R.id.notificationPickUpDateCustomer);
        ImageView imageView = notificationView.findViewById(R.id.notificationImageCustomer);

        Button callButton = notificationView.findViewById(R.id.buttonCallCustomer);

        title.setText("Order Notification");
        message.setText("Prepare your parcel. Our deliveyr man will soon receive the parcel from you");

        details.setText("Address: " + document.getString("address") + ", " + document.getString("locations"));
        weightView.setText("Quantity: " + document.getDouble("quantity"));
        customerNameView.setText("Price: " + document.getDouble("price") * document.getDouble("quantity"));

        String imageUrl = document.getString("image");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.default_profile);
        }

        String phoneNumber = "38556";

        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });

        notificationsLayout.addView(notificationView);
    }







    public static class UserNotification {
        private String title;
        private String message;
        private Timestamp timestamp;

        public UserNotification(){

        }

        public UserNotification(String title, String message, Timestamp timestamp) {
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public Date getDate() {
            return timestamp != null ? timestamp.toDate() : null;
        }
    }


}

