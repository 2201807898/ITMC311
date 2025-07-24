package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    Button a1, a2, resetBtn;
    TextView pText, timerText;
    int num1, num2, points = 0;
    Random random = new Random();

    CountDownTimer countDownTimer;
    long timeLeftInMillis = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        a1 = findViewById(R.id.a1);
        a2 = findViewById(R.id.a2);
        resetBtn = findViewById(R.id.resetBtn);
        pText = findViewById(R.id.p);
        timerText = findViewById(R.id.timerText);

        roll();
        startTimer();

        a1.setOnClickListener(view -> checkAnswer(num1, num2));
        a2.setOnClickListener(view -> checkAnswer(num2, num1));

        resetBtn.setOnClickListener(view -> {
            points = 0;
            pText.setText("Points: 0");
            resetBtn.setVisibility(View.GONE);
            a1.setEnabled(true);
            a2.setEnabled(true);
            timeLeftInMillis = 30000;
            startTimer();
            roll();
        });
    }

    void checkAnswer(int chosen, int other) {
        if (chosen > other) {
            points++;
            Toast.makeText(this, "Correct 👏", Toast.LENGTH_SHORT).show();
        } else {
            points--;
            Toast.makeText(this, "Wrong ❌", Toast.LENGTH_SHORT).show();
        }

        pText.setText("Points: " + points);

        if (points >= 10) {
            Toast.makeText(this, "🎉 Well done! You reached 10 points!", Toast.LENGTH_LONG).show();
            a1.setEnabled(false);
            a2.setEnabled(false);
            resetBtn.setVisibility(View.VISIBLE);
            countDownTimer.cancel();
        } else {
            roll();
        }
    }

    void roll() {
        num1 = random.nextInt(100);
        do {
            num2 = random.nextInt(100);
        } while (num1 == num2);

        a1.setText(String.valueOf(num1));
        a2.setText(String.valueOf(num2));
    }

    void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerText.setText("Time: " + secondsLeft);
            }

            @Override
            public void onFinish() {
                timerText.setText("Time: 0");
                a1.setEnabled(false);
                a2.setEnabled(false);
                Toast.makeText(MainActivity.this, "⏰ Time's up!", Toast.LENGTH_LONG).show();
                resetBtn.setVisibility(View.VISIBLE);
            }
        }.start();
    }
}
