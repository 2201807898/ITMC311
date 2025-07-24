package com.example.facedetectionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ ربط الواجهة مع ملف XML الذي أنشأته
        setContentView(R.layout.activity_main);

        // ✅ زر "ابدأ" للانتقال إلى واجهة الكاميرا
        Button startButton = findViewById(R.id.startCameraButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // فتح CameraActivity عند الضغط على زر "ابدأ"
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
    }
}
