package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class VideoBibActivity extends AppCompatActivity {

    // **************************************************************************
    // here insert video-player-bib-content for selecting videos

    private Button buttonVideoBibNext;      // button for switching to next activity (temporary!)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videobib);
        // registering button listener
        addListenerOnButton();
    }

    public void addListenerOnButton() {
        final Context context = this;
        // registering button and button-behaviour by on-clicking
        buttonVideoBibNext = (Button) findViewById(R.id.buttonVideoBibNext);
        buttonVideoBibNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, InformationActivity.class);
                startActivity(intent);
            }
        });
    }
    // **************************************************************************
}
