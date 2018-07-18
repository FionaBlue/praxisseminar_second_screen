package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InformationActivity extends AppCompatActivity {

    // **************************************************************************
    // here insert information content for specific video


    private Button buttonInformationNext;      // button for switching to next activity (temporary!)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        // registering button listener
        addListenerOnButton();
    }

    public void addListenerOnButton() {
        final Context context = this;
        // registering button and button-behaviour by on-clicking
        buttonInformationNext = (Button) findViewById(R.id.buttonInformationNext);
        buttonInformationNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, QuizActivity.class);
                startActivity(intent);
            }
        });
    }
    // **************************************************************************
}
