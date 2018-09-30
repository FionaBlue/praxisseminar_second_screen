package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class VideoOverviewActivity extends AppCompatActivity {

    ArrayList<ContinentTitleModel> arrayOfContinents = new ArrayList<>();
    ProgressBar progressBar;

    private MenuItem activeCast;
    private MenuItem inactiveCast;

    private SignalRClient signalRClient;

    private CategoryColorHandler colorHandler = new CategoryColorHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videooverview);

        // get current instance of SignalR Client from Application
        final WildLive app = (WildLive)getApplication();
        signalRClient = app.getSRClient();

        if(signalRClient != null) {

            // start loader on First Screen for user-feedback
            signalRClient.sendMsg("startLoader");

            // set message listener for retaining first screen connection
            signalRClient.setMessageListener(new SignalRClient.SignalRCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    if(message.toString().contains("firstScreenConnected")){
                        signalRClient.isConnectedToFS = true;
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }

        // start progressBar for user-feedback
        progressBar = findViewById(R.id.load_continents);

        // get continent/category titles from AsyncTask
        VideoRequestHandler.GetPlaylists asyncTask = (VideoRequestHandler.GetPlaylists) new VideoRequestHandler.GetPlaylists(new VideoRequestHandler.GetPlaylists.AsyncResponse(){
        @Override
        public void processFinish(final LinkedHashMap output) {
                if(signalRClient != null){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setContinentOverview(output);
                            sendMessagesToFS();
                        }
                    }, 2000);
                }
            }
        }).execute();
    }

    // update First Screen with private score
    // remove First Screen PopUps (Guide and Loader)
    private void sendMessagesToFS(){
        SharedPreferences sp = getSharedPreferences("quizdata", MODE_PRIVATE);
        final int score = sp.getInt("score", 0);
        signalRClient.sendMsg("closePopUp");
        signalRClient.sendMsg("score" + String.valueOf(score));
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.videooverview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handles cast-button visibility-states
    // is called regularly in asyncTask checkCastConnection in SignalRClient
    public boolean onPrepareOptionsMenu(Menu menu) {
        activeCast = menu.findItem(R.id.action_cast_connected_overview);
        inactiveCast = menu.findItem(R.id.action_cast_overview);
        if(signalRClient.isConnectedToFS == true){
            activeCast.setVisible(true);
            inactiveCast.setVisible(false);
        } else {
            activeCast.setVisible(false);
            inactiveCast.setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onBackPressed(){
        //do nothing for setting Back Button on Device disabled
    }

    // reset map on first screen to coloured when user is going back from VideoBib- to VideoOverviewActivity
    protected void onResume() {
        if(signalRClient != null){
            signalRClient.sendMsg("Coloured");
        }
        super.onResume();
    }

    // hands the data from the AsyncTask over to the ContinentOverviewAdapter
    // ends the loading circle
    private void setContinentOverview(LinkedHashMap continents){
        List continentList = new ArrayList(continents.keySet());
        for (int i=0; i<continentList.size(); i++){
            String currentContinentTitle = (String) continentList.get(i);                                                  // get current title
            int currentContinentColor = colorHandler.getContinentColor(currentContinentTitle, getApplicationContext());    // get current color
            // set title and color in new Model
            ContinentTitleModel newContinentTitleModel = new ContinentTitleModel(currentContinentTitle, currentContinentColor);
            // add new Model to ArrayList
            arrayOfContinents.add(newContinentTitleModel);
        }
        ContinentOverviewAdapter overviewAdapter = new ContinentOverviewAdapter(this, arrayOfContinents);    // create new Adapter
        ListView continentOverview = findViewById(R.id.overviewList);                                               // set ListView from VideoOverview-xml
        continentOverview.setAdapter(overviewAdapter);                                                              // set Adapter to ListView
        progressBar.setVisibility(View.GONE);                                                                       // set loading circle gone
    }



    // **************************************************************************

    public class ContinentOverviewAdapter extends ArrayAdapter<ContinentTitleModel> {

        private class ContinentViewHolder {
            TextView continentTitle;
            CardView continentCard;
        }

        // constructor
        ContinentOverviewAdapter(Context context, ArrayList<ContinentTitleModel> continentTitleModels) {
            super(context, R.layout.overview_item, continentTitleModels);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ContinentTitleModel continentTitleModel = getItem(position);
            final ContinentViewHolder continentViewHolder;
            if(convertView == null){
                continentViewHolder = new ContinentViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.overview_item, parent, false);

                // sets xml-views to videoViewHolder
                continentViewHolder.continentTitle = convertView.findViewById(R.id.overviewItemTitle);
                continentViewHolder.continentCard = convertView.findViewById(R.id.overviewItemCard);

                convertView.setTag(continentViewHolder);
            } else {
                continentViewHolder = (ContinentViewHolder) convertView.getTag();
            }

            continentViewHolder.continentTitle.setText(continentTitleModel.continentTitle);                 // set title
            continentViewHolder.continentCard.setCardBackgroundColor(continentTitleModel.continentColor);   // set color

            addListenerOnCards(continentViewHolder.continentCard, continentTitleModel.continentTitle.toString());

            return convertView;
        }

        // add listener on cards to switch to VideoBibActivity
        private void addListenerOnCards(CardView card, final String continent){
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), VideoBibActivity.class);
                    // pass current continent/category via intent for setting correct view
                    intent.putExtra("currentContinent", continent);
                    startActivity(intent);
                }
            });
        }
    }

    // **************************************************************************

    // ContentTitleModel for ContinentOverviewAdapter
    public class ContinentTitleModel{
        String continentTitle;
        int continentColor;

        ContinentTitleModel(String continentTitle, int continentColor){
            this.continentTitle = continentTitle;
            this.continentColor = continentColor;
        }
    }
}
