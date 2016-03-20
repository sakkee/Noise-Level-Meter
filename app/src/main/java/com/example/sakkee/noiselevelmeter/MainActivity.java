package com.example.sakkee.noiselevelmeter;

import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;
import java.lang.String;

import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.graphics.Color;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    //soundMeter sensor
    private soundMeter sensor;
    //Main Menu background
    private static RelativeLayout mainMenu;
    //Delay of the main timer
    private int delay = 0;
    //Repeat the main timer every X ms
    private int period = 100;
    //Keep count on the timer calls, used to calculate the average
    private int timerCount = 0;
    //Average dB for the measured decibels
    private double average = 0;
    //Highest measured dB
    private double highest = 0;
    //Whether user has paused or not
    private boolean measuring = true;
    //Visual progress bar for decibel display
    public static ProgressBar progressBar;
    //Background color
    public static String bgColor = "#FFAAAA";
    //Decibel bar color
    public static String barColor = "#FF0000";
    //Preference handler
    public static SharedPreferences prefs;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add the main menu on screen
        setContentView(R.layout.activity_main);

        //Read preferences and store them on bgColor and barColor
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundColor = prefs.getString("BgColor", "");
        if (!backgroundColor.equalsIgnoreCase("")) {
            bgColor = backgroundColor;
        }
        String progressBarColor = prefs.getString("BarColor", "");
        if (!progressBarColor.equalsIgnoreCase("")) {
            barColor = progressBarColor;
        }

        //Call and start the sound meter class
        sensor = new soundMeter();
        sensor.start();

        //Define the main menu background
        mainMenu  = (RelativeLayout) findViewById(R.id.backgroundMain);

        //Define the progressBar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Call a timer to measure decibel every X milliseconds (period) and update the app
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //Check if paused
                        if (measuring) {
                            //Get dB
                            double db = sensor.getDecibel();
                            //If it's the highest measured, save it and replace the older one and update the high text
                            if (db>highest) {
                                highest=db;
                                TextView highText = (TextView) findViewById(R.id.highText);
                                String highestText = String.format("%.2f dB", highest);
                                highText.setText(highestText);
                            }
                            //Calculate average and update the average text
                            //Formula: avg = (avg*(X-1))/X + dB/X, where X is a total count of measured decibels
                            timerCount++;
                            average = (average*(timerCount-1))/timerCount + db/timerCount;
                            TextView averageText = (TextView) findViewById(R.id.averageText);
                            String avgText = String.format("%.2f dB", average);
                            averageText.setText(avgText);

                            //Replace current decibel text
                            TextView currentText = (TextView) findViewById(R.id.currentText);
                            String currText = String.format("%.2f dB", db);
                            currentText.setText(currText);

                            //Update the visual decibel bar
                            progressBar.setProgress((int) (db/140*100));
                        }
                    }
                });
            }
        }, delay, period);

        //Do visual functions after progress bar is constructed
        final ProgressBar tv = (ProgressBar)findViewById(R.id.progressBar);
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                //Draw background, progress bar scales and progress bar
                drawBackground();
                drawScaleOnBar();
                drawProgressBar();

                //Remove the listener after it's loaded for the first time
                ViewTreeObserver obs = tv.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });
    }

    //Draw progress bar
    public static void drawProgressBar() {

        //Gradient, from Bottom to Top, start color White and end color the new bar color
        //If you want to add a center color between end and start, just add a new Color between the start and end
        //i.e. new int[] {startColor, centerColor, endColor}
        GradientDrawable progressGradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, new int[] {
                0xffffffff,Color.parseColor(barColor)});
        //Add the gradient to a clip and set it from bottom to top
        ClipDrawable progressClipDrawable = new ClipDrawable(
                progressGradientDrawable, Gravity.BOTTOM, ClipDrawable.VERTICAL);
        //Define background color of the bar to white, edit the colorDrawable if you wanted to edit the background
        Drawable[] progressDrawables = {
                new ColorDrawable(Color.WHITE),
                progressClipDrawable, progressClipDrawable};
        LayerDrawable progressLayerDrawable = new LayerDrawable(progressDrawables);
        progressLayerDrawable.setId(0, android.R.id.background);
        progressLayerDrawable.setId(1, android.R.id.secondaryProgress);
        progressLayerDrawable.setId(2, android.R.id.progress);
        progressBar.setProgressDrawable(progressLayerDrawable);
    }

    //Draw background color of the app
    public static void drawBackground() {

        //Define a new shape drawable of a rect shape
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        //Define the height to the height of the main menu background
        int h = mainMenu.getHeight();
        //Add a gradient shader
        mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor(bgColor), Color.WHITE, Shader.TileMode.REPEAT));
        //Check the android OS and call a set background function based on the OS level
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk<android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mainMenu.setBackgroundDrawable(mDrawable);
        }
        else {
            mainMenu.setBackground(mDrawable);
        }
    }

    //Draw the scales on the decibel bar
    private void drawScaleOnBar() {
        //Get height and width of the bar
        int progressBarHeight = progressBar.getHeight();
        int progressBarWidth = progressBar.getWidth();
        //Define padding between the texts
        int paddingY = progressBarHeight/7;
        //Get position of the bar
        float progressBarY = progressBar.getY();
        float progressBarX = progressBar.getX();

        //Add 8 texts
        for (int i=0;i<8;i++) {
            //Define a new text and format it
            TextView tv = new TextView(this);
            String formatString = String.format("%d dB", 140-i*20);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(16);
            tv.setText(formatString);
            tv.setIncludeFontPadding(false);
            tv.measure(0, 0);

            //Set the position
            tv.setX(progressBarX - tv.getMeasuredWidth() - progressBarWidth/2);
            tv.setY(progressBarY - tv.getMeasuredHeight() + paddingY * i - Math.round(tv.getTextSize()/2));

            //Add it on the main menu
            mainMenu.addView(tv);
        }
    }
    //Open settings and start the settings activity
    public void openSettings(View v) {
        startActivity(new Intent(getApplicationContext(), settings.class));
    }

    //Switch-like pause button
    public void buttonPause(View w) {
        //Stop or start the sensor based on the call
        if (measuring) {
            ((Button) w).setText("RESUME");
            measuring=false;
            sensor.stop();
        }
        else {
            ((Button) w).setText("PAUSE");
            measuring=true;
            sensor.start();
        }
    }

    //Create the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(getApplicationContext(), settings.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
