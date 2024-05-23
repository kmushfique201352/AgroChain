package com.example.agrochain;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        userID = getIntent().getStringExtra("userID");
        Toast.makeText(this, "User:"+userID, Toast.LENGTH_SHORT).show();

        Product product = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (product == null) {
            Log.e(TAG, "No product received, closing activity.");
            finish();
            return;
        } else {
            Log.d("ProductDetailActivity", "Product received: " + product.getName());
        }
        Log.d(TAG, "Product received: " + product.getName());


        ImageView productImage = findViewById(R.id.productImageDetail);
        TextView productName = findViewById(R.id.productNameDetail);
        TextView productBrand = findViewById(R.id.productBrandDetail);
        TextView productPrice = findViewById(R.id.productPriceDetail);
        TextView productOldPrice = findViewById(R.id.productOldPriceDetail);
        TextView productDescription = findViewById(R.id.productDescriptionDetail);
        TextView productSize = findViewById(R.id.productSizeDetail);
        Button buyButton = findViewById(R.id.buyButton);

        Glide.with(this).load(product.getImageUrl()).into(productImage);

//        productName.setText(product.getName());
//        productBrand.setText(product.getBrand());
//        productPrice.setText(String.format("TK. %s", product.getPrice()));
//        productOldPrice.setText(String.format("TK. %s", product.getOldPrice()));
//        productDescription.setText(product.getDescription());
//        productDescription.setText(product.getSize());

        if (product != null) {
            productName.setText(product.getName() != null ? product.getName() : "N/A");
            productBrand.setText(product.getBrand() != null ? product.getBrand() : "N/A");
            productPrice.setText(product.getPrice() != null ? String.format("TK. %s", product.getPrice()) : "Price N/A");
            productOldPrice.setText(product.getOldPrice() != null ? String.format("TK. %s", product.getOldPrice()) : "Old Price N/A");
            productDescription.setText(product.getDescription() != null ? product.getDescription() : "Description N/A");
            productSize.setText(product.getSize() != null ? product.getSize() : "Size N/A");
        } else {
            Log.e("ProductDetailActivity", "Product object is null");
            finish();
        }

//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, product.getSize());
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        productSize.setAdapter(spinnerAdapter);

        buyButton = findViewById(R.id.buyButton);
        buyButton.setOnClickListener(v -> {
            try {
                if (product != null) {
                    Intent memoIntent = new Intent(ProductDetailActivity.this, MemoActivity.class);
                    memoIntent.putExtra("PRODUCT", product);
                    memoIntent.putExtra("userID", userID);
                    Log.d(TAG, "Starting MemoActivity with product: " + product.getName() + " and userID: " + userID);
                    startActivity(memoIntent);
                } else {
                    Log.e(TAG, "Product data is null, cannot proceed to MemoActivity.");
                    Toast.makeText(this, "Product data is missing.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error when attempting to start MemoActivity", e);
            }
        });

    }
}
