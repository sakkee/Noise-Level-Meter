package com.example.sakkee.noiselevelmeter;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.graphics.Color;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.LinearGradient;
import android.graphics.Shader;

public class settings extends AppCompatActivity {
    //Previous background and decibel bar colors
    private String prevBgColor;
    private String prevBarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add the settings menu on the screen
        setContentView(R.layout.activity_settings);

        //Populate the lists
        populateListView();

        //Add click listeners on the lists
        registerClickCallback();

        //Save the previous background and decibel bar colors
        prevBarColor = MainActivity.barColor;
        prevBgColor = MainActivity.bgColor;

        //Do the drawBackground function after the lists have been constructed
        final ListView tv = (ListView) findViewById(R.id.listBarColors);
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                drawBackground(prevBgColor);
                ViewTreeObserver obs = tv.getViewTreeObserver();
                //Delete the listener
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

    }

    //Set items on the lists
    private void populateListView() {
        String[] choices = {"Red", "Green", "Black", "Yellow", "Orange", "White"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.choice, choices);
        ListView listBarColors = (ListView) findViewById(R.id.listBarColors);
        listBarColors.setAdapter(adapter);
        ListView listBgColors = (ListView) findViewById(R.id.listBgColors);
        listBgColors.setAdapter(adapter);


    }

    //Draw background on the settings menu
    private void drawBackground(String color) {
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        RelativeLayout settingsMenu = (RelativeLayout) findViewById(R.id.settingsBg);
        int h = settingsMenu.getHeight();
        mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor(color), Color.WHITE, Shader.TileMode.REPEAT));
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk<android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settingsMenu.setBackgroundDrawable(mDrawable);
        }
        else {
            settingsMenu.setBackground(mDrawable);
        }

        //Draw the main menu background
        MainActivity.bgColor = color;
        MainActivity.drawBackground();
    }

    //Click listener on the lists
    private void registerClickCallback() {
        ListView barList = (ListView) findViewById(R.id.listBarColors);
        barList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                //Check the item which was pressed and save the choice on chosenColor
                TextView textView = (TextView) viewClicked;
                String chosen = textView.getText().toString();
                String chosenColor = "#FF0000";
                if (chosen.equals("Red")) {
                    chosenColor = "#FF0000";
                } else if (chosen.equals("Green")) {
                    chosenColor = "#00FF00";
                } else if (chosen.equals("Black")) {
                    chosenColor = "#000000";
                } else if (chosen.equals("Yellow")) {
                    chosenColor = "#FFFF00";
                } else if (chosen.equals("Orange")) {
                    chosenColor = "#F5A700";
                } else if (chosen.equals("White")) {
                    chosenColor = "#DDDDDD";
                }
                //Replace the main menu's bar color with the chosenColor and draw the decibel bar again
                MainActivity.barColor = chosenColor;
                MainActivity.drawProgressBar();
            }
        });

        //Background color click registering
        ListView bgList = (ListView) findViewById(R.id.listBgColors);
        bgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                String chosenColor = "#FFBFA5";
                String chosen = textView.getText().toString();
                if (chosen.equals("Red")) {
                    chosenColor = "#FFAAAA";
                } else if (chosen.equals("Green")) {
                    chosenColor = "#AAFFAA";
                } else if (chosen.equals("Black")) {
                    chosenColor = "#999999";
                } else if (chosen.equals("Yellow")) {
                    chosenColor = "#FFFFAA";
                } else if (chosen.equals("Orange")) {
                    chosenColor = "#FFBFA5";
                } else if (chosen.equals("White")) {
                    chosenColor = "#DDDDDD";
                }
                //Draw the menu background of main and settings
                drawBackground(chosenColor);
            }
        });
    }
    //Save the settings menu
    public void saveSettings(View v) {
        //Save the settings on preferences
        SharedPreferences.Editor editor = MainActivity.prefs.edit();
        editor.putString("BarColor",MainActivity.barColor);
        editor.putString("BgColor",MainActivity.bgColor);
        editor.apply();
        this.finish();
    }
    //Undo the changes and close the menu
    public void closeSettings(View v) {
        MainActivity.bgColor = prevBgColor;
        MainActivity.drawBackground();
        MainActivity.barColor = prevBarColor;
        MainActivity.drawProgressBar();
        this.finish();

    }
}
