package com.example.handler;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private ProgressBar progressBar;
    private TextView captionTextView;
    private EditText inputEditText;
    private Button actionButton;

    private int progressAccumulator = 0;
    private long startTimeMillis;
    private final String PATIENCE_MESSAGE = "Some important data is being collected now.\nPlease be patient.";

    private final Handler uiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupButtonListener();
    }

    private void initViews() {
        captionTextView = findViewById(R.id.lblTopCaption);
        progressBar = findViewById(R.id.myBar);
        progressBar.setMax(100);
        inputEditText = findViewById(R.id.txtBox1);
        inputEditText.setHint("Foreground distraction. Enter some data here");
        actionButton = findViewById(R.id.btnDoSomething);
    }

    private void setupButtonListener() {
        actionButton.setOnClickListener(v -> {
            Editable text = inputEditText.getText();
            Toast.makeText(getBaseContext(), "You said >> " + text, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressAccumulator = 0;
        startTimeMillis = System.currentTimeMillis();
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        Thread backgroundThread = new Thread(backgroundTask, "BackgroundWorker");
        backgroundThread.start();
    }

    private final Runnable foregroundTask = () -> {
        int step = 5;
        long elapsedSec = (System.currentTimeMillis() - startTimeMillis) / 1000;
        captionTextView.setText(PATIENCE_MESSAGE + "\nTotal sec. so far: " + elapsedSec);

        progressBar.incrementProgressBy(step);
        progressAccumulator += step;

        if (progressAccumulator >= progressBar.getMax()) {
            captionTextView.setText("Background work is OVER!");
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    private final Runnable backgroundTask = () -> {
        try {
            for (int i = 0; i < 20; i++) {
                Thread.sleep(1000);
                uiHandler.post(foregroundTask);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
}
