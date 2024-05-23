package com.example.agrochain;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProtectionSchool extends AppCompatActivity {
    private TextView textViewCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protection_school);

        textViewCrop = findViewById(R.id.textViewCrop);

        textViewCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        setupUI();

    }
    private void setupUI() {
//      onclick
        LinearLayout btn1=findViewById(R.id.btn1);
        LinearLayout btn2=findViewById(R.id.btn2);
        LinearLayout btn3=findViewById(R.id.btn3);
        LinearLayout btn4=findViewById(R.id.btn4);
//      Head
        ImageView imageView = findViewById(R.id.crop_image_view);
        TextView textView = findViewById(R.id.crop_text_view);
//      H  b lbl 1
        TextView headLabel1 = findViewById(R.id.headLabel1);
        TextView baseLabel1 = findViewById(R.id.baseLabel1);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int imageResource = extras.getInt("imageResource");
            String labelText = extras.getString("labelText");

            String Hlbl = extras.getString("headLabel");
            String Blbl = extras.getString("baseLabel");

            imageView.setImageResource(imageResource);
            textView.setText(labelText);
            headLabel1.setText(Hlbl);
            baseLabel1.setText(Blbl);
        }



//      btn1
        ImageView imgbtn1 = findViewById(R.id.imgbtn1);
        TextView lblbtn1 = findViewById(R.id.lblbtn1);
        extras = getIntent().getExtras();
        if (extras != null) {
            int ImgBTN = extras.getInt("imagebutton1");
            String LblBTN = extras.getString("labelbutton1");

            imgbtn1.setImageResource(ImgBTN);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                lblbtn1.setText(Html.fromHtml(LblBTN, Html.FROM_HTML_MODE_LEGACY));
            } else {
                lblbtn1.setText(Html.fromHtml(LblBTN));
            }
//            lblbtn1.setText(LblBTN);

            //      onclick
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProtectionSchool.this, DetailsActivity2.class);
                    intent.putExtra("img1", ImgBTN);
                    intent.putExtra("lbl1", LblBTN);
                    intent.putExtra("vc1", R.raw.v2);
                    startActivity(intent);
                }
            });
        }


//      btn2
        ImageView imgbtn2 = findViewById(R.id.imgbtn2);
        TextView lblbtn2 = findViewById(R.id.lblbtn2);
        extras = getIntent().getExtras();
        if (extras != null) {
            int ImgBTN2 = extras.getInt("imagebutton2");
            String LblBTN2 = extras.getString("labelbutton2");

            imgbtn2.setImageResource(ImgBTN2);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                lblbtn2.setText(Html.fromHtml(LblBTN2, Html.FROM_HTML_MODE_LEGACY));
            } else {
                lblbtn2.setText(Html.fromHtml(LblBTN2));
            }
        }
//      btn3
        ImageView imgbtn3 = findViewById(R.id.imgbtn3);
        TextView lblbtn3 = findViewById(R.id.lblbtn3);
        extras = getIntent().getExtras();
        if (extras != null) {
            int ImgBTN3 = extras.getInt("imagebutton3");
            String LblBTN3 = extras.getString("labelbutton3");

            imgbtn3.setImageResource(ImgBTN3);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                lblbtn3.setText(Html.fromHtml(LblBTN3, Html.FROM_HTML_MODE_LEGACY));
            } else {
                lblbtn3.setText(Html.fromHtml(LblBTN3));
            }
        }
//      btn4
        ImageView imgbtn4 = findViewById(R.id.imgbtn4);
        TextView lblbtn4 = findViewById(R.id.lblbtn4);
        extras = getIntent().getExtras();
        if (extras != null) {
            int ImgBTN4 = extras.getInt("imagebutton4");
            String LblBTN4 = extras.getString("labelbutton4");

            imgbtn4.setImageResource(ImgBTN4);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                lblbtn4.setText(Html.fromHtml(LblBTN4, Html.FROM_HTML_MODE_LEGACY));
            } else {
                lblbtn4.setText(Html.fromHtml(LblBTN4));
            }
        }



    }
}
