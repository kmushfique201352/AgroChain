package com.example.agrochain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsViewActivity extends AppCompatActivity {
    private LinearLayout layoutNews;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_view);

        firestore = FirebaseFirestore.getInstance();
        layoutNews = findViewById(R.id.layoutNews);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadNews);

        loadNews();
    }

    private void loadNews() {
        swipeRefreshLayout.setRefreshing(true);
        firestore.collectionGroup("NEWS")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        layoutNews.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            addNewsToLayout(document);
                        }
                    } else {
                        Toast.makeText(NewsViewActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
    private void addNewsToLayout(QueryDocumentSnapshot document) {
        View newsView = LayoutInflater.from(this).inflate(R.layout.item_news, layoutNews, false);

        ImageView imageView = newsView.findViewById(R.id.imageViewNews);
        TextView textViewTitle = newsView.findViewById(R.id.textViewTitle);
        TextView textViewContent = newsView.findViewById(R.id.textViewContent);

        textViewTitle.setText(document.getString("title"));
        textViewContent.setText(document.getString("content"));

        Glide.with(this).load(document.getString("imageUrl")).into(imageView);

        layoutNews.addView(newsView);
    }

}

