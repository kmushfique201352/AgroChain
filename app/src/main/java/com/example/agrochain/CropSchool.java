package com.example.agrochain;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CropSchool extends AppCompatActivity {
    private TextView textViewProtection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_school);

        textViewProtection = findViewById(R.id.textViewProtection);

        textViewProtection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CropSchool.this, ProtectionSchool.class);
                intent.putExtra("imageResource", R.drawable.tomato);
                intent.putExtra("labelText", "টমেটো");
//              Crop
                intent.putExtra("headLabel","এই পর্যায়ে নিম্নলিখিত ঘটতে পারে");
                intent.putExtra("baseLabel","");
//                btn1
                intent.putExtra("imagebutton1",R.drawable.imgbtn1p);
                String dynamicHtmlText1 = "<b>ব্যাকটেরিয়াল স্পেক</b><br/><br/>" +
                        "<b>নিম্নলিখিত লক্ষণগুলির জন্য পর্যবেক্ষণ করুন</b><br/>" +
                        "        ১. পাতায়: তরুণ পাতাগুলো বেশি সংবেদনশীল হয় তবে প্রথমে পুরানো পাতায় লক্ষণগুলি প্রকাশ পায় যা ছোট (প্রায় অর্ধ ইঞ্চি চওড়া) জল-সিক্ত গাঢ় বাদামি থেকে কালো দাগ দিয়ে একটি হলুদ হ্যালো দিয়ে ঘেরা হয়।<br/><br/>"+
                        "        ২. কিন্তু তরুণ বিকাশমান পাতাগুলিতে প্রায়শই দাগগুলি বড় হয়, অনিয়মিত আকারের হয় এবং পাতাগুলিকে বিকৃত এবং মাঝে মাঝে ছিঁড়ে যেতে পারে।"+
                        "        ৩. পরবর্তী পর্যায়ে, দাগগুলি একত্রিত হয়ে বড় অনিয়মিত বাদামি এলাকা তৈরি করে।"+
                        "        ৪. পাতার প্রান্তগুলি বাদামি হয়ে যায়, কখনো কখনো পাতায় নীচের দিকে বাদামি টিস্যুর একটি খাঁজ তৈরি করে।"+
                        "৫. গুরুতরভাবে প্রভাবিত পাতাগুলি হলুদ হয়ে গিয়ে শেষ পর্যন্ত মরে যায়। কাণ্ডে: কাণ্ড এবং পাতার ডগায় ছোট ওভাল আকারের দাগ দেখা যায়।"+
                        "৬. পরবর্তীতে দাগগুলি একত্রিত হয়ে বড় বাদামি টিস্যুর এলাকা তৈরি করে। ফলে: ফলে খুব ছোট (সাধারণত অর্ধ ইঞ্চির কম) গাঢ় বাদামি দাগ পড়ে এবং সাধারণত নখ দিয়ে ঘষে ফেলা যায়।"+
                        "৮. শুধুমাত্র সবুজ ফলগুলি এই সংক্রমণের প্রতি সংবেদনশীল। মাঝে মাঝে গ্রিনহাউসে চারা গাছে লক্ষণগুলি দেখা দেয় যখন দূষিত বীজ রোপণ করা হয় অথবা যখন পর্যাপ্ত স্যানিটেশন নেই।";
                intent.putExtra("labelbutton1", dynamicHtmlText1);


//                btn2
                intent.putExtra("imagebutton2",R.drawable.imgbtn5p);
                String dynamicHtmlText2 ="<b>লিফ মাইনার</b><br/><br/>";
                intent.putExtra("labelbutton2", dynamicHtmlText2);
//                btn3
                intent.putExtra("imagebutton3",R.drawable.imgbtn3p);
                String dynamicHtmlText3 ="<b>লিফ কার্ল ভাইরাস</b><br/><br/>";
                intent.putExtra("labelbutton3", dynamicHtmlText3);
//                btn4
                intent.putExtra("imagebutton4",R.drawable.imgbtn4p);
                String dynamicHtmlText4 ="<b>হলুদ মোজাইক</b><br/><br/>";
                intent.putExtra("labelbutton4", dynamicHtmlText4);

                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        setupUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void setupUI() {
//      onclick
        LinearLayout btn1=findViewById(R.id.btn1);
        LinearLayout btn2=findViewById(R.id.btn2);
        LinearLayout btn3=findViewById(R.id.btn3);
        LinearLayout btn4=findViewById(R.id.btn4);
//      Head Section
        ImageView imageView = findViewById(R.id.crop_image_view);
        TextView textView = findViewById(R.id.crop_text_view);
//      H b lbl 1
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
                    Intent intent = new Intent(CropSchool.this, DetailActivity.class);
                    intent.putExtra("img1", ImgBTN);
                    intent.putExtra("lbl1", LblBTN);
                    intent.putExtra("vc1", R.raw.v1);
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
