package com.example.agrochain;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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
