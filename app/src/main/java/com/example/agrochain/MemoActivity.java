package com.example.agrochain;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MemoActivity extends AppCompatActivity {

    private ImageView productImageView;
    private TextView productNameTextView, productBrandTextView, productPriceTextView, productOldPriceTextView, productSizeTextView, totalPriceTextView;
    private EditText nameEditText, mobileEditText, addressEditText;
    private Button decreaseButton, increaseButton, proceedButton;
    private int quantity = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        String userID = getIntent().getStringExtra("userID");
        if (userID == null) {
            Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toast.makeText(this, "User: " + userID, Toast.LENGTH_SHORT).show();

        initializeViews();
        setListeners();
        fetchUserDetails(userID);
    }

    private void initializeViews() {

        Product product = (Product) getIntent().getSerializableExtra("PRODUCT");

        if (product == null) {
            Toast.makeText(this, "Product data is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productImageView = findViewById(R.id.productImageView);
        productNameTextView = findViewById(R.id.productNameTextView);
        productBrandTextView = findViewById(R.id.productBrandTextView);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        productOldPriceTextView = findViewById(R.id.productOldPriceTextView);
        productSizeTextView = findViewById(R.id.productSizeTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);

        nameEditText = findViewById(R.id.nameEditText);
        mobileEditText = findViewById(R.id.mobileEditText);
        addressEditText = findViewById(R.id.addressEditText);

        decreaseButton = findViewById(R.id.decreaseButton);
        increaseButton = findViewById(R.id.increaseButton);
        proceedButton = findViewById(R.id.proceedButton);

        Glide.with(this).load(product.getImageUrl()).into(productImageView);
        productNameTextView.setText(product.getName());
        productBrandTextView.setText(product.getBrand());
        productPriceTextView.setText(String.format("TK %s", product.getPrice()));
        productOldPriceTextView.setText(String.format("TK %s", product.getOldPrice()));
        productSizeTextView.setText(product.getSize());

        updateTotalPrice();
    }

    private void setListeners() {
        decreaseButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });

        increaseButton.setOnClickListener(v -> {
            quantity++;
            updateQuantity();
        });

        proceedButton.setOnClickListener(v -> saveOrder());
    }

    private void updateQuantity() {
        TextView quantityTextView = findViewById(R.id.quantityTextView);
        quantityTextView.setText(String.valueOf(quantity));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        try {
            String priceString = productPriceTextView.getText().toString().replaceAll("[^\\d.]", "");
            double price = Double.parseDouble(priceString);
            double total = price * quantity;

            totalPriceTextView.setText(String.format("Total Price: TK %.2f", total));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse price", e);
            totalPriceTextView.setText("Price format error");
        }
    }



    private void saveOrder() {
        try {
            if (productNameTextView.getText().toString().isEmpty() ||
                    productPriceTextView.getText().toString().isEmpty() ||
                    nameEditText.getText().toString().isEmpty() ||
                    mobileEditText.getText().toString().isEmpty() ||
                    addressEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Incomplete data for order.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("name", productNameTextView.getText().toString());
            orderDetails.put("brand", productBrandTextView.getText().toString());
            orderDetails.put("price", productPriceTextView.getText().toString());
            orderDetails.put("oldPrice", productOldPriceTextView.getText().toString());
            orderDetails.put("size", productSizeTextView.getText().toString());
            orderDetails.put("quantity", quantity);
            orderDetails.put("customerName", nameEditText.getText().toString());
            orderDetails.put("customerMobile", mobileEditText.getText().toString());
            orderDetails.put("customerAddress", addressEditText.getText().toString());
            orderDetails.put("totalPrice", totalPriceTextView.getText().toString());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("GOV").document("GOVA-CyUsLz").collection("PRODUCT_ORDERS").add(orderDetails)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Order placed successfully with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to place order", e);
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error processing order: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error in saveOrder", e);
        }
    }

    static class Order {
        String name, brand, price, oldPrice, size;
        int quantity;
        String customerName, customerMobile, customerAddress;
        String totalPrice;

        public Order(String name, String brand, String price, String oldPrice, String size, int quantity,
                     String customerName, String customerMobile, String customerAddress, String totalPrice) {
            this.name = name;
            this.brand = brand;
            this.price = price;
            this.oldPrice = oldPrice;
            this.size = size;
            this.quantity = quantity;
            this.customerName = customerName;
            this.customerMobile = customerMobile;
            this.customerAddress = customerAddress;
            this.totalPrice = totalPrice;
        }
    }
    private void fetchUserDetails(String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("FAR").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String division = task.getResult().getString("division");
                String district = task.getResult().getString("district");
                String upazila = task.getResult().getString("upazila");

                String address = (division != null ? division : "") +
                        (district != null ? ", " + district : "") +
                        (upazila != null ? ", " + upazila : "");
                addressEditText.setText(address);
                nameEditText.setText(task.getResult().getString("name"));
                mobileEditText.setText(task.getResult().getString("phone"));
            } else {
                Log.e(TAG, "Failed to fetch user details", task.getException());
                Toast.makeText(this, "Failed to load user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

}