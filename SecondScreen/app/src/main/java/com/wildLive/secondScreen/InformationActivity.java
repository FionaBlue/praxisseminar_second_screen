package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class InformationActivity extends AppCompatActivity {

    private String searchTerm = "Kaiserpinguin";
    private TextView wikiContentTitle;          // text view for showing wiki-requested title content
    private TextView wikiContentExtract;        // text view for showing wiki-requested extract content
    private ImageView wikiImage;                // image view for showing wiki-requested image content
    private Button buttonInformationNext;       // button for switching to next activity (temporary!)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) for information activity
        super.onCreate(savedInstanceState);
        // registering layout and layout-components
        setContentView(R.layout.activity_information);
        wikiImage = (ImageView) findViewById(R.id.wikiImage);
        wikiContentTitle = (TextView) findViewById(R.id.currentWikiTitle);
        wikiContentExtract = (TextView) findViewById(R.id.currentWikiExtract);
        buttonInformationNext = (Button) findViewById(R.id.buttonInformationNext);

        // registering button listener
        addListenerOnButton();
        // integrating requested wiki-information for user view
        integrateInformation();
    }

    public void integrateInformation() {
        // calling url-request and getting wiki content via async task
        new WikiRequestHandler(new WikiRequestHandler.AsyncResponse() {
            @Override
            public void processFinished(Object output) {
                WikiRequestHandler.WikiContentElements contentOutput = (WikiRequestHandler.WikiContentElements) output;
                // loading responded information in text view
                wikiContentTitle.setText(contentOutput.wikiContentTitle);
                wikiContentExtract.setText(contentOutput.wikiContentExtract);

                // loading responded image in image view (temporary: local image)
                wikiImage.setImageResource(R.drawable.wiki_kaiserpinguin);
            }
        }).execute(searchTerm);
    }

    public void addListenerOnButton() {
        final Context context = this;
        // registering button and button-behaviour by on-clicking
        buttonInformationNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, QuizActivity.class);
                startActivity(intent);
            }
        });
    }
}
