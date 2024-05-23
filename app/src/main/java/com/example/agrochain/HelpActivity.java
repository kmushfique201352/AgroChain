package com.example.agrochain;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 201;
    private static final int PICK_IMAGES_REQUEST = 202;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFilePath = null;
    private boolean isRecording = false;
    private Button btnCancelAudio;
    private ImageButton recordVoiceButton;
    private ImageButton btnPlayAudio;
    private SeekBar seekBarAudio;
    private RelativeLayout audioPlayer;
    private Handler handler = new Handler();
    private ArrayList<Uri> imageUris;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        userID = getIntent().getStringExtra("userID");

        recordVoiceButton = findViewById(R.id.recordVoiceButton);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        audioPlayer = findViewById(R.id.audioPlayer);
        Button selectImagesButton = findViewById(R.id.selectImagesButton);
        btnCancelAudio = findViewById(R.id.btnCancelAudio);

        imageUris = new ArrayList<>();
        Button postToFirestoreButton = findViewById(R.id.postToFirestoreButton);
        postToFirestoreButton.setOnClickListener(v -> uploadAudioAndPost());

        if (!checkPermissions()) {
            requestPermissions();
        }

        selectImagesButton.setOnClickListener(v -> selectImages());
        recordVoiceButton.setOnClickListener(v -> toggleRecording());

        btnPlayAudio.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                prepareMediaPlayer();
            }

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayAudio.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                btnPlayAudio.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar();
            }
        });

        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });

        btnCancelAudio.setOnClickListener(v -> {
            stopAudioPlayback();
            new File(audioFilePath).delete();
            audioPlayer.setVisibility(View.GONE);
        });
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO_PERMISSION);
            return false;
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Permission Denied. Please enable permissions in app settings.", Toast.LENGTH_LONG).show();
        }
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            LinearLayout imagesContainer = findViewById(R.id.imagesContainer);
            imagesContainer.removeAllViews();
            imageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(250, 250));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageURI(imageUri);
                    imagesContainer.addView(imageView);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(250, 250));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageURI(imageUri);
                imagesContainer.addView(imageView);
            }
        }
    }

    private void startRecording() {
        if (!isRecording) {
            try {
                File outputFile = File.createTempFile("temp_audio", ".3gp", getExternalFilesDir(Environment.DIRECTORY_MUSIC));
                audioFilePath = outputFile.getAbsolutePath();

                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setOutputFile(audioFilePath);
                mediaRecorder.prepare();
                mediaRecorder.start();

                isRecording = true;
                btnCancelAudio.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Toast.makeText(this, "Recording setup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            stopRecording();
        }
    }

    private void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
            recordVoiceButton.setBackgroundColor(Color.GREEN);
            prepareMediaPlayer();
            audioPlayer.setVisibility(View.VISIBLE);
        } else {
            startRecording();
            recordVoiceButton.setBackgroundColor(Color.RED);
            audioPlayer.setVisibility(View.GONE);
        }
    }
    private void prepareMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp -> btnPlayAudio.setImageResource(android.R.drawable.ic_media_play));

            seekBarAudio.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            Toast.makeText(this, "Playback failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            seekBarAudio.setProgress(mediaPlayer.getCurrentPosition());
            Runnable updater = this::updateSeekBar;
            handler.postDelayed(updater, 1000);
        }
    }

    private void stopAudioPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        audioPlayer.setVisibility(View.GONE);
        btnCancelAudio.setVisibility(View.GONE);
        btnPlayAudio.setImageResource(android.R.drawable.ic_media_play);

    }






    private void uploadAudioAndPost() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Uri fileUri = Uri.fromFile(new File(audioFilePath));
        StorageReference audioRef = storage.getReference().child("audio/" + fileUri.getLastPathSegment());

        audioRef.putFile(fileUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return audioRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    String audioDownloadUrl = uri.toString();
                    uploadImagesAndPost(audioDownloadUrl);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Audio upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImagesAndPost(String audioUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        List<String> imageUrls = new ArrayList<>();
        List<Task<Uri>> uploadTasks = new ArrayList<>();

        for (Uri uri : imageUris) {
            StorageReference imageRef = storage.getReference().child("images/" + uri.getLastPathSegment());
            Task<Uri> uploadTask = imageRef.putFile(uri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    });
            uploadTasks.add(uploadTask);
        }

        Task<List<Uri>> allTasks = Tasks.whenAllSuccess(uploadTasks);
        allTasks.addOnSuccessListener(uris -> {
            for (Uri uri : uris) {
                imageUrls.add(uri.toString());
            }
            if (imageUrls.size() == imageUris.size()) {
                postToFirestore(audioUrl, imageUrls);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void postToFirestore(String audioUrl, List<String> imageUrls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> helpPost = new HashMap<>();
        helpPost.put("audioPath", audioUrl);
        helpPost.put("images", imageUrls);

        db.collection("GOV").document("GOVA-CyUsLz").collection("HELP_POST").document(userID)
                .set(helpPost)
                .addOnSuccessListener(aVoid -> Toast.makeText(HelpActivity.this, "Help post uploaded!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(HelpActivity.this, "Error uploading help post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


}
