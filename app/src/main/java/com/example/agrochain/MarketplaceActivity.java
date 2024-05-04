package com.example.agrochain;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.agrochain.Product;
import com.example.agrochain.ProductAdapter;
import com.example.agrochain.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceActivity extends AppCompatActivity {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        String userID = getIntent().getStringExtra("userID");
        Toast.makeText(this, "User:"+userID, Toast.LENGTH_SHORT).show();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        productsRecyclerView.setAdapter(productAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
        Log.d("MarketplaceActivity", "Activity created and product loading initiated.");

        loadProducts();
    }

    private void loadProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("GOV").document("GOVA-CyUsLz").collection("PRODUCT_MARKET")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                        Log.d("MarketplaceActivity", "Products loaded successfully, total products: " + productList.size());
                    } else {
                        Log.e("MarketplaceActivity", "Error loading products", task.getException());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
}
