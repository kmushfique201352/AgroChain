package com.example.agrochain;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {
    StorageReference storageReferance;
    LinearProgressIndicator progress;
    private Spinner spinnerSeason, spinnerProduct;
    private TextView textViewPrice;



    private EditText editTextQuantity, editTextReleaseDate, editTextNote;
    private ImageView imageViewPostImage;
    private Button buttonSubmitPost;
    private Uri image;

    private final ActivityResultLauncher<Intent> activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                if (result.getData() != null) {
                    image=result.getData().getData();
                    imageViewPostImage.setEnabled(true);
                    Glide.with(getApplicationContext()).load(image).into(imageViewPostImage);
                }
            }else
                Toast.makeText(CreatePostActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        storageReferance=FirebaseStorage.getInstance().getReference();

        spinnerSeason = findViewById(R.id.spinnerSeason);
        spinnerProduct = findViewById(R.id.spinnerProduct);
        textViewPrice = findViewById(R.id.textViewPrice);

        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextReleaseDate = findViewById(R.id.editTextReleaseDate);
        editTextNote = findViewById(R.id.editTextNote);
        imageViewPostImage = findViewById(R.id.imageViewPostImage);
        buttonSubmitPost = findViewById(R.id.buttonSubmitPost);

        loadSeasons();

        editTextReleaseDate.setOnClickListener(v -> showDatePickerDialog());
        imageViewPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });
        buttonSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(image!=null){
                    UploadImage(image);
                }else{
                    Toast.makeText(CreatePostActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });


        String userID = getUserID();
        if (userID != null) {
            Toast.makeText(this, "User ID: " + userID, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadSeasons() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("GOV").document("GOVA-CyUsLz").collection("PRODUCT")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> seasons = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String category = document.getString("category");
                            if (!seasons.contains(category)) {
                                seasons.add(category);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seasons);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerSeason.setAdapter(adapter);

                        spinnerSeason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedSeason = adapter.getItem(position);
                                if (selectedSeason != null) {
                                    loadProducts(selectedSeason);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }


    private void loadProducts(String season) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("GOV").document("GOVA-CyUsLz").collection("PRODUCT")
                .whereEqualTo("category", season)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> products = new ArrayList<>();
                        Map<String, String> productPriceMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("name");
                            String price = document.getString("price");
                            products.add(productName);
                            productPriceMap.put(productName, price);
                        }

                        if (!products.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, products);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerProduct.setAdapter(adapter);

                            spinnerProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedProduct = adapter.getItem(position);
                                    if (selectedProduct != null && productPriceMap.containsKey(selectedProduct)) {
                                        textViewPrice.setText(productPriceMap.get(selectedProduct));
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    textViewPrice.setText("");
                                }
                            });
                        } else {
                            Toast.makeText(CreatePostActivity.this, "No products found for the selected season.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }





    public String getUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }

    private void UploadImage(Uri image){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "IMG_" + dateFormat.format(new Date()) + ".jpg";

        StorageReference reference = storageReferance.child("images/"+ fileName);
        reference.putFile(image)
                .addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressDialog.dismiss();
                    String imageUrl = uri.toString();
                    submitPost(imageUrl);
                    Toast.makeText(CreatePostActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    double progressPercentage = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + (int) progressPercentage + "%...");
                });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + yearSelected;
                    editTextReleaseDate.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }


    private void submitPost(String imageUrl) {
        String userID = getUserID();
        if (userID == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedCategory = (String) spinnerSeason.getSelectedItem();
        String selectedProduct = (String) spinnerProduct.getSelectedItem();
        String priceText = textViewPrice.getText().toString();

        if (selectedCategory == null || selectedProduct == null || priceText.isEmpty()) {
            Toast.makeText(this, "Please make sure all fields are filled out correctly.", Toast.LENGTH_SHORT).show();
            return;
        }

        String collectionName = determineCollectionName(userID);
        if(collectionName.isEmpty()){
            Toast.makeText(this, "Unable to determine collection name from userID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("category", selectedCategory);
        post.put("name", selectedProduct);
        post.put("price", priceText);
        post.put("quantity", editTextQuantity.getText().toString());
        post.put("releaseDate", editTextReleaseDate.getText().toString());
        post.put("note", editTextNote.getText().toString());
        post.put("imageUrl", imageUrl);

        String postDocumentName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(collectionName)
                .document(userID)
                .collection("POST")
                .document(postDocumentName)
                .set(post)
                .addOnSuccessListener(aVoid -> Toast.makeText(CreatePostActivity.this, "Post submitted successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(CreatePostActivity.this, "Error submitting post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private String determineCollectionName(String userID) {
        if(userID.startsWith("FAR")) return "FAR";
        if(userID.startsWith("GOV")) return "GOV";
        if(userID.startsWith("WHO")) return "WHO";
        if(userID.startsWith("RET")) return "RET";
        if(userID.startsWith("CUS")) return "CUS";

        return "";
    }
}
