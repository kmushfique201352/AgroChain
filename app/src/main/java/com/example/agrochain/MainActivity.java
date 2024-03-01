package com.example.agrochain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        ImageView textViewAgriChain = findViewById(R.id.imageViewLogo);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        Animation fadeInStay = AnimationUtils.loadAnimation(this, R.anim.fade_in_and_hold);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeInButtons = AnimationUtils.loadAnimation(this, R.anim.fade_in_buttons);

        imageView.setVisibility(View.VISIBLE);
        textViewAgriChain.setVisibility(View.VISIBLE);
        imageView.startAnimation(fadeInStay);
        textViewAgriChain.startAnimation(fadeInStay);

        fadeInStay.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textViewAgriChain.startAnimation(slideUp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonRegister.startAnimation(fadeInButtons);
                buttonLogin.startAnimation(fadeInButtons);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeInButtons.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonRegister.setVisibility(View.VISIBLE);
                buttonLogin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
