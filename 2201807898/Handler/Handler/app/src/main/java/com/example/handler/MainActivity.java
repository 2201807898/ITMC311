package com.example.handler;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

public class MainActivity extends Activity {
    ProgressBar myBar;
    TextView lblTopCaption;
    EditText txtBox1;
    Button btnDoSomething;
    int accum= 0;
    long startingMills= System.currentTimeMillis();
    String PATIENCE= "Some important data is been collected now. "+"\nPleasebe patient. ";


    Handler myHandler= new Handler();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lblTopCaption= (TextView)findViewById(R.id.lblTopCaption);
        myBar= (ProgressBar) findViewById(R.id.myBar);
        myBar.setMax(100);
        txtBox1= (EditText) findViewById(R.id.txtBox1);
        txtBox1.setHint("Foreground distraction. Enter some data here");
        btnDoSomething= (Button)findViewById(R.id.btnDoSomething);
        btnDoSomething.setOnClickListener(new OnClickListener() {
            @Override
                    public void onClick(View v) {
                Editable txt = txtBox1.getText();
                Toast.makeText(getBaseContext(), "You said >> "+ txt,Toast.LENGTH_LONG).show();
            }//onClick
        });//setOnClickListener
    }//onCreate

    @Override
    protected void onStart() {
        super.onStart();
// create background thread were the busy work will be done
        Thread myThread1 = new Thread(backgroundTask, "backAlias1");
        myThread1.start();
        myBar.incrementProgressBy(0);
    }
// this is the foreground "Runnable" object responsible for GUI updates
    private Runnable foregroundTask= new Runnable() {
        @Override
                public void run() {
            try{
                int progressStep= 5;
                lblTopCaption.setText(PATIENCE+ "\nTotalsec. so far: "+
                        (System.currentTimeMillis() -startingMills) / 1000 );
                myBar.incrementProgressBy(progressStep);
                accum+= progressStep;
                if(accum>= myBar.getMax()){
                    lblTopCaption.setText("Background work is OVER!");
                    myBar.setVisibility(View.INVISIBLE);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }//run
    }; //foregroundTask



//this is the "Runnable" object that executes the background thread
    private Runnable backgroundTask= new Runnable() {
        @Override
                public void run() {
//busy work goes here...
            try{
                for(int n=0; n<20; n++) {
//this simulates 1 sec. of busy activity
                    Thread.sleep(1000);
//now talk to the main thread
                    myHandler.post(foregroundTask);
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }//run
    };//backgroundTask
}//ThreadsPosting
