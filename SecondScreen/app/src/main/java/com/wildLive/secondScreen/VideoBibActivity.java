package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class VideoBibActivity extends AppCompatActivity implements YouTubeThumbnailView.OnInitializedListener {

    // **************************************************************************
    // here insert video-player-bib-content for selecting videos

    // src:
    // ********
    //http://android-coding.blogspot.com/2013/04/display-youtubethumbnailview-of-youtube.html
    //https://stackoverflow.com/questions/37253796/youtube-playlist-to-listview-in-android-studio#
    //https://stackoverflow.com/questions/34371461/how-to-load-youtube-thumbnails-in-a-recyclerview-using-youtube-api

    public static final String DEVELOPER_KEY = "AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI";
    private static final String PLAYLIST_ID = "PLrEmzVduftH2M2tt4GVOTz8DoVkTZwFqg";

    private YouTubeThumbnailView thumbnailView1;
    private YouTubeThumbnailView thumbnailView2;
    private YouTubeThumbnailLoader youTubeThumbnailLoader;

    private ImageView chevron_right;
    private ImageView chevron_left;

    private TextView continentTitle;
    private LinkedHashMap continents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videobib);
        thumbnailView1 = (YouTubeThumbnailView)findViewById(R.id.thumbnailview1);
        thumbnailView1.initialize(DEVELOPER_KEY, this);
        thumbnailView1.setTag(R.id.videoId, "Nbrx5tFJzyQ");

        thumbnailView2 = (YouTubeThumbnailView)findViewById(R.id.thumbnailview2);
        thumbnailView2.initialize(DEVELOPER_KEY, this);
        thumbnailView2.setTag(R.id.videoId, "b5Kk_qe8f4g");
        // registering button listener
        addListenerOnThumbnails(thumbnailView1);
        addListenerOnThumbnails(thumbnailView2);

        GetPlaylists asyncTask = (GetPlaylists) new GetPlaylists(new GetPlaylists.AsyncResponse(){

            @Override
            public void processFinish(LinkedHashMap output){
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                System.out.println("Main " + output);
                continentTitle = (TextView)findViewById(R.id.continent);
                updateContinentTitle(output, 0);
                continents = output;
            }
        }).execute();


        //chevron handling
        addListenerOnChevrons();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.videobib, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void updateContinentTitle(LinkedHashMap map, Integer index) {
        continentTitle.setText((String) getContinentByIndex(map, index));
        //get continent-Videos here
    }

    //https://stackoverflow.com/questions/5237101/is-it-possible-to-get-element-from-hashmap-by-its-position/5237147
    //http://www.tutorialspoint.com/java/java_linkedhashmap_class.htm
    //https://gist.github.com/tejainece/d32cba84b747c0b2e7df
    private String getContinentByIndex(LinkedHashMap map, int index){
        Set entrySet = map.entrySet();
        Map.Entry theEntry = (Map.Entry) entrySet.toArray()[index];
        System.out.println("entry" + theEntry);
        return (String) theEntry.getKey();
    }

    private void addListenerOnChevrons() {
        chevron_left = (ImageView)findViewById(R.id.category_chevron_left);
        chevron_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //last continent
                Toast.makeText(getApplicationContext(),
                        "LAST", Toast.LENGTH_SHORT).show();

                Integer continentIndex = (Integer) getContinentIndex(continents, (String) continentTitle.getText());

                System.out.println("ci" + continentIndex + " " + "cs" + continents.size());
                if (continentIndex - 1 < 0) {
                    String lastContinent = getContinentByIndex(continents, continents.size() -1);
                    continentTitle.setText(lastContinent);
                } else {
                    String lastContinent = getContinentByIndex(continents, continentIndex -1);
                    continentTitle.setText(lastContinent);
                }
            }
        });

        chevron_right = (ImageView)findViewById(R.id.category_chevron_right);
        chevron_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //next continent
                Toast.makeText(getApplicationContext(),
                        "NEXT", Toast.LENGTH_SHORT).show();

                Integer continentIndex = (Integer) getContinentIndex(continents, (String) continentTitle.getText());
                if (continentIndex + 1 > continents.size()-1) {
                    String nextContinent = getContinentByIndex(continents, 0);
                    continentTitle.setText(nextContinent);
                } else {
                    String nextContinent = getContinentByIndex(continents, continentIndex+1);
                    continentTitle.setText(nextContinent);
                }
            }
        });
    }

    //https://stackoverflow.com/questions/10387290/how-to-get-position-of-key-value-in-linkedhashmap-using-its-key
    private Integer getContinentIndex(LinkedHashMap map, String continent) {
        Integer index;
        index = new ArrayList<String>(map.keySet()).indexOf(continent);
        return index;
    }

    @Override
    public void onInitializationFailure(YouTubeThumbnailView thumbnailView,
                                        YouTubeInitializationResult errorReason) {

        String errorMessage =
                String.format("onInitializationFailure (%1$s)",
                        errorReason.toString());
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView thumbnailView,
                                        YouTubeThumbnailLoader thumbnailLoader) {

        Toast.makeText(getApplicationContext(),
                "onInitializationSuccess", Toast.LENGTH_SHORT).show();

        youTubeThumbnailLoader = thumbnailLoader;
        thumbnailLoader.setOnThumbnailLoadedListener(new ThumbnailListener());
        youTubeThumbnailLoader.setVideo((String) thumbnailView.getTag(R.id.videoId));
    }

    private final class ThumbnailListener implements
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView thumbnail, String videoId) {
            Toast.makeText(getApplicationContext(),
                    "onThumbnailLoaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView thumbnail,
                                     YouTubeThumbnailLoader.ErrorReason reason) {
            Toast.makeText(getApplicationContext(),
                    "onThumbnailError", Toast.LENGTH_SHORT).show();
        }
    }

    public void addListenerOnThumbnails(YouTubeThumbnailView thumbnailView) {
        final Context context = this;
        // registering button and button-behaviour by on-clicking
        thumbnailView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, InformationActivity.class);
                startActivity(intent);
            }
        });
    }
    // **************************************************************************

    private static class GetPlaylists extends AsyncTask<String, Void, LinkedHashMap> {

        public AsyncResponse delegate = null;
        private Exception exception;
        private String PLAYLISTS_REQUEST = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=UC2ETsCbgegY8iqQFwS9Xi4w&key=AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI&maxResults=50";

        public GetPlaylists(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public interface AsyncResponse {
            void processFinish(LinkedHashMap output);
        }

        protected LinkedHashMap doInBackground(String... string) {

            LinkedHashMap<String, String> playlistRondell = new LinkedHashMap<String, String>();

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(PLAYLISTS_REQUEST);
            org.apache.http.HttpResponse response = null;

            try {
                response = httpclient.execute(httpget);
            } catch (Exception e) {
                this.exception = e;
            }
            String responseAsString;
            JSONObject responseAsJSONObject = null;
            JSONArray playlistsArray;

            try {
                responseAsString = EntityUtils.toString(response.getEntity());
                try {
                    responseAsJSONObject = new JSONObject(responseAsString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println(responseAsJSONObject);
            try {
                playlistsArray = responseAsJSONObject.getJSONArray("items");

                for (int i=0; i<playlistsArray.length(); i++){
                    JSONObject playlist = playlistsArray.getJSONObject(i);
                    //System.out.println(playlist);
                    JSONObject snippet = playlist.getJSONObject("snippet");
                    playlistRondell.put(snippet.getString("title"), playlist.getString("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //System.out.println(playlistRondell);
            return playlistRondell;
        }

        protected void onPostExecute(LinkedHashMap result) {
            delegate.processFinish(result);
        }
    }
}
