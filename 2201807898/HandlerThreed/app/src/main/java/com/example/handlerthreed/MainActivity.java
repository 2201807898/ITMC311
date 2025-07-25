package com.example.handlerthreed;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends Activity {

    private ProgressBar bar1;
    private ProgressBar bar2;
    private TextView msgWorking;
    private TextView msgReturned;

    private static final int MAX_SEC = 60;
    private volatile boolean isRunning = false;  // volatile لضمان التزامن بين الخيوط

    private int intTest = 0;
    private String strTest = "global value seen by all threads";

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String returnedValue = (String) msg.obj;

            msgReturned.setText("Returned by background thread:\n\n" + returnedValue);
            bar1.incrementProgressBy(2);

            int progress = bar1.getProgress();
            int max = bar1.getMax();

            if (progress >= MAX_SEC) {
                msgReturned.setText("Done \nBackground thread has been stopped");
                isRunning = false;
            }
            if (progress >= max) {
                msgWorking.setText("Done");
                bar1.setVisibility(View.INVISIBLE);
                bar2.setVisibility(View.INVISIBLE);
            } else {
                msgWorking.setText("Working... " + progress);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar1 = findViewById(R.id.progress);
        bar2 = findViewById(R.id.progress2);
        msgWorking = findViewById(R.id.TextView01);
        msgReturned = findViewById(R.id.TextView02);

        bar1.setMax(MAX_SEC);
        bar1.setProgress(0);

        strTest += "-01";
        intTest = 1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBackgroundThread();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;  // يوقف الـ thread بشكل آمن عند خروج النشاط
    }

    private void startBackgroundThread() {
        isRunning = true;

        new Thread(() -> {
            try {
                Random rnd = new Random();
                for (int i = 0; i < MAX_SEC && isRunning; i++) {
                    Thread.sleep(1000);

                    String data = "Thread Value: " + rnd.nextInt(101) + "\n" + strTest + " " + intTest;
                    intTest++;

                    if (isRunning) {
                        Message msg = handler.obtainMessage(1, data);
                        handler.sendMessage(msg);
                    }
                }
            } catch (InterruptedException e) {
                // يمكن هنا التعامل مع إيقاف الخيط بشكل أفضل
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
