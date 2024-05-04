package com.example.agrochain;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class WeatherDetailsActivity extends AppCompatActivity {
    private TextView detailedWeatherInfo;
    private TextView forecastWeatherInfo;
    private final static String API_KEY = "9b05a6b1c536afcd22f2a0fd1f6768e7";
    private final static String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid=" + API_KEY + "&units=metric";
    private final static String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}&appid=" + API_KEY + "&units=metric&cnt=2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);
        detailedWeatherInfo = findViewById(R.id.detailedWeatherInfo);
        forecastWeatherInfo = findViewById(R.id.forecastWeatherInfo);

        fetchWeatherDetails();
        fetchForecastDetails();
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
                            double humidity = main.getDouble("humidity");
                            JSONObject wind = jsonObject.getJSONObject("wind");
                            double windSpeed = wind.getDouble("speed");
                            JSONArray weatherArray = jsonObject.getJSONArray("weather");
                            JSONObject weather = weatherArray.getJSONObject(0);
                            String description = weather.getString("description");
                            String weatherCondition = determineWeatherCondition(description);

                            detailedWeatherInfo.setText("Today's Weather:\nTemperature: " + temp + "°C\n"
                                    + "Humidity: " + humidity + "%\n"
                                    + "Wind Speed: " + windSpeed + "m/s\n"
                                    + "Condition: " + weatherCondition);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            detailedWeatherInfo.setText("Failed to parse weather data.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Log.e("WeatherApp", "Error Response code: " + error.networkResponse.statusCode);
                }
                detailedWeatherInfo.setText("Failed to get weather data: " + error.toString());
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
                                JSONObject forecast = list.getJSONObject(1);
                                JSONObject main = forecast.getJSONObject("main");
                                double temp = main.getDouble("temp");
                                double humidity = main.getDouble("humidity");
                                JSONObject wind = forecast.getJSONObject("wind");
                                double windSpeed = wind.getDouble("speed");

                                JSONObject weather = forecast.getJSONArray("weather").getJSONObject(0);
                                String description = weather.getString("description");
                                String weatherCondition = determineWeatherCondition(description);
                                forecastWeatherInfo.setText("Tomorrow's Forecast:\nTemperature: " + temp + "°C\n"
                                        + "Humidity: " + humidity + "%\n"
                                        + "Wind Speed: " + windSpeed + "m/s\n"
                                        + "Condition: " + weatherCondition);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            forecastWeatherInfo.setText("Failed to parse forecast data.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Log.e("WeatherApp", "Forecast Error Response code: " + error.networkResponse.statusCode);
                }
                forecastWeatherInfo.setText("Failed to get forecast data: " + error.toString());
            }
        });

        queue.add(stringRequest);
    }
    private String determineWeatherCondition(String description) {
        if (description.contains("rain") || description.contains("drizzle")) {
            return "Bad day - Expect rain, take an umbrella.";
        } else if (description.contains("clear")) {
            return "Good day - Clear skies.";
        } else if (description.contains("clouds")) {
            return "Moderate day - Cloudy skies.";
        } else if (description.contains("thunderstorm")) {
            return "Bad day - Thunderstorms expected.";
        }  else {
            return "No significant weather changes expected.";
        }
    }
}
