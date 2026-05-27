package com.example.agriscan.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agriscan.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 📄 GOOGLE DRIVE LINK
        TextView docs = findViewById(R.id.txtDocs);
        if (docs != null) {
            docs.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://drive.google.com/drive/folders/1Eu0CVOSGLwlvK6wdEj5O7ePUiRwVrUEy?usp=drive_link"));
                startActivity(intent);
            });
        }

        // 🔗 LINKEDIN BUTTON 1
        Button link1 = findViewById(R.id.link1);
        if (link1 != null) {
            link1.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.linkedin.com/in/rushikeshshinde4241/"));
                startActivity(intent);
            });
        }

        // 🔗 LINKEDIN BUTTON 2
        Button link2 = findViewById(R.id.link2);
        if (link2 != null) {
            link2.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.linkedin.com/in/harshal-mekhe-04a05131a/"));
                startActivity(intent);
            });
        }

        // 🔗 LINKEDIN BUTTON 3
        Button link3 = findViewById(R.id.link3);
        if (link3 != null) {
            link3.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.linkedin.com/in/ashmit-budhe-3a90363ba/"));
                startActivity(intent);
            });
        }

        // 🔗 LINKEDIN BUTTON 4
        Button link4 = findViewById(R.id.link4);
        if (link4 != null) {
            link4.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.linkedin.com/in/vedant-patil-1b036033b/"));
                startActivity(intent);
            });
        }


    }
}