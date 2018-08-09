package com.wildLive.secondScreen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    // **************************************************************************
    // here insert code for guide-screen for connecting devices (start screen!)

    private Button buttonMainNext;      // button for switching to next activity (temporary!)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // registering button listener
        addListenerOnButton();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void addListenerOnButton() {
        final Context context = this;
        // registering button and button-behaviour by on-clicking
        buttonMainNext = (Button) findViewById(R.id.buttonMainNext);
        buttonMainNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, VideoBibActivity.class);
                startActivity(intent);
            }
        });
    }
    // **************************************************************************
}