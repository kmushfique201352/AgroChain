package com.example.agrochain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        FrameLayout tomatoFrame = findViewById(R.id.tomato_frame);


//        FrameLayout watermelonFrame = findViewById(R.id.watermelon_frame);
//        FrameLayout onionFrame = findViewById(R.id.onion_frame);
//        FrameLayout garlicFrame = findViewById(R.id.garlic_frame);
//        FrameLayout cabbageFrame = findViewById(R.id.cabbage_frame);
//        FrameLayout cucumberFrame = findViewById(R.id.cucumber_frame);
//        FrameLayout potatoFrame = findViewById(R.id.potato_frame);
//        FrameLayout wheatFrame = findViewById(R.id.wheat_frame);
//        FrameLayout carrotsFrame = findViewById(R.id.carrots_frame);
//        FrameLayout gingerFrame = findViewById(R.id.ginger_frame);
//        FrameLayout sunflowerFrame = findViewById(R.id.sunflower_frame);

        tomatoFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
                intent.putExtra("imageResource", R.drawable.tomato);
                intent.putExtra("labelText", "টমেটো");
//              Crop
                intent.putExtra("headLabel","রোপণের ৩-৪ সপ্তাহ আগে");
                intent.putExtra("baseLabel","");
//                btn1
                intent.putExtra("imagebutton1",R.drawable.imgbtn11);
                String dynamicHtmlText1 = "<b>সাইট নির্বাচন এবং প্রাথমিক জমি প্রস্তুতি</b><br/><br/>" +
                        "<b>সংক্ষেপে</b><br/>" +
                        "এই কাজটি সাইট নির্বাচন এবং ভূমি প্রস্তুতির জন্য।<br/><br/>" +
                        "<b>টাস্ক নির্দেশিকা</b><br/>" +
                        "১) উপযুক্ত মাটি এবং আবহাওয়ার অবস্থা টমেটো চাষের জন্য সবচেয়ে গুরুত্বপূর্ণ কারণগুলি<br/>" +
                        "২) টমেটো হালকা বালি থেকে ভারী মাটিতে সব ধরণের মাটিতে বাড়তে পারে।<br/>" +
                        "৩) হালকা মাটি প্রারম্ভিক ফসলের জন্য ভাল, অন্যদিকে কাদামাটি লোম এবং সিল্ট-লোম মাটি ভারী ফসলের জন্য উপযুক্ত<br/>" +
                        "৪) টমেটো সবচেয়ে ভালো বাড়ে যখন মাটির পিএইচ ৬.০ থেকে ৭.০ হয়।<br/>" +
                        "৫) যদি মাটি অম্লীয় হয়, তবে চুন দেওয়ার প্রয়োজন হয়<br/>" +
                        "৬) পর্যাপ্ত জলের উপলব্ধতা, যদি কোনো আগের ফসলের অবশিষ্টাংশ থাকে তাহলে তা অপসারণ, জমি চাষ এবং জমি সমতল করা<br/>" +
                        "৭) যদি ক্ষেতে কোনো কাণ্ডকীর্তি থাকে তাহলে তা অপসারণ করা।<br/><br/>" +
                        "<b>আবহাওয়ার শর্তাবলী:</b><br/>" +
                        "১) টমেটো একটি উষ্ণ মৌসুমের ফসল।<br/>" +
                        "২) এই ফসল গড় মাসিক তাপমাত্রা ২১ degree Celcius থেকে ২৩ degree Celcius এ ভালো ফলন দেয় <br/>" +
                        "৩) তাপমাত্রা এবং আলোর তীব্রতা ফলের সেট, রংদান এবং ফলের পুষ্টিমূল্যে প্রভাব ফেলে<br/>" +
                        "৪) দীর্ঘ শুষ্ক মৌসুম এবং প্রচুর বৃষ্টি উভয়ই বৃদ্ধি ও ফলনে ক্ষতিকর প্রভাব ফেলে।<br/>";
                intent.putExtra("labelbutton1", dynamicHtmlText1);


//                btn2
                intent.putExtra("imagebutton2",R.drawable.imgbtn21);
                String dynamicHtmlText2 ="<b>বৈচিত্র্য নির্বাচন করা, বীজ সংগ্রহ করা বা চারার জন্য অর্ডার দেওয়া এবং ফাঁক পূরণের জন্য কিছু অতিরিক্ত গাছের পরিকল্পনা করতে পারে।</b><br/><br/>";
                intent.putExtra("labelbutton2", dynamicHtmlText2);
//                btn3
                intent.putExtra("imagebutton3",R.drawable.imgbtn31);
                String dynamicHtmlText3 ="<b>বপন এবং নার্সারি বৃদ্ধি</b><br/><br/>";
                intent.putExtra("labelbutton3", dynamicHtmlText3);
//                btn4
                intent.putExtra("imagebutton4",R.drawable.imgbtn41);
                String dynamicHtmlText4 ="<b>ফার্ম ইয়ার্ড সার সংগ্রহ</b><br/><br/>";
                intent.putExtra("labelbutton4", dynamicHtmlText4);


                startActivity(intent);
            }
        });

//        watermelonFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.watermelon);
//                intent.putExtra("labelText", "Watermelon");
//                startActivity(intent);
//            }
//        });
//
//        onionFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.onion);
//                intent.putExtra("labelText", "Onion");
//                startActivity(intent);
//            }
//        });
//
//        garlicFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.garlic);
//                intent.putExtra("labelText", "Garlic");
//                startActivity(intent);
//            }
//        });
//
//        cabbageFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.cabbage);
//                intent.putExtra("labelText", "Cabbage");
//                startActivity(intent);
//            }
//        });
//
//        cucumberFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.cucumber);
//                intent.putExtra("labelText", "Cucumber");
//                startActivity(intent);
//            }
//        });
//
//        potatoFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.potato);
//                intent.putExtra("labelText", "Potato");
//                startActivity(intent);
//            }
//        });
//
//        wheatFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.wheat);
//                intent.putExtra("labelText", "Wheat");
//                startActivity(intent);
//            }
//        });
//
//        carrotsFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.carrots);
//                intent.putExtra("labelText", "Carrot");
//                startActivity(intent);
//            }
//        });
//
//        gingerFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.ginger);
//                intent.putExtra("labelText", "Ginger");
//                startActivity(intent);
//            }
//        });
//
//        sunflowerFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(CategoriesActivity.this, CropSchool.class);
//                intent.putExtra("imageResource", R.drawable.sunflower);
//                intent.putExtra("labelText", "Sunflower");
//                startActivity(intent);
//            }
//        });




    }
}
