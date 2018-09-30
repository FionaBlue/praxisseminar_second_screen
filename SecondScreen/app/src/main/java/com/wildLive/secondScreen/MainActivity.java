package com.wildLive.secondScreen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private SignalRClient SRClient = null;                  // instance of signalR client behaviour
    // cast buttons
    private MenuItem activeCast;
    private MenuItem inactiveCast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start checking internet connectivity
        new InternetConnectionHandler.checkInternetConnectionState().execute();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // registering menu (cast) items
        inactiveCast = menu.findItem(R.id.action_cast_main);
        activeCast = menu.findItem(R.id.action_cast_connected_main);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.dropdown_firstscreen) {
            // instantiate new SignalR Client
            SRClient = new SignalRClient();

            // switching to next activity
            if (SRClient != null) {
                Intent intent = new Intent(getApplicationContext(), VideoOverviewActivity.class);
                startActivity(intent);

                // set SignalR Client Instance available for all Activities via Application
                WildLive app = (WildLive) getApplication();
                app.setSRClient(SRClient);
            }
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}