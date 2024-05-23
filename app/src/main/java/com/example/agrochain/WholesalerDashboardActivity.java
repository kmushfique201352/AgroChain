package com.example.agrochain;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WholesalerDashboardActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private LinearLayout layoutPost;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button filterButton;
    private String userID;
    private String selectedDivision, selectedDistrict, selectedUpazila;

    private ArrayAdapter<CharSequence> divisionAdapter, districtAdapter, upazilaAdapter;
    private Map<String, List<String>> divisionDistrictsMap = new HashMap<>();
    private Map<String, List<String>> districtUpazilasMap = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wholesaler_dashboard);

        userID = getIntent().getStringExtra("userID");
        if (userID != null) {
            listenForNotifications(userID);
            Toast.makeText(this, "User:"+userID, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
        }

        firestore = FirebaseFirestore.getInstance();
        layoutPost = findViewById(R.id.layoutPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        filterButton = findViewById(R.id.filterButton);
        Button newsBtn=findViewById(R.id.newsButton);
        View postView = LayoutInflater.from(this).inflate(R.layout.item_post_for_who, layoutPost, false);

        swipeRefreshLayout.setOnRefreshListener(() -> loadPosts());
        filterButton.setOnClickListener(v -> showFilterPopup());
        loadPosts();

        newsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(WholesalerDashboardActivity.this,NewsViewActivity.class);
                startActivity(intent);
            }
        });
    }


    private void listenForNotifications(String userId) {
        FirebaseFirestore.getInstance()
                .collection("WHO")
                .document(userId)
                .collection("NOTIFICATION")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Firestore", "Listen failed.", e);
                            return;
                        }

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            if (doc.exists()) {

                                showNotificationPopup(doc);
                            }
                        }
                    }
                });
    }

    private void showNotificationPopup(DocumentSnapshot document) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Notification Details");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 50, 50, 50);

            // Image handling with Glide
            String imageUrl = document.getString("image");
            ImageView imageView = new ImageView(this);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .override(600)
                        .centerCrop()
                        .into(imageView);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));
                layout.addView(imageView);
            }

            addTextView(layout, "Address: ", document.getString("address"));
            addTextView(layout, "Location: ", document.getString("location"));
            addTextView(layout, "Price: ", safeDoubleToString(document, "price"));
            addTextView(layout, "Quantity: ", safeLongToString(document, "quantity"));
            addTextView(layout, "Total Cost: ", document.getString("totalCost"));

            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, id) -> {
                dialog.dismiss();
                deleteNotification(document);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("PopupError", "Error showing notification popup", e);
            Toast.makeText(this, "Failed to display notification details.", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteNotification(DocumentSnapshot document) {
        if (document.getReference() != null) {
            document.getReference().delete()
                    .addOnSuccessListener(aVoid -> Log.d("DeleteNotification", "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w("DeleteNotification", "Error deleting document", e));
        }
    }
    private String safeDoubleToString(DocumentSnapshot document, String field) {
        try {
            Double value = document.getDouble(field);
            return value != null ? String.format("%.2f", value) : "N/A";
        } catch (RuntimeException ex) {
            Log.e("Firestore Conversion", "Field '" + field + "' is not a Double.", ex);
            return "Invalid data";
        }
    }


    private String safeLongToString(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value != null ? String.valueOf(value) : "N/A";
    }

    private void addTextView(LinearLayout layout, String prefix, String text) {
        TextView textView = new TextView(this);
        textView.setText(prefix + (text != null ? text : "Not available"));
        layout.addView(textView);
    }


















    private void loadPosts() {
        if (selectedDivision == null || selectedDistrict == null || selectedUpazila == null) {
            loadAllPosts();
        } else {
            firestore.collection("FAR")
                    .whereEqualTo("division", selectedDivision)
                    .whereEqualTo("district", selectedDistrict)
                    .whereEqualTo("upazila", selectedUpazila)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        layoutPost.removeAllViews();
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(this, "No users match this filter.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (DocumentSnapshot userSnapshot : queryDocumentSnapshots) {
                                Query postQuery = userSnapshot.getReference().collection("POST");
                                String userIDFar = userSnapshot.getReference().getId();
                                postQuery.get().addOnSuccessListener(postSnapshots -> {
                                    for (QueryDocumentSnapshot postSnapshot : postSnapshots) {
                                        addPostView(postSnapshot, userSnapshot.getString("name"), userSnapshot.getString("profilePictureUrl"), formatAddress(userSnapshot),userSnapshot.getString("phone"),userIDFar);
                                    }
                                });
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore Error", "Error loading filtered posts: ", e);
                        Toast.makeText(this, "Error loading filtered posts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void loadAllPosts() {
        firestore.collectionGroup("POST")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutPost.removeAllViews();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(WholesalerDashboardActivity.this, "No posts available.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                            DocumentReference userRef = postSnapshot.getReference().getParent().getParent();
                            if (userRef != null) {
                                userRef.get().addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        String userIDFar = userSnapshot.getReference().getId();
                                        String userName = userSnapshot.getString("name");
                                        String userAddress = userSnapshot.getString("division") + ", "
                                                + userSnapshot.getString("district") + ", "
                                                + userSnapshot.getString("upazila");
                                        String userProfileImageUrl = userSnapshot.getString("profilePictureUrl");
                                        String phoneNumber=userSnapshot.getString("phone");

                                        addPostView(postSnapshot,userName,userProfileImageUrl,userAddress,phoneNumber,userIDFar);
                                    }
                                });
                            }
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error loading all posts: ", e);
                    Toast.makeText(WholesalerDashboardActivity.this, "Error loading all posts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private String formatAddress(DocumentSnapshot userSnapshot) {
        return userSnapshot.getString("division") + ", " + userSnapshot.getString("district") + ", " + userSnapshot.getString("upazila");
    }

    private void addPostView(QueryDocumentSnapshot postSnapshot,String userName, String profilePictureUrl,String userAddress,String phone,String userIDFar) {
        View postView = LayoutInflater.from(this).inflate(R.layout.item_post_for_who, layoutPost, false);
        TextView userNameView = postView.findViewById(R.id.postUserName);
        TextView detailsView = postView.findViewById(R.id.postDetails);
        ImageView imageView = postView.findViewById(R.id.postImage);
        de.hdodenhof.circleimageview.CircleImageView userImageView = postView.findViewById(R.id.postUserImage);

        userNameView.setText(userName);

        String postDetails = formatPostDetails(postSnapshot,userAddress);
        detailsView.setText(postDetails);

        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(postSnapshot.getString("imageUrl")).into(imageView);

        String userProfileImageUrl = profilePictureUrl;
        Glide.with(this).load(userProfileImageUrl).placeholder(R.drawable.default_profile).into(userImageView);

        postView.setOnClickListener(v -> showPostDialog(userName, userAddress, phone, postSnapshot,userIDFar));

        layoutPost.addView(postView);
    }
    private String formatPostDetails(QueryDocumentSnapshot postSnapshot, String userAddress) {
        return "Product: " + postSnapshot.getString("product") + "\n" +
                "Price: " + postSnapshot.getString("price") + "\n" +
                "Quantity: " + postSnapshot.getString("quantity") + "\n" +
                "Release Date: " + postSnapshot.getString("releaseDate") + "\n" +
                "Address: " + userAddress + "\n" +
                "Note: " + postSnapshot.getString("note");
    }
    private void showPostDialog(String userName, String userAddress, String phoneNumber, QueryDocumentSnapshot postSnapshot,String userIDFar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(userName + " - " + userAddress);
        builder.setMessage(formatPostDetails(postSnapshot, userAddress));

        builder.setPositiveButton("Purchase", (dialog, which) -> {
            Intent purchaseIntent = new Intent(WholesalerDashboardActivity.this, PurchaseActivity.class);
//            far
            purchaseIntent.putExtra("imageUrl", postSnapshot.getString("imageUrl"));
            purchaseIntent.putExtra("price", postSnapshot.getString("price"));
            purchaseIntent.putExtra("quantity", postSnapshot.getString("quantity"));
            purchaseIntent.putExtra("userName", userName);
            purchaseIntent.putExtra("userAddress", userAddress);
            purchaseIntent.putExtra("phoneNumber", phoneNumber);
            purchaseIntent.putExtra("userIDFar", userIDFar);
//            who
            purchaseIntent.putExtra("userID", userID);
            purchaseIntent.putExtra("userType", "WHO");
            startActivity(purchaseIntent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View filterView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_posts, null);
        builder.setView(filterView);

        setupDivisionDistrictMap();
        setupDistrictUpazilaMap();

        Spinner spinnerDivision = filterView.findViewById(R.id.spinnerDivision);
        Spinner spinnerDistrict = filterView.findViewById(R.id.spinnerDistrict);
        Spinner spinnerUpazila = filterView.findViewById(R.id.spinnerUpazila);


        setupAdapters();
        spinnerDivision.setAdapter(divisionAdapter);
        spinnerDistrict.setAdapter(districtAdapter);
        spinnerUpazila.setAdapter(upazilaAdapter);

        spinnerDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDistrictAdapter(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUpazilaAdapter(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog dialog = builder.create();

        Button applyBtn = filterView.findViewById(R.id.btnApplyFilter);
        applyBtn.setOnClickListener(v -> {
            selectedDivision = spinnerDivision.getSelectedItem().toString();
            selectedDistrict = spinnerDistrict.getSelectedItem().toString();
            selectedUpazila = spinnerUpazila.getSelectedItem().toString();
            loadPosts();
            dialog.dismiss();  
        });

        dialog.show();
    }

    private void setupAdapters() {
        divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.divisions_array));
        districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        upazilaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
    }

    private void updateDistrictAdapter(String division) {
        List<String> districts = divisionDistrictsMap.getOrDefault(division, new ArrayList<>());
        districtAdapter.clear();
        districtAdapter.addAll(districts);
        districtAdapter.notifyDataSetChanged();
    }

    private void updateUpazilaAdapter(String district) {
        List<String> upazilas = districtUpazilasMap.getOrDefault(district, new ArrayList<>());
        upazilaAdapter.clear();
        upazilaAdapter.addAll(upazilas);
        upazilaAdapter.notifyDataSetChanged();
    }
    private void setupDivisionDistrictMap() {
        divisionDistrictsMap.put("Barishal", Arrays.asList("Barguna", "Barishal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur"));
        divisionDistrictsMap.put("Chattogram", Arrays.asList("Brahmanbaria", "Chandpur", "Chattogram", "Cumilla", "Cox's Bazar", "Feni", "Khagrachhari", "Lakshmipur", "Noakhali", "Rangamati"));
        divisionDistrictsMap.put("Dhaka", Arrays.asList("Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail"));
        divisionDistrictsMap.put("Khulna", Arrays.asList("Bagerhat", "Chuadanga", "Jessore", "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira"));
        divisionDistrictsMap.put("Mymensingh", Arrays.asList("Jamalpur", "Mymensingh", "Netrokona", "Sherpur"));
        divisionDistrictsMap.put("Rajshahi", Arrays.asList("Bogra", "Chapainawabganj", "Joypurhat", "Naogaon", "Natore", "Pabna", "Rajshahi", "Sirajganj"));
        divisionDistrictsMap.put("Rangpur", Arrays.asList("Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchag arh", "Rangpur", "Thakurgaon"));
        divisionDistrictsMap.put("Sylhet", Arrays.asList("Habiganj", "Moulvibazar", "Sunamganj", "Sylhet"));
    }

    private void setupDistrictUpazilaMap() {
        districtUpazilasMap.put("Barguna", Arrays.asList("Amtali", "Bamna", "Barguna Sadar", "Betagi", "Patharghata", "Taltali"));
        districtUpazilasMap.put("Barishal", Arrays.asList("Agailjhara", "Babuganj", "Bakerganj", "Banaripara", "Gaurnadi", "Hizla", "Mehendiganj", "Muladi", "Wazirpur", "Barishal Sadar"));
        districtUpazilasMap.put("Bhola", Arrays.asList("Bhola Sadar", "Burhanuddin", "Char Fasson", "Daulatkhan", "Lalmohan", "Manpura", "Tazumuddin"));
        districtUpazilasMap.put("Jhalokati", Arrays.asList("Jhalokati Sadar", "Kathalia", "Nalchity", "Rajapur"));
        districtUpazilasMap.put("Patuakhali", Arrays.asList("Bauphal", "Dashmina", "Galachipa", "Kalapara", "Mirzaganj", "Patuakhali Sadar", "Rangabali", "Dumki"));
        districtUpazilasMap.put("Pirojpur", Arrays.asList("Bhandaria", "Kawkhali", "Mathbaria", "Nazirpur", "Nesarabad (Swarupkati)", "Pirojpur Sadar", "Zianagar"));

        districtUpazilasMap.put("Brahmanbaria", Arrays.asList("Akhaura", "Ashuganj", "Bancharampur", "Bijoynagar", "Kasba", "Nabinagar", "Nasirnagar", "Sarail", "Brahmanbaria Sadar"));
        districtUpazilasMap.put("Chandpur", Arrays.asList("Chandpur Sadar", "Faridganj", "Haimchar", "Haziganj", "Kachua", "Matlab North", "Matlab South", "Shahrasti"));
        districtUpazilasMap.put("Chattogram", Arrays.asList("Anwara", "Banshkhali", "Boalkhali", "Chandanaish", "Fatikchhari", "Hathazari", "Lohagara", "Mirsharai", "Patiya", "Rangunia", "Raozan", "Sandwip", "Satkania", "Sitakunda", "Chattogram Sadar"));
        districtUpazilasMap.put("Cumilla", Arrays.asList("Barura", "Brahmanpara", "Burichang", "Chandina", "Chauddagram", "Daudkandi", "Debidwar", "Homna", "Laksam", "Langalkot", "Meghna", "Muradnagar", "Nangalkot", "Titas", "Cumilla Sadar", "Cumilla Sadar South", "Adarsa Sadar"));
        districtUpazilasMap.put("Cox's Bazar", Arrays.asList("Chakaria", "Cox's Bazar Sadar", "Kutubdia", "Maheshkhali", "Pekua", "Ramu", "Teknaf", "Ukhia"));
        districtUpazilasMap.put("Feni", Arrays.asList("Chhagalnaiya", "Daganbhuiyan", "Feni Sadar", "Fulgazi", "Parshuram", "Sonagazi"));
        districtUpazilasMap.put("Khagrachhari", Arrays.asList("Dighinala", "Khagrachhari", "Lakshmichhari", "Mahalchhari", "Manikchhari", "Matiranga", "Panchhari", "Ramgarh"));
        districtUpazilasMap.put("Lakshmipur", Arrays.asList("Lakshmipur Sadar", "Raipur", "Ramganj", "Ramgati", "Kamalnagar"));
        districtUpazilasMap.put("Noakhali", Arrays.asList("Begumganj", "Chatkhil", "Companiganj", "Hatiya", "Kabirhat", "Senbagh", "Sonaimuri", "Subarnachar", "Noakhali Sadar"));
        districtUpazilasMap.put("Rangamati", Arrays.asList("Baghaichhari", "Barkal", "Kaptai", "Kawkhali (Betbunia)", "Langadu", "Naniarchar", "Rajasthali", "Rangamati Sadar"));

        districtUpazilasMap.put("Dhaka", Arrays.asList("Dhamrai", "Dohar", "Keraniganj", "Nawabganj", "Savar", "Dhaka Sadar"));
        districtUpazilasMap.put("Gazipur", Arrays.asList("Gazipur Sadar", "Kaliakair", "Kaliganj", "Kapasia", "Sreepur"));
        districtUpazilasMap.put("Gopalganj", Arrays.asList("Gopalganj Sadar", "Kashiani", "Kotalipara", "Muksudpur", "Tungipara"));
        districtUpazilasMap.put("Kishoreganj", Arrays.asList("Austagram", "Bajitpur", "Bhairab", "Hossainpur", "Itna", "Karimganj", "Katiadi", "Kishoreganj Sadar", "Kuliarchar", "Mithamain", "Nikli", "Pakundia", "Tarail"));
        districtUpazilasMap.put("Madaripur", Arrays.asList("Madaripur Sadar", "Kalkini", "Rajoir", "Shibchar"));
        districtUpazilasMap.put("Manikganj", Arrays.asList("Daulatpur", "Ghior", "Harirampur", "Manikganj Sadar", "Saturia", "Shivalaya", "Singair"));
        districtUpazilasMap.put("Munshiganj", Arrays.asList("Gazaria", "Lohajang", "Munshiganj Sadar", "Sirajdikhan", "Sreenagar", "Tongibari"));
        districtUpazilasMap.put("Narayanganj", Arrays.asList("Araihazar", "Bandar", "Narayanganj Sadar", "Rupganj", "Sonargaon"));
        districtUpazilasMap.put("Narsingdi", Arrays.asList("Belabo", "Monohardi", "Narsingdi Sadar", "Palash", "Raipura", "Shibpur"));
        districtUpazilasMap.put("Rajbari", Arrays.asList("Baliakandi", "Goalandaghat", "Pangsha", "Rajbari Sadar"));
        districtUpazilasMap.put("Shariatpur", Arrays.asList("Bhedarganj", "Damudya", "Gosairhat", "Naria", "Shariatpur Sadar", "Zanjira"));
        districtUpazilasMap.put("Tangail", Arrays.asList("Basail", "Bhuapur", "Delduar", "Ghatail", "Gopalpur", "Kalihati", "Madhupur", "Mirzapur", "Nagarpur", "Sakhipur", "Tangail Sadar"));

        districtUpazilasMap.put("Bagerhat", Arrays.asList("Bagerhat Sadar", "Chitalmari", "Fakirhat", "Kachua", "Mollahat", "Mongla", "Morrelganj", "Rampal", "Sarankhola"));
        districtUpazilasMap.put("Jessore", Arrays.asList("Abhaynagar", "Bagherpara", "Chaugachha", "Jhikargachha", "Keshabpur", "Jessore Sadar", "Manirampur", "Sharsha"));
        districtUpazilasMap.put("Jhenaidah", Arrays.asList("Harinakunda", "Jhenaidah Sadar", "Kaliganj", "Kotchandpur", "Maheshpur", "Shailkupa"));
        districtUpazilasMap.put("Magura", Arrays.asList("Magura Sadar", "Mohammadpur", "Shalikha", "Sreepur"));
        districtUpazilasMap.put("Meherpur", Arrays.asList("Gangni", "Meherpur Sadar", "Mujibnagar"));
        districtUpazilasMap.put("Narail", Arrays.asList("Kalia", "Lohagara", "Narail Sadar"));
        districtUpazilasMap.put("Satkhira", Arrays.asList("Assasuni", "Debhata", "Kalaroa", "Kaliganj", "Satkhira Sadar", "Shyamnagar", "Tala"));

        districtUpazilasMap.put("Jamalpur", Arrays.asList("Baksiganj", "Dewanganj", "Islampur", "Jamalpur Sadar", "Madarganj", "Melandaha", "Sarishabari"));
        districtUpazilasMap.put("Mymensingh", Arrays.asList("Bhaluka", "Dhobaura", "Fulbaria", "Gafargaon", "Gouripur", "Haluaghat", "Ishwarganj", "Muktagachha", "Mymensingh Sadar", "Nandail", "Phulpur", "Trishal"));
        districtUpazilasMap.put("Netrokona", Arrays.asList("Atpara", "Barhatta", "Durgapur", "Khaliajuri", "Kalmakanda", "Kendua", "Madan", "Mohanganj", "Netrokona Sadar", "Purbadhala", "Susong Durgapur"));
        districtUpazilasMap.put("Sherpur", Arrays.asList("Jhenaigati", "Nakla", "Nalitabari", "Sherpur Sadar", "Sreebardi"));

        districtUpazilasMap.put("Bogra", Arrays.asList("Adamdighi", "Bogra Sadar", "Dhunat", "Dhupchanchia", "Gabtali", "Kahaloo", "Nandigram", "Sariakandi", "Shajahanpur", "Sherpur", "Shibganj", "Sonatala"));
        districtUpazilasMap.put("Chapainawabganj", Arrays.asList("Bholahat", "Gomastapur", "Nachole", "Chapainawabganj Sadar", "Shibganj"));
        districtUpazilasMap.put("Joypurhat", Arrays.asList("Akkelpur", "Joypurhat Sadar", "Kalai", "Khetlal", "Panchbibi"));
        districtUpazilasMap.put("Naogaon", Arrays.asList("Atrai", "Badalgachhi", "Dhamoirhat", "Manda", "Mohadevpur", "Naogaon Sadar", "Niamatpur", "Patnitala", "Porsha", "Raninagar", "Sapahar"));
        districtUpazilasMap.put("Natore", Arrays.asList("Bagatipara", "Baraigram", "Gurudaspur", "Lalpur", "Natore Sadar", "Singra"));
        districtUpazilasMap.put("Pabna", Arrays.asList("Atgharia", "Bera", "Bhangura", "Chatmohar", "Faridpur", "Ishwardi", "Pabna Sadar", "Santhia", "Sujanagar"));
        districtUpazilasMap.put("Rajshahi", Arrays.asList("Bagha", "Bagmara", "Boalia", "Charghat", "Durgapur", "Godagari", "Matihar", "Mohanpur", "Paba", "Puthia", "Rajpara", "Shah Mokdum", "Tanore"));
        districtUpazilasMap.put("Sirajganj", Arrays.asList("Belkuchi", "Chauhali", "Kamarkhanda", "Kazipur", "Raiganj", "Shahjadpur", "Sirajganj Sadar", "Tarash", "Ullahpara"));

        districtUpazilasMap.put("Dinajpur", Arrays.asList("Birampur", "Birganj", "Biral", "Bochaganj", "Chirirbandar", "Phulbari", "Ghoraghat", "Hakimpur", "Kaharole", "Khansama", "Dinajpur Sadar", "Nawabganj", "Parbatipur"));
        districtUpazilasMap.put("Gaibandha", Arrays.asList("Fulchhari", "Gaibandha Sadar", "Gobindaganj", "Palashbari", "Sadullapur", "Saghata", "Sundarganj"));
        districtUpazilasMap.put("Kurigram", Arrays.asList("Bhurungamari", "Char Rajibpur", "Chilmari", "Phulbari", "Kurigram Sadar", "Nageshwari", "Rajarhat", "Raomari", "Ulipur"));
        districtUpazilasMap.put("Lalmonirhat", Arrays.asList("Aditmari", "Hatibandha", "Kaliganj", "Lalmonirhat Sadar", "Patgram"));
        districtUpazilasMap.put("Nilphamari", Arrays.asList("Dimla", "Domar", "Jaldhaka", "Kishoreganj", "Nilphamari Sadar", "Saidpur"));
        districtUpazilasMap.put("Panchagarh", Arrays.asList("Atwari", "Boda", "Debiganj", "Panchagarh Sadar", "Tetulia"));
        districtUpazilasMap.put("Thakurgaon", Arrays.asList("Baliadangi", "Haripur", "Pirganj", "Ranisankail", "Thakurgaon Sadar"));

        districtUpazilasMap.put("Habiganj", Arrays.asList("Ajmiriganj", "Bahubal", "Baniachang", "Chunarughat", "Habiganj Sadar", "Lakhai", "Madhabpur", "Nabiganj"));
        districtUpazilasMap.put("Moulvibazar", Arrays.asList("Barlekha", "Juri", "Kamalganj", "Kulaura", "Moulvibazar Sadar", "Rajnagar", "Sreemangal"));
        districtUpazilasMap.put("Sunamganj", Arrays.asList("Bishwamvarpur", "Chhatak", "Dakshin Sunamganj", "Derai", "Dharampasha", "Dowarabazar", "Jagannathpur", "Jamalganj", "Sullah", "Sunamganj Sadar", "Tahirpur"));
        districtUpazilasMap.put("Sylhet", Arrays.asList("Balaganj", "Beanibazar", "Bishwanath", "Companiganj", "Fenchuganj", "Golapganj", "Gowainghat", "Jaintiapur", "Kanaighat", "Osmani Nagar", "Sylhet Sadar", "Zakiganj"));

    }


}

