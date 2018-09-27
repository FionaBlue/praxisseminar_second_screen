package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VideoBibActivity extends AppCompatActivity {

    // src:
    // ********
    //http://android-coding.blogspot.com/2013/04/display-youtubethumbnailview-of-youtube.html
    //https://stackoverflow.com/questions/37253796/youtube-playlist-to-listview-in-android-studio#
    //https://stackoverflow.com/questions/34371461/how-to-load-youtube-thumbnails-in-a-recyclerview-using-youtube-api
    //https://stackoverflow.com/questions/32409964/get-color-resource-as-string/32410035
    //https://stackoverflow.com/questions/18708955/invisible-components-still-take-up-space
    //https://stackoverflow.com/questions/5237101/is-it-possible-to-get-element-from-hashmap-by-its-position/5237147
    //http://www.tutorialspoint.com/java/java_linkedhashmap_class.htm
    //https://gist.github.com/tejainece/d32cba84b747c0b2e7df
    //https://stackoverflow.com/questions/10387290/how-to-get-position-of-key-value-in-linkedhashmap-using-its-key
    //https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
    //https://stackoverflow.com/questions/35606368/java-lang-illegalstateexception-not-connected-call-connect-youtube-api

    private ImageView chevron_right;
    private ImageView chevron_left;

    private TextView continentTitle;
    private LinkedHashMap continents;

    private String currentContinent;

    private ProgressBar progressBar;

    ArrayList<VideoDataModel> arrayOfVideos = new ArrayList<>();
    private SignalRClient srClient = null;

    private MenuItem activeCast;
    private MenuItem inactiveCast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videobib);
        progressBar = findViewById(R.id.load_videos);

        Bundle extras = getIntent().getExtras();
        currentContinent = extras.getString("currentContinent");

        //getting YouTube playlists from WildLive Channel in GetPlaylists AsyncTask
        VideoRequestHandler.GetPlaylists asyncTask = (VideoRequestHandler.GetPlaylists) new VideoRequestHandler.GetPlaylists(new VideoRequestHandler.GetPlaylists.AsyncResponse(){
            @Override
            public void processFinish(LinkedHashMap playlists){ //receiving the result fired from async task
                continentTitle = (TextView)findViewById(R.id.continent);
                //updateContinentTitle(playlists, 0);
                updateContinentTitle(playlists, currentContinent);
                continents = playlists;
                //initializing listeners but now for bug prevention
                addListenerOnChevrons();
            }
        }).execute();

        WildLive app = (WildLive) getApplication();
        srClient = app.getSRClient();
        if(srClient != null) {
            srClient.setMessageListener(new SignalRClient.SignalRCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    if(message.toString().contains("firstScreenConnected")){
                        srClient.isConnectedToFS = true;
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.videobib, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        activeCast = menu.findItem(R.id.action_cast_connected_videobib);
        inactiveCast = menu.findItem(R.id.action_cast_videobib);
        if(srClient.isConnectedToFS == true){
            activeCast.setVisible(true);
            inactiveCast.setVisible(false);
        } else {
            activeCast.setVisible(false);
            inactiveCast.setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    private void resetVideoView(){
        arrayOfVideos.clear();
        ArrayList emptyArray = new ArrayList();
        setVideoList(emptyArray);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void updateContinentTitle(LinkedHashMap map, String continent) { //Integer index) {
        //continentTitle.setText((String) getContinentByIndex(map, index));
        //String continentName = (String) continentTitle.getText();
        continentTitle.setText(continent);
        //setContientColor(continentName);
        setContientColor(continent);
        //String continentID = (String) map.get(continentName);
        String continentID = (String) map.get(continent);
        arrayOfVideos.clear();
        getContinentVideos(continentID);
    }

    private void setContientColor(String continent) {
        System.out.println("Continent " + continent);
        CardView continentCard = (CardView) continentTitle.getParent();
        int continentColor;
        switch(continent) {
            case "Arktis":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorArktis)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;
            case "Antarktis":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAntarktis)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;
            case "Afrika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAfrika)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;
            case "Australien":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAustralien)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;
            case "Südamerika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorSüdamerika)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;
            case "Nordamerika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorNordamerika)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;
            case "Europa":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorEuropa)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;
            case "Asien":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorAsien)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg(continent);
                }
                break;

            default:
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorMainBlue)));
                continentCard.setCardBackgroundColor(continentColor);
                if(srClient != null){
                    srClient.sendMsg("Default");
                }
                break;
        }
    }

    //gets PlayListItems and the video durations from Videos in two extra AsyncTasks from YouTube
    private void getContinentVideos(String continent) {
        VideoRequestHandler.GetVideos videoAsyncTask = (VideoRequestHandler.GetVideos) new VideoRequestHandler.GetVideos(new VideoRequestHandler.GetVideos.AsyncResponse(){
            @Override
            public void processFinish(final ArrayList videoList){
                for(int i = 0; i < videoList.size(); i++) {
                    final VideoRequestHandler.GetVideos.VideoInformationModel currentVideo = (VideoRequestHandler.GetVideos.VideoInformationModel) videoList.get(i);
                    VideoRequestHandler.GetVideoLength videoLenghtAsyncTask = (VideoRequestHandler.GetVideoLength) new VideoRequestHandler.GetVideoLength(new VideoRequestHandler.GetVideoLength.AsyncResponse(){
                        @Override
                        public void processFinish(String videoLength){
                            currentVideo.videoLength = parseDuration(videoLength);
                            resetVideoView();
                            setVideoList(videoList);
                        }
                    }).execute(currentVideo.videoID);
                }
                //sets chevrons clickable but now for bug prevention
                chevron_right.setClickable(true);
                chevron_left.setClickable(true);
            }
        }).execute(continent);
    }

    //parses the duration ISO8601 format from YouTube
    private String parseDuration(String vLength){
        String parsedLength = "";
        for(int i = 2; i<vLength.length()-1;i++){
            if(Character.isDigit(vLength.charAt(i))){
                parsedLength += vLength.charAt(i);
            } else {
                parsedLength += ":";
            }
        }
        //supplements missing zeros from ISO8601 format
        if(parsedLength.length()%2 == 0){
            if(parsedLength.length() == 4 && parsedLength.indexOf(":") == 2){
                parsedLength = parsedLength.substring(0, parsedLength.length()-1) + "0" + parsedLength.substring(parsedLength.length()-1, parsedLength.length());
            } else if (parsedLength.length() == 6){
                parsedLength = parsedLength.substring(0, parsedLength.length()-4) + "0" + parsedLength.substring(parsedLength.length()-4, parsedLength.length());
            } else if (parsedLength.length() == 2){
                if(vLength.contains("M")) {
                    parsedLength = parsedLength + ":00";
                } else {
                    parsedLength = "00:" + parsedLength;
                }
            } else if (parsedLength.length() == 1){
                parsedLength = "0:0" + parsedLength;
            }
        }
        return parsedLength;
    }

    //hands the data from the AsyncTasks over to the VideoListAdapter
    //ends the loading circle
    private void setVideoList(ArrayList videoInformation){
        for (int i=0; i<videoInformation.size(); i++){
            VideoRequestHandler.GetVideos.VideoInformationModel currentInfoModel = (VideoRequestHandler.GetVideos.VideoInformationModel) videoInformation.get(i);
            VideoDataModel newVideoData = new VideoDataModel(currentInfoModel.videoTitle, currentInfoModel.videoID, currentInfoModel.videoLength, currentInfoModel.videoDescription);
            arrayOfVideos.add(newVideoData);
        }
        VideoListAdapter videoListAdapter = new VideoListAdapter(this, arrayOfVideos);
        ListView videoListView = (ListView) findViewById(R.id.videoList);
        videoListView.setAdapter(videoListAdapter);
        progressBar.setVisibility(View.GONE);
    }

    private String getContinentByIndex(LinkedHashMap map, int index){
        Set entrySet = map.entrySet();
        Map.Entry theEntry = (Map.Entry) entrySet.toArray()[index];
        return (String) theEntry.getKey();
    }

    //implements logic for 'Rondell' functionality
    private void addListenerOnChevrons() {
        //adds listener to left chevron
        chevron_left = (ImageView)findViewById(R.id.category_chevron_left);
        chevron_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chevron_left.setClickable(false);
                chevron_right.setClickable(false);

                Integer continentIndex = (Integer) getContinentIndex(continents, (String) continentTitle.getText());
                if (continentIndex - 1 < 0) {
                    String lastContinent = getContinentByIndex(continents, continents.size() -1);
                    continentTitle.setText(lastContinent);
                    setContientColor(lastContinent);
                    resetVideoView();
                    getContinentVideos((String) continents.get(lastContinent));
                } else {
                    String lastContinent = getContinentByIndex(continents, continentIndex -1);
                    continentTitle.setText(lastContinent);
                    setContientColor(lastContinent);
                    resetVideoView();
                    getContinentVideos((String) continents.get(lastContinent));
                }
            }
        });

        //adds listener to right chevron
        chevron_right = (ImageView)findViewById(R.id.category_chevron_right);
        chevron_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chevron_left.setClickable(false);
                chevron_right.setClickable(false);

                Integer continentIndex = (Integer) getContinentIndex(continents, (String) continentTitle.getText());
                if (continentIndex + 1 > continents.size()-1) {
                    String nextContinent = getContinentByIndex(continents, 0);
                    continentTitle.setText(nextContinent);
                    setContientColor(nextContinent);
                    resetVideoView();
                    getContinentVideos((String) continents.get(nextContinent));
                } else {
                    String nextContinent = getContinentByIndex(continents, continentIndex+1);
                    continentTitle.setText(nextContinent);
                    setContientColor(nextContinent);
                    resetVideoView();
                    getContinentVideos((String) continents.get(nextContinent));
                }
            }
        });
    }

    private Integer getContinentIndex(LinkedHashMap map, String continent) {
        Integer index;
        index = new ArrayList<String>(map.keySet()).indexOf(continent);
        return index;
    }

    // **************************************************************************

    public class VideoListAdapter extends ArrayAdapter<VideoDataModel> {

        private String PRE_REQUEST = "https://i.ytimg.com/vi/";
        private String POST_REQUEST = "/mqdefault.jpg";

        private class VideoViewHolder {
            TextView videoTitle;
            TextView videoLength;
            TextView videoDescription;
            YouTubeThumbnailView videoThumbnail;
            ProgressBar videoLoad;
            CardView videoCard;
        }

        VideoListAdapter(Context context, ArrayList<VideoDataModel> videoDataModels) {
            super(context, R.layout.videolist_item, videoDataModels);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent){
            VideoDataModel videoDataModel = getItem(position);
            final VideoViewHolder videoViewHolder;
            if (convertView == null) {
                videoViewHolder = new VideoViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.videolist_item, parent, false);

                videoViewHolder.videoTitle = (TextView) convertView.findViewById(R.id.titleView);
                videoViewHolder.videoLength = (TextView) convertView.findViewById(R.id.lengthView);
                videoViewHolder.videoDescription = (TextView) convertView.findViewById(R.id.descriptionView);
                videoViewHolder.videoThumbnail = (YouTubeThumbnailView) convertView.findViewById(R.id.thumbnailView);
                videoViewHolder.videoLoad = (ProgressBar) convertView.findViewById(R.id.thumbnailLoad);
                videoViewHolder.videoCard = (CardView) convertView.findViewById(R.id.videoItemCard);
                convertView.setTag(videoViewHolder);
            } else {
                videoViewHolder = (VideoViewHolder) convertView.getTag();
            }

            //set video-information texts
            videoViewHolder.videoTitle.setText(videoDataModel.videoTitle);
            videoViewHolder.videoLength.setText(videoDataModel.videoLength);
            if(videoDataModel.videoDescription.equals("")){
                videoViewHolder.videoDescription.setText("Keine Beschreibung verfügbar");
            } else {
                videoViewHolder.videoDescription.setText(videoDataModel.videoDescription);
            }

            //add listener to video-item-card for switching to information activity
            videoViewHolder.videoCard.setTag(videoDataModel.videoID);
            addListenerOnCards(videoViewHolder.videoCard, videoViewHolder.videoLength.getText().toString());

            //getting thumbnail-images via asynctask
            String urlRequest = PRE_REQUEST + videoDataModel.videoID + POST_REQUEST;
            VideoRequestHandler.GetImage imageAsyncTask = (VideoRequestHandler.GetImage) new VideoRequestHandler.GetImage(new VideoRequestHandler.GetImage.AsyncResponse(){
                @Override
                public void processFinish(Drawable output) {
                    videoViewHolder.videoThumbnail.setImageDrawable(output);
                    videoViewHolder.videoLoad.setVisibility(View.GONE);
                    videoViewHolder.videoThumbnail.setVisibility(View.VISIBLE);
                }
            }).execute(urlRequest);

            return convertView;
        }

        private void addListenerOnCards(final CardView videoCard, final String videoLength) {
            final Context context = getApplicationContext();
            // registering button and button-behaviour by on-click
            videoCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    if(srClient != null){
                        srClient.sendMsg("startLoader");
                    }

                    // switching to next activity on button click
                    Intent intent = new Intent(context, InformationActivity.class);
                    String videoID = new String(videoCard.getTag().toString());
                    intent.putExtra("videoID", videoID);
                    intent.putExtra("videoLength", videoLength);
                    startActivity(intent);
                }
            });
        }
    }

    // **************************************************************************

    //VideoDataModel for VideoViewHolder
    public class VideoDataModel {
        String videoTitle;
        String videoID;
        String videoLength;
        String videoDescription;

        VideoDataModel(String videoTitle, String videoID, String videoLength, String videoDescription) {
            this.videoTitle = videoTitle;
            this.videoID = videoID;
            this.videoLength = videoLength;
            this.videoDescription = videoDescription;
        }
    }
}
