package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videooverview);

        final WildLive app = (WildLive)getApplication();
        signalRClient = app.getSRClient();

        if(signalRClient != null) {

            signalRClient.sendMsg("startLoader");

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

        progressBar = findViewById(R.id.load_continents);

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

    private void setContinentOverview(LinkedHashMap continents){
        List continentList = new ArrayList(continents.keySet());
        //System.out.println("CONTINENTS!!! " + continentList);
        for (int i=0; i<continentList.size(); i++){
            String currentContinentTitle = (String) continentList.get(i);
            int currentContinentColor = getContientColor(currentContinentTitle);
            ContinentTitleModel newContinentTitleModel = new ContinentTitleModel(currentContinentTitle, currentContinentColor);
            arrayOfContinents.add(newContinentTitleModel);
        }
        ContinentOverviewAdapter overviewAdapter = new ContinentOverviewAdapter(this, arrayOfContinents);
        ListView continentOverview = findViewById(R.id.overviewList);
        continentOverview.setAdapter(overviewAdapter);
        progressBar.setVisibility(View.GONE);
    }

    private int getContientColor(String continent) {
        int continentColor;
        switch(continent) {
            case "Arktis":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorArktis)));
                break;
            case "Antarktis":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAntarktis)));
                break;
            case "Afrika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAfrika)));
                break;
            case "Australien":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAustralien)));
                break;
            case "Südamerika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorSüdamerika)));
                break;
            case "Nordamerika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNordamerika)));
                break;
            case "Europa":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorEuropa)));
                break;
            case "Asien":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAsien)));
                break;

            default:
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorMainBlue)));
                break;
        }
        return continentColor;
    }


    public class ContinentOverviewAdapter extends ArrayAdapter<ContinentTitleModel> {

        private class ContinentViewHolder {
            TextView continentTitle;
            CardView continentCard;
        }

        ContinentOverviewAdapter(Context context, ArrayList<ContinentTitleModel> continentTitleModels) {
            super(context, R.layout.overview_item, continentTitleModels);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ContinentTitleModel continentTitleModel = getItem(position);
            final ContinentViewHolder continentViewHolder;
            if(convertView == null){
                continentViewHolder = new ContinentViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.overview_item, parent, false);

                continentViewHolder.continentTitle = convertView.findViewById(R.id.overviewItemTitle);
                continentViewHolder.continentCard = convertView.findViewById(R.id.overviewItemCard);

                convertView.setTag(continentViewHolder);
            } else {
                continentViewHolder = (ContinentViewHolder) convertView.getTag();
            }
            continentViewHolder.continentTitle.setText(continentTitleModel.continentTitle);


            continentViewHolder.continentCard.setCardBackgroundColor(continentTitleModel.continentColor);

            addListenerOnCards(continentViewHolder.continentCard, continentTitleModel.continentTitle.toString());

            return convertView;
        }

        private void addListenerOnCards(CardView card, final String continent){
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), VideoBibActivity.class);
                    intent.putExtra("currentContinent", continent);
                    startActivity(intent);
                }
            });
        }

    }

    public class ContinentTitleModel{
        String continentTitle;
        int continentColor;

        ContinentTitleModel(String continentTitle, int continentColor){
            this.continentTitle = continentTitle;
            this.continentColor = continentColor;
        }
    }


}
