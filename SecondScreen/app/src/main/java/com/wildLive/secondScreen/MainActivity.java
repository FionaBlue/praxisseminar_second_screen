package com.wildLive.secondScreen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    public  final String TAG = "MEK_Plugin_Activity";   // tags for logging
    private Button buttonMainNext;                      // button for switching to next activity (temporary!)
    private Button connectionButton;                    // button for connecting devices via signalR (temporary)!)
    private SignalRClient SRClient = null;              // instance of signalR client behaviour

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // registering signalR client
        SRClient = new SignalRClient(messageCallback);

        // registering button listener
        addListenerOnButtons();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void addListenerOnButtons() {
        final Context context = this;

        // registering button for switching to new activity behaviour
        buttonMainNext = (Button) findViewById(R.id.buttonMainNext);
        buttonMainNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, VideoBibActivity.class);
                startActivity(intent);
            }
        });

        // registering button for establishing connection between devices
        connectionButton = (Button) findViewById(R.id.connectingButton);
        connectionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // connecting via signalR-client
                SRClient.sendMsg("connection request from android phone");
            }
        });
    }

    // creating new instance of signalR client after reconnecting-event (callback)
    public void handleReconnection() {
        SRClient = null;
        SRClient = new SignalRClient(messageCallback);
    }

    // handling message requests from server-client-connection (via signalR)
    final SignalRCallback<String> messageCallback = new SignalRCallback<String>() {
        @Override
        public void onSuccess(String var1) {
            Log.d(TAG, "message success: " + var1);
        }
        @Override
        public void onError(Error var1) {
            Log.d(TAG, "message error: " + var1);
        }
    };
}