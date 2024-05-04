package com.example.agrochain;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FarmerDashboardActivity extends AppCompatActivity {

    private ImageView imgFarmerProfile;
    private FirebaseFirestore db;
    private String userID;
    private ImageView fabCreatePost,imgHome;
    private LinearLayout layoutPosts;
    private FirebaseFirestore firestore;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView weatherButton,forecastButton;
    private Button btnHelp;
    private final static String API_KEY = "9b05a6b1c536afcd22f2a0fd1f6768e7";
    private final static String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid=" + API_KEY + "&units=metric";
    private final static String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}&appid=" + API_KEY + "&units=metric&cnt=2";



    private void market() {
        ImageView marketPlaceButton = findViewById(R.id.imgMarketPlace);
        marketPlaceButton.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, MarketplaceActivity.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
        });
    }
    private void cropCare() {
        ImageView marketPlaceButton = findViewById(R.id.cropCare);
        marketPlaceButton.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, CategoriesActivity.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        userID = getIntent().getStringExtra("userID");

        weatherButton = findViewById(R.id.weatherButton);
        forecastButton = findViewById(R.id.forecastButton);
        btnHelp = findViewById(R.id.btnHelp);

        createNotificationChannel();
        fetchWeatherDetails();
        fetchForecastDetails();

        checkForTotalCost();
        market();
        cropCare();

        ImageView newsBtn=findViewById(R.id.imgNews);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
            }
        });

        layoutPosts = findViewById(R.id.layoutPosts);
        firestore = FirebaseFirestore.getInstance();

        loadPosts();
        loadFreePosts();

        imgFarmerProfile = findViewById(R.id.imgFarmerProfile);
        db = FirebaseFirestore.getInstance();

        listenForNotifications();

        if (userID != null) {
            imgFarmerProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FarmerDashboardActivity.this, EditProfileActivity.class);
                intent.putExtra("userID", userID);
                Log.d("LoginActivity", "Passing userID: " + userID);

                startActivity(intent);
            });
            Toast.makeText(this, "User:1"+userID, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
        }
        fabCreatePost=findViewById(R.id.fabCreatePost);
        fabCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FarmerDashboardActivity.this, PostControlActivity.class);
//                intent.putExtra("UserID",userID);
                startActivity(intent);
            }
        });
        newsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FarmerDashboardActivity.this,NewsViewActivity.class);
                startActivity(intent);
            }
        });
        ImageView imgNotification = findViewById(R.id.imgNotification);
        imgNotification.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, NotificationActivity.class);
            intent.putExtra("userID", userID);
            Log.d("FarmerDashboardActivity", "Passing userID: " + userID);
            startActivity(intent);
        });
        weatherButton.setOnClickListener(v -> {
            // Intent to start WeatherDetailsActivity
            Intent intent = new Intent(FarmerDashboardActivity.this, WeatherDetailsActivity.class);
            startActivity(intent);
        });
        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, HelpActivity.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
        });
    }

    private void checkForTotalCost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("FAR").document(userID).collection("NOTIFICATION")
                .whereEqualTo("status", "pending");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        if (document.contains("totalCost")) {
                            String totalCost = document.getString("totalCost");
                            String price = document.contains("price") ? String.valueOf(document.getDouble("price")) : "Not available";
                            Toast.makeText(FarmerDashboardActivity.this, "Total Cost exists: " + totalCost, Toast.LENGTH_LONG).show();
                            sendCostNotification(price,document.getId());
                        } else {
                            Toast.makeText(FarmerDashboardActivity.this, "No totalCost field found", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.d("DocumentFetch", "No such document");
                    Toast.makeText(FarmerDashboardActivity.this, "Document does not exist", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("DocumentFetch", "get failed with ", task.getException());
                Toast.makeText(FarmerDashboardActivity.this, "Error fetching document: " + task.getException(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void sendCostNotification(String totalCost, String docId) {
        Log.d("NotificationSetup", "Sending notification for totalCost: " + totalCost);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weatherAlert")
                .setSmallIcon(R.drawable.order)
                .setContentTitle("Total Cost Alert")
                .setContentText("Your Order has been placed with a Total Cost of " + totalCost + " TK")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build()); // Unique ID for each notification
        DocumentReference notificationRef = FirebaseFirestore.getInstance()
                .collection("FAR").document(userID).collection("NOTIFICATION").document(docId);
        notificationRef.update("status", "delivered")
                .addOnSuccessListener(aVoid -> Log.d("Firestore Update", "Document successfully updated!"))
                .addOnFailureListener(e -> Log.w("Firestore Update", "Error updating document", e));
    }














    public void saveUserID(String userID) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserID", userID);
        editor.apply();
    }
    private void fetchWeatherDetails() {
        String url = WEATHER_URL.replace("{lat}", "23.7731731").replace("{lon}", "90.3245001");

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject main = jsonObject.getJSONObject("main");
                            double temp = main.getDouble("temp");
                            weatherButton.setText(temp + "°C");

                            if (temp > 30) {
                                sendHighTemperatureWarning(temp);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            weatherButton.setText("Failed to parse weather data.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                weatherButton.setText("Failed to get weather data.");
            }
        });

        queue.add(stringRequest);
    }
    private void fetchForecastDetails() {
        String url = FORECAST_URL.replace("{lat}", "23.7731731").replace("{lon}", "90.3245001");

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray list = jsonObject.getJSONArray("list");
                            if (list.length() > 0) {
                                JSONObject forecast = list.getJSONObject(1); // Assuming index 1 is tomorrow
                                JSONObject main = forecast.getJSONObject("main");
                                double temp = main.getDouble("temp");
                                double humidity = main.getDouble("humidity");
                                JSONObject wind = forecast.getJSONObject("wind");
                                double windSpeed = wind.getDouble("speed");
                                JSONObject weather = forecast.getJSONArray("weather").getJSONObject(0);
                                String description = weather.getString("description");

                                String forecastText = temp + "°C\n"
                                        + "Humidity: " + humidity + "%\n"
                                        + "Wind Speed: " + windSpeed + " m/s\n"
                                        + "Condition: " + description;
                                forecastButton.setText(forecastText);

                                sendForecastNotification(description, forecastText);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            forecastButton.setText("Failed to parse forecast data.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                forecastButton.setText("Failed to get forecast data: " + error.toString());
            }
        });

        queue.add(stringRequest);
    }


    private void loadPosts() {
        firestore.collectionGroup("POST")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutPosts.removeAllViews();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(FarmerDashboardActivity.this, "No posts available.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                            DocumentReference userRef = postSnapshot.getReference().getParent().getParent();
                            if (userRef != null) {
                                userRef.get().addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        String userName = userSnapshot.getString("name");
                                        String userAddress = userSnapshot.getString("division") + ", "
                                                + userSnapshot.getString("district") + ", "
                                                + userSnapshot.getString("upazila");
                                        String postDetails = formatPostDetails(postSnapshot,userAddress);
                                        String imageUrl = postSnapshot.getString("imageUrl");
                                        String postDate = postSnapshot.getId();
                                        String userProfileImageUrl = userSnapshot.getString("profilePictureUrl");
                                        addPostView(userName, postDetails, imageUrl,postDate,userProfileImageUrl);
                                    }
                                });
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FarmerDashboardActivity.this, "Error loading posts.", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
    private void loadFreePosts() {
        FirebaseFirestore.getInstance().collection("GOV")
                .document("GOVA-CyUsLz").collection("FREE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutPosts.removeAllViews();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        View postView = LayoutInflater.from(this).inflate(R.layout.item_post_free, layoutPosts, false);
                        ((TextView) postView.findViewById(R.id.freeItemName)).setText(document.getString("name"));
                        ((TextView) postView.findViewById(R.id.freeItemCollectionPoint)).setText(document.getString("collectionPoint"));
                        ((TextView) postView.findViewById(R.id.freeItemQuantity)).setText(document.getString("quantity"));
                        ((TextView) postView.findViewById(R.id.freeItemNote)).setText(document.getString("note"));

                        String imageUrl = document.getString("image");
                        ImageView imageView = postView.findViewById(R.id.freeItemImage);
                        Glide.with(this).load(imageUrl).into(imageView);

                        layoutPosts.addView(postView);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(FarmerDashboardActivity.this, "Error loading free posts.", Toast.LENGTH_SHORT).show());
    }


    private String formatPostDetails(QueryDocumentSnapshot postSnapshot, String userAddress) {
        return "com.example.agrochain.Product: " + postSnapshot.getString("product") + "\n" +
                "Price: " + postSnapshot.getString("price") + "\n" +
                "Quantity: " + postSnapshot.getString("quantity") + "\n" +
                "Release Date: " + postSnapshot.getString("releaseDate") + "\n" +
                "Address: " + userAddress + "\n" +
                "Note: " + postSnapshot.getString("note");
    }


    private void addPostView(String userName, String postDetails, String imageUrl, String postDate,String profilePictureUrl) {
        View postView = LayoutInflater.from(this).inflate(R.layout.item_post, layoutPosts, false);
        TextView userNameView = postView.findViewById(R.id.postUserName);
        TextView postDetailsView = postView.findViewById(R.id.postDetails);
        ImageView postImageView = postView.findViewById(R.id.postImage);
        TextView postDateView=postView.findViewById(R.id.postDate);
        de.hdodenhof.circleimageview.CircleImageView userImageView = postView.findViewById(R.id.postUserImage);

        userNameView.setText(userName);
        postDetailsView.setText(postDetails);

        Glide.with(this).load(imageUrl).into(postImageView);

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        try {
            Date date = originalFormat.parse(postDate);
            String formattedDate = targetFormat.format(date);
            postDateView.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            postDateView.setText(postDate);
        }


        postImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));
        postImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(imageUrl).into(postImageView);

        String userProfileImageUrl = profilePictureUrl;
        Glide.with(this).load(userProfileImageUrl).placeholder(R.drawable.default_profile).into(userImageView);
        layoutPosts.addView(postView);
    }


    private String getUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }
    private void listenForNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (userID == null) {
            Log.e("FarmerDashboard", "UserID is null, cannot listen for notifications");
            return;
        }
        db.collection("FAR").document(userID).collection("NOTIFICATIONS")
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Notifications", "Listen failed.", e);
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        final int unreadCount = snapshots.size();
                        runOnUiThread(() -> updateNotificationIcon(unreadCount));
                    } else {
                        runOnUiThread(() -> updateNotificationIcon(0));
                    }
                });
    }

    private void updateNotificationIcon(int unreadCount) {
        Log.d("NotificationUpdate", "Unread Count: " + unreadCount);
        TextView tvNotificationCount = findViewById(R.id.tvNotificationCount);
        if (unreadCount > 0) {
            tvNotificationCount.setText(String.valueOf(unreadCount));
            tvNotificationCount.setVisibility(View.VISIBLE);
        } else {
            tvNotificationCount.setVisibility(View.GONE);
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            NotificationChannel highTempChannel = new NotificationChannel(
                    "highTemperature",
                    "High Temperature Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            highTempChannel.setDescription("Notifies when the temperature exceeds a certain threshold.");
            notificationManager.createNotificationChannel(highTempChannel);

            NotificationChannel forecastChannel = new NotificationChannel(
                    "weatherForecast",
                    "Weather Forecast",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            forecastChannel.setDescription("Daily weather forecast notifications.");
            notificationManager.createNotificationChannel(forecastChannel);

            NotificationChannel channel = new NotificationChannel(
                    "costAlertChannel",
                    "Cost Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for cost-related alerts");

            NotificationManager notificationManagerx = getSystemService(NotificationManager.class);
            notificationManagerx.createNotificationChannel(channel);



//            CharSequence name = "WeatherAlertChannel";
//            String description = "Channel for Weather Alerts";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel("weatherAlert", name, importance);
//            channel.setDescription(description);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
        }
    }


    private void sendHighTemperatureWarning(double temp) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weatherAlert")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("High Temperature Warning")
                .setContentText("Temperature is unusually high at " + temp + "°C. Stay cool and hydrated.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
    private void sendForecastNotification(String description, String message) {
        if (description.contains("rain") || description.contains("thunderstorm") || description.contains("snow")) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weatherAlert")
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Weather Forecast Alert")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            notificationManager.notify(2, builder.build());
        } else if (description.contains("clear")) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weatherAlert")
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Weather Forecast Alert")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            notificationManager.notify(2, builder.build());
        }else if (description.contains("clouds")) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weatherAlert")
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Weather Forecast Alert")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            notificationManager.notify(2, builder.build());
        }
    }







}
