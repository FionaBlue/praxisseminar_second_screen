package com.wildLive.secondScreen;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {//implements DialogHandler.OnInputListener {

    // src:
    // ************
    // https://developer.android.com/guide/topics/ui/dialogs

    // interfaces from dialogHandler
    /*@Override
    public void sendInput(String input, int buttonId) {
        switchCastButton(buttonId);
        // registering signalR client
        SRClient = new SignalRClient();
    }
    @Override
    public void sendInput(int buttonId) {
        switchCastButton(buttonId);
    }

    // switching between cast buttons in options menu
    private void switchCastButton(int buttonId) {
        switch (buttonId) {
            case 0:
                break;
            case 1:
                if (activeCast.isVisible()) {
                    activeCast.setVisible(false);
                    inactiveCast.setVisible(true);


                    //waits about 5 seconds until the message is sent so
                    //SignalR is definitley up when the message score is transferred
                    //has to be adjusted if the connection gets handled automatically
                    //and not via ID entering
                    SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
                    final int score = sp.getInt("score", 0);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(SRClient != null){
                                SRClient.sendMsg("score"+String.valueOf(score));
                                SRClient.sendMsg("endguide");
                            }
                        }
                    }, 5000);


                } else {
                    inactiveCast.setVisible(false);
                    activeCast.setVisible(true);

                }
                break;
            default:
                break;
        }
    }*/

    public  final String TAG = "MEK_Plugin_Activity";       // tags for logging
    private Button buttonMainNext;                          // button for switching to next activity (temporary!)
    private SignalRClient SRClient = null;                  // instance of signalR client behaviour
    private MenuItem activeCast;
    private MenuItem inactiveCast;
    private DialogHandler dialog = new DialogHandler();     // for handling dialogs (options menu)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start checking internet connectivity
        new InternetConnectionHandler.getInternetConnectionState().execute();

        // registering button listener
        //addListenerOnButtons();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // registering menu (cast) items for further on-click handling (e.g. visibility)
        inactiveCast = menu.findItem(R.id.action_cast_main);
        activeCast = menu.findItem(R.id.action_cast_connected_main);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (item.getItemId() == R.id.action_cast_main) {
            // calling popup dialog (for deactivating connection)
            callCastActivation();
            return true;
        } else if (item.getItemId() == R.id.action_cast_connected_main) {
            // calling popup dialog (for activating connection)
            callCastDeactivation();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }*/
        int itemId = item.getItemId();
        if (itemId == R.id.dropdown_firstscreen) {
            SRClient = new SignalRClient();
            //waits about 5 seconds until the message is sent so
            //SignalR is definitley up when the message score is transferred
            //has to be adjusted if the connection gets handled automatically
            //and not via ID entering
            SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
            final int score = sp.getInt("score", 0);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (SRClient != null) {
                        SRClient.sendMsg("score" + String.valueOf(score));
                        SRClient.sendMsg("endguide");
                    }
                }
            }, 5000);

            // switching to next activity
            if (SRClient != null) {
                Intent intent = new Intent(getApplicationContext(), VideoOverviewActivity.class);
                startActivity(intent);
                WildLive app = (WildLive) getApplication();
                app.setSRClient(SRClient);
            }
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void callCastActivation() {
        // setting new component values for dialog
        dialog.setDialogTitle("Geräteverbindung");
        dialog.setDialogDescription("Geben Sie hier die ID zur Verbindung mit dem First Screen ein:", true);
        dialog.setDialogActionButtons("VERBINDEN", true, "ABBRECHEN", true);

        // showing dialog
        dialog.show(getSupportFragmentManager(), "connectionDialogHandler");
    }

    private void callCastDeactivation() {
        // setting new component values for dialog
        dialog.setDialogTitle("Geräteverbindung");
        dialog.setDialogDescription("Möchten Sie die Geräteverbindung trennen?", false);
        dialog.setDialogActionButtons("JA", true, "NEIN", true);

        // showing dialog
        dialog.show(getSupportFragmentManager(), "disconnectionDialogHandler");
    }

    /*public void addListenerOnButtons() {
        final Context context = this;

        // registering button for switching to new activity behaviour
        buttonMainNext = (Button) findViewById(R.id.buttonMainNext);
        buttonMainNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, VideoBibActivity.class);
                startActivity(intent);
                WildLive app = (WildLive) getApplication();
                app.setSRClient(SRClient);
            }
        });
    }*/

    // creating new instance of signalR client after reconnecting-event (callback)
    public void handleReconnection() {
        SRClient = null;
        SRClient = new SignalRClient();
    }
}