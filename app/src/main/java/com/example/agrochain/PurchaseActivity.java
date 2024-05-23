package com.example.agrochain;

import static java.lang.Double.parseDouble;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PurchaseActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private String userIDFar,userID;
    ImageView productImageView;
    TextView priceTextView, quantityTextView,totalPriceTextView;
    EditText addressEditText, deliveryLocationEditText, contactEditText;
    Button minusButton, plusButton, proceedButton;
    double currentQuantity;
    double maxQuantity;
    double pricePerUnit;
    String price;
    double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        firestore = FirebaseFirestore.getInstance();

        productImageView = findViewById(R.id.productImageView);
        priceTextView = findViewById(R.id.priceTextView);
        quantityTextView = findViewById(R.id.quantityTextView);
        addressEditText = findViewById(R.id.addressEditText);
        deliveryLocationEditText = findViewById(R.id.deliveryLocationEditText);
        contactEditText = findViewById(R.id.contactEditText);
        proceedButton = findViewById(R.id.proceedButton);
        minusButton = findViewById(R.id.minusButton);
        plusButton = findViewById(R.id.plusButton);
        proceedButton = findViewById(R.id.proceedButton);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);

        if (quantityTextView == null) {
            Log.e("PurchaseActivity", "quantityTextView is null!");
        } else {
            Log.d("PurchaseActivity", "quantityTextView is properly initialized.");
        }

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        price = intent.getStringExtra("price");
        String quantity = intent.getStringExtra("quantity");
        String userName = intent.getStringExtra("userName");

        userID = intent.getStringExtra("userID");
        String userType = getIntent().getStringExtra("userType");
        userIDFar = intent.getStringExtra("userIDFar");


        try {
            maxQuantity = parseDouble(quantity);
            Log.d("PurchaseActivity", "Max quantity set to: " + maxQuantity);
        } catch (NumberFormatException e) {
            maxQuantity = 1.0;
            Log.e("PurchaseActivity", "Failed to parse max quantity", e);
        }



        Toast.makeText(PurchaseActivity.this, userID, Toast.LENGTH_SHORT).show();
        if (userID != null) {
            String collectionPath = userType.equals("WHO") ? "WHO" : "CUS";
            firestore.collection(collectionPath).document(userID).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userAddress = documentSnapshot.getString("division") + ", " + documentSnapshot.getString("district") + ", " + documentSnapshot.getString("upazila");
                    String phoneNumber = documentSnapshot.getString("phone");

                    addressEditText.setText(userAddress);
                    contactEditText.setText(phoneNumber);
                } else {
                    Toast.makeText(PurchaseActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(PurchaseActivity.this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(PurchaseActivity.this, "Error: User ID is missing", Toast.LENGTH_SHORT).show();
        }

        Glide.with(this).load(imageUrl).into(productImageView);
        pricePerUnit=Double.parseDouble(price);
        priceTextView.setText(price);
        currentQuantity = 1.0;
        updateTotalPrice();
        quantityTextView.setText(String.format("%.1f", currentQuantity));

        setUpQuantityControls();



        proceedButton.setOnClickListener(v -> {
            Toast.makeText(PurchaseActivity.this, "Proceeding with purchase...", Toast.LENGTH_SHORT).show();
            postPurchaseData();
        });
    }




    private void updateTotalPrice() {
        total = pricePerUnit * currentQuantity;
        totalPriceTextView.setText(String.format("%.2f", total));
    }

    private void setUpQuantityControls() {
        minusButton.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity -= 0.5;
                quantityTextView.setText(String.format("%.1f", currentQuantity));
                Log.d("PurchaseActivity", "Decreased: " + currentQuantity);
            } else {
                Toast.makeText(this, "Cannot decrease below 1 kg", Toast.LENGTH_SHORT).show();
            }
        });

        plusButton.setOnClickListener(v -> {
            Log.d("PurchaseActivity", "Trying to increase from: " + currentQuantity + " with max: " + maxQuantity);
            if (currentQuantity < maxQuantity) {
                currentQuantity += 0.5;
                quantityTextView.setText(String.format("%.1f", currentQuantity));
                updateTotalPrice();
                Log.d("PurchaseActivity", "Increased: " + currentQuantity);
            } else {
                Toast.makeText(this, "Cannot exceed available quantity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postPurchaseData() {
        Map<String, Object> purchaseData = new HashMap<>();
//        product
        purchaseData.put("userIDFar", userIDFar);
        purchaseData.put("image", getIntent().getStringExtra("imageUrl"));
        purchaseData.put("price", priceTextView.getText().toString());
        purchaseData.put("userName", getIntent().getStringExtra("userName"));
        purchaseData.put("phoneNumber", getIntent().getStringExtra("phoneNumber"));
        purchaseData.put("userAddress", getIntent().getStringExtra("userAddress"));
//        Who/Cus
        purchaseData.put("userID", userID);
        purchaseData.put("address", addressEditText.getText().toString());
        purchaseData.put("deliveryLocation", deliveryLocationEditText.getText().toString());
        purchaseData.put("quantity", quantityTextView.getText().toString());
        purchaseData.put("contact", contactEditText.getText().toString());

        purchaseData.put("CUS_status", "pending");
        purchaseData.put("FAR_status", "pending");
        purchaseData.put("Total", total);


        // Post to GOV
        firestore.collection("GOV").document("GOVA-CyUsLz").collection("DELIVERY")
                .add(purchaseData)
                .addOnSuccessListener(documentReference -> Toast.makeText(PurchaseActivity.this, "Delivery posted to GOV", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(PurchaseActivity.this, "Failed to post delivery to GOV: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Post to FAR
//        firestore.collection("FAR").document(userIDFar).collection("NOTIFICATION")
//                .add(purchaseData)
//                .addOnSuccessListener(documentReference -> Toast.makeText(PurchaseActivity.this, "Delivery posted to FAR", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(PurchaseActivity.this, "Failed to post delivery to FAR: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }




}
