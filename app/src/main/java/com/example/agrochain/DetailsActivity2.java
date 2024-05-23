package com.example.agrochain;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity2 extends AppCompatActivity {
    private LinearLayout fertilizersContainer;
    private LinearLayout pesticidesContainer;

    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private RecyclerView recyclerViewFertilizers, recyclerViewPesticides;
    private ProductAdapter fertilizerAdapter, pesticideAdapter;
    private List<Product> fertilizerList, pesticideList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details2);


        recyclerViewFertilizers = findViewById(R.id.recyclerViewFertilizers);
        recyclerViewPesticides = findViewById(R.id.recyclerViewPesticides);

        fertilizerList = new ArrayList<>();
        fertilizerAdapter = new ProductAdapter(this, fertilizerList);
        recyclerViewFertilizers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFertilizers.setAdapter(fertilizerAdapter);

        pesticideList = new ArrayList<>();
        pesticideAdapter = new ProductAdapter(this, pesticideList);
        recyclerViewPesticides.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPesticides.setAdapter(pesticideAdapter);

        loadProducts("PRODUCT_MARKET", fertilizerList, fertilizerAdapter, "BAAL");
        loadProducts("PRODUCT_MARKET", pesticideList, pesticideAdapter, "WOW");




        ImageView imageView = findViewById(R.id.detail_image);
        TextView labelView = findViewById(R.id.detail_label);
        playButton = findViewById(R.id.play_voice_button);


        Intent intent = getIntent();
        int imageResId = intent.getIntExtra("img1", 0);
        String labelText = intent.getStringExtra("lbl1");
        int voiceClipResId = intent.getIntExtra("vc1", -1);

        imageView.setImageResource(imageResId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            labelView.setText(Html.fromHtml(labelText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            labelView.setText(Html.fromHtml(labelText));
        }

        if (voiceClipResId != -1) {
            mediaPlayer = MediaPlayer.create(this, voiceClipResId);
            playButton.setOnClickListener(v -> togglePlayPause());
        }
    }

    private void loadProducts(String category, List<Product> productList, ProductAdapter adapter, String brand) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("GOV").document("GOVA-CyUsLz").collection(category)
                .whereEqualTo("brand", brand)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("DetailsActivity2", "Error loading " + category, task.getException());
                    }
                });
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
