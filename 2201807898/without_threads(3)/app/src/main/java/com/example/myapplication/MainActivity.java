package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {
    private TextView myTextView;
    private View rootLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.myTextView);
        rootLayout = findViewById(R.id.rootLayout);

    }

    // استدعي هذه الدالة من زر في الواجهة
    public void buttonClick(View view) {
        long endTime = System.currentTimeMillis() + 20 * 1000; // 20 ثانية

        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                    // تجاهل الاستثناء
                }
            }
        }

    }

}