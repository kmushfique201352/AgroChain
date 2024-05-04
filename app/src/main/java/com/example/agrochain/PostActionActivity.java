package com.example.agrochain;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostActionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_action);

        TextView tvPostDetails = findViewById(R.id.tvPostDetails);
        TextView userId=findViewById(R.id.user);
        EditText etComment = findViewById(R.id.etComment);
        Button btnWarning = findViewById(R.id.btnWarning);
        Button btnDelete = findViewById(R.id.btnDelete);

        String postDetails = getIntent().getStringExtra("postDetails");
        String userID = getIntent().getStringExtra("userID");
        tvPostDetails.setText(postDetails);
        userId.setText(userID);

        btnWarning.setOnClickListener(v -> {
            sendWarning(userID,etComment.getText().toString());
        });

        btnDelete.setOnClickListener(v -> {
            requestDelete(etComment.getText().toString());
        });
    }

    private void sendWarning(String farmerId, String comment) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("message", comment);
        notificationData.put("timestamp", new Date());
        notificationData.put("read", false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("FAR").document(farmerId).collection("NOTIFICATION")
                .add(notificationData)
                .addOnSuccessListener(aVoid -> Log.d("Notification", "Notification sent successfully."))
                .addOnFailureListener(e -> Log.e("Notification", "Error sending notification.", e));
    }
    private void sendPushNotification(String token, String message) {

    }


    private void requestDelete(String comment) {
    }
}
