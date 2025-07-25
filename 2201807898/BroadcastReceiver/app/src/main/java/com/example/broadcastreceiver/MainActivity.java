package com.example.broadcastreceiver;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textView;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            progressBar.setProgress(level);
            textView.setText("Battery Level: " + level + "%");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تهيئة العناصر مرة واحدة فقط
        progressBar = findViewById(R.id.progressbar);
        textView = findViewById(R.id.textfield);

        // تسجيل البث
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // إزالة تسجيل البث لتجنب تسرب الذاكرة
        unregisterReceiver(mBatInfoReceiver);
    }
}



