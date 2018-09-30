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
    // http://android-coding.blogspot.com/2013/04/display-youtubethumbnailview-of-youtube.html
    // https://stackoverflow.com/questions/37253796/youtube-playlist-to-listview-in-android-studio#
    // https://stackoverflow.com/questions/34371461/how-to-load-youtube-thumbnails-in-a-recyclerview-using-youtube-api
    // https://stackoverflow.com/questions/32409964/get-color-resource-as-string/32410035
    // https://stackoverflow.com/questions/18708955/invisible-components-still-take-up-space
    // https://stackoverflow.com/questions/5237101/is-it-possible-to-get-element-from-hashmap-by-its-position/5237147
    // http://www.tutorialspoint.com/java/java_linkedhashmap_class.htm
    // https://gist.github.com/tejainece/d32cba84b747c0b2e7df
    // https://stackoverflow.com/questions/10387290/how-to-get-position-of-key-value-in-linkedhashmap-using-its-key
    // https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
    // https://stackoverflow.com/questions/35606368/java-lang-illegalstateexception-not-connected-call-connect-youtube-api

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

    private CategoryColorHandler colorHandler = new CategoryColorHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videobib);
        progressBar = findViewById(R.id.load_videos);

        Bundle extras = getIntent().getExtras();
        currentContinent = extras.getString("currentContinent");

        // getting YouTube playlists from WildLive Channel in GetPlaylists AsyncTask
        VideoRequestHandler.GetPlaylists asyncTask = (VideoRequestHandler.GetPlaylists) new VideoRequestHandler.GetPlaylists(new VideoRequestHandler.GetPlaylists.AsyncResponse(){
            @Override
            public void processFinish(LinkedHashMap playlists){ //receiving the result fired from async task
                continentTitle = (TextView)findViewById(R.id.continent);
                updateContinentTitle(playlists, currentContinent);
                continents = playlists;
                //initializing listeners but now for bug prevention
                addListenerOnChevrons();
            }
        }).execute();

        // get current instance of SignalR Client from Application
        WildLive app = (WildLive) getApplication();
        srClient = app.getSRClient();
        if(srClient != null) {

            // set message listener for retaining first screen connection
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

    // reset map on first screen to current continent when user is going back from Information- to VideoBibActivity
    @Override
    protected void onResume() {
        if(srClient != null){
            srClient.sendMsg(currentContinent);
        }
        super.onResume();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.videobib, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handles cast-button visibility-states
    // is called regularly in asyncTask checkCastConnection in SignalRClient
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

    // clears videos in view for loading the new videos from new category/continent
    // sets progressBar visible for loading-user-feedback
    private void resetVideoView(){
        arrayOfVideos.clear();
        ArrayList emptyArray = new ArrayList();
        setVideoList(emptyArray);
        progressBar.setVisibility(View.VISIBLE);
    }

    // updates continent/category title and calls method for getting the videos of this continent
    private void updateContinentTitle(LinkedHashMap map, String continent) {
        continentTitle.setText(continent);
        // set currentContinent for onResume
        currentContinent = continent;
        setContinentColor(continent);
        String continentID = (String) map.get(continent);
        arrayOfVideos.clear();
        getContinentVideos(continentID);
    }

    // sets the continent-matching color
    // for not defined categories there is a default color for flexibility
    private void setContinentColor(String continent) {
        CardView continentCard = (CardView) continentTitle.getParent();
        int continentColor = colorHandler.getContinentColor(continent, getApplicationContext());
        continentCard.setCardBackgroundColor(continentColor);
        if(srClient != null){
            srClient.sendMsg(continent);
        }
    }

    // gets PlayListItems (video-id, -description, -title) and video durations  from YouTube in two extra AsyncTasks
    private void getContinentVideos(String continent) {
        VideoRequestHandler.GetVideos videoAsyncTask = (VideoRequestHandler.GetVideos) new VideoRequestHandler.GetVideos(new VideoRequestHandler.GetVideos.AsyncResponse(){
            @Override
            public void processFinish(final ArrayList videoList){
                for(int i = 0; i < videoList.size(); i++) {
                    final VideoRequestHandler.GetVideos.VideoInformationModel currentVideo = (VideoRequestHandler.GetVideos.VideoInformationModel) videoList.get(i);
                    // start new videoLength-asyncTasks here
                    // needed because video-duration is only available in specific url request
                    VideoRequestHandler.GetVideoLength videoLenghtAsyncTask = (VideoRequestHandler.GetVideoLength) new VideoRequestHandler.GetVideoLength(new VideoRequestHandler.GetVideoLength.AsyncResponse(){
                        @Override
                        public void processFinish(String videoLength){
                            currentVideo.videoLength = parseDuration(videoLength);
                            resetVideoView();           // clears view
                            setVideoList(videoList);    // loads view with new duration value
                        }
                    }).execute(currentVideo.videoID);
                }
                // set chevrons clickable but now for bug prevention
                chevron_right.setClickable(true);
                chevron_left.setClickable(true);
            }
        }).execute(continent);
    }

    // parses the duration ISO8601 format from YouTube
    private String parseDuration(String vLength){
        String parsedLength = "";
        for(int i = 2; i<vLength.length()-1;i++){
            if(Character.isDigit(vLength.charAt(i))){
                parsedLength += vLength.charAt(i);
            } else {
                parsedLength += ":";
            }
        }
        // supplements missing zeros from ISO8601 format
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

    // hands the data from the AsyncTasks over to the VideoListAdapter
    // ends the loading circle
    private void setVideoList(ArrayList videoInformation){
        for (int i=0; i<videoInformation.size(); i++){
            // hand over current VideoInformationModel from ArrayList (see GetVideos AsyncTask)
            VideoRequestHandler.GetVideos.VideoInformationModel currentInfoModel = (VideoRequestHandler.GetVideos.VideoInformationModel) videoInformation.get(i);
            // create new VideoDataModel from current VideoInformationModel
            VideoDataModel newVideoData = new VideoDataModel(currentInfoModel.videoTitle, currentInfoModel.videoID, currentInfoModel.videoLength, currentInfoModel.videoDescription);
            // add new VideoDataModel to arrayOfVideos-ArrayList for VideoListAdapter
            arrayOfVideos.add(newVideoData);
        }
        VideoListAdapter videoListAdapter = new VideoListAdapter(this, arrayOfVideos);   // create new Adapter
        ListView videoListView = (ListView) findViewById(R.id.videoList);                       // set ListView from VideoBib-xml
        videoListView.setAdapter(videoListAdapter);                                             // set Adapter to ListView
        progressBar.setVisibility(View.GONE);                                                   // set loading circle gone
    }

    // get continent/category by index from LinkedHashMap
    private String getContinentByIndex(LinkedHashMap map, int index){
        Set entrySet = map.entrySet();
        Map.Entry theEntry = (Map.Entry) entrySet.toArray()[index];
        return (String) theEntry.getKey();
    }

    // implements logic for 'Rondell' functionality
    private void addListenerOnChevrons() {
        // add listener to left chevron
        chevron_left = (ImageView)findViewById(R.id.category_chevron_left);
        chevron_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chevron_left.setClickable(false);
                chevron_right.setClickable(false);

                // clicking on the left chevron moves 'Rondell' to previous continent/category in the list
                Integer continentIndex = (Integer) getContinentIndex(continents, (String) continentTitle.getText());
                String previousContinent;
                if (continentIndex - 1 < 0) {                       // if the current continent is the first in the list switch to the last
                    previousContinent = getContinentByIndex(continents, continents.size() -1);
                } else {
                    previousContinent = getContinentByIndex(continents, continentIndex -1);
                }
                switchToNewContinent(previousContinent);
            }
        });

        // add listener to right chevron
        chevron_right = (ImageView)findViewById(R.id.category_chevron_right);
        chevron_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chevron_left.setClickable(false);
                chevron_right.setClickable(false);

                // clicking on the right chevron moves 'Rondell' to next continent/category in the list
                Integer continentIndex = (Integer) getContinentIndex(continents, (String) continentTitle.getText());
                String nextContinent;
                if (continentIndex + 1 > continents.size()-1) {     // if the current continent is the last in the list switch to the first
                    nextContinent = getContinentByIndex(continents, 0);
                } else {
                    nextContinent = getContinentByIndex(continents, continentIndex+1);
                }
                switchToNewContinent(nextContinent);
            }
        });
    }

    private void switchToNewContinent(String newContinent){
        currentContinent = newContinent;                               // set current continent for onResume
        continentTitle.setText(newContinent);                          // set new continent
        setContinentColor(newContinent);                               // set new matching color
        resetVideoView();                                              // remove old videos
        getContinentVideos((String) continents.get(newContinent));     // set new videos
    }

    // get index of specific continent/category
    private Integer getContinentIndex(LinkedHashMap map, String continent) {
        Integer index;
        index = new ArrayList<String>(map.keySet()).indexOf(continent);
        return index;
    }

    // **************************************************************************

    public class VideoListAdapter extends ArrayAdapter<VideoDataModel> {

        private String PRE_REQUEST = "https://i.ytimg.com/vi/";     // pre-part of request for thumbnail-images
        private String POST_REQUEST = "/mqdefault.jpg";             // post-part of request for thumbnail-images

        private class VideoViewHolder {
            TextView videoTitle;
            TextView videoLength;
            TextView videoDescription;
            YouTubeThumbnailView videoThumbnail;
            ProgressBar videoLoad;
            CardView videoCard;
        }

        // constructor
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

                // sets xml-views to videoViewHolder
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

            // set video-information texts
            videoViewHolder.videoTitle.setText(videoDataModel.videoTitle);                  // set video title
            videoViewHolder.videoLength.setText(videoDataModel.videoLength);                // set video duration
            // if description is empty, set no-description-placeholder
            if(videoDataModel.videoDescription.equals("")){
                videoViewHolder.videoDescription.setText(R.string.no_description_placeholder);
            } else {
                videoViewHolder.videoDescription.setText(videoDataModel.videoDescription);  // set video description
            }

            // add listener to video-item-card for switching to information activity
            videoViewHolder.videoCard.setTag(videoDataModel.videoID);
            addListenerOnCards(videoViewHolder.videoCard, videoViewHolder.videoLength.getText().toString());

            // getting thumbnail-images via asyncTask
            String urlRequest = PRE_REQUEST + videoDataModel.videoID + POST_REQUEST;
            VideoRequestHandler.GetImage imageAsyncTask = (VideoRequestHandler.GetImage) new VideoRequestHandler.GetImage(new VideoRequestHandler.GetImage.AsyncResponse(){
                @Override
                public void processFinish(Drawable output) {
                    videoViewHolder.videoThumbnail.setImageDrawable(output);                // set video thumbnail-image
                    videoViewHolder.videoLoad.setVisibility(View.GONE);
                    videoViewHolder.videoThumbnail.setVisibility(View.VISIBLE);
                }
            }).execute(urlRequest);

            return convertView;
        }

        // add listener on cards to switch to InformationActivity
        private void addListenerOnCards(final CardView videoCard, final String videoLength) {
            final Context context = getApplicationContext();
            // registering button and button-behaviour by on-click
            videoCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    // start loader on First Screen for loading-user-feedback
                    if(srClient != null){
                        srClient.sendMsg("startLoader");
                    }

                    Intent intent = new Intent(context, InformationActivity.class);
                    // pass videoId via intent to InformationActivity for getting right video-metainformation
                    String videoID = new String(videoCard.getTag().toString());
                    intent.putExtra("videoID", videoID);
                    // pass video duration via intent to InformationActivity for showing correct video-duration-progress
                    intent.putExtra("videoLength", videoLength);

                    startActivity(intent);
                }
            });
        }
    }

    // **************************************************************************

    // VideoDataModel for VideoListAdapter
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
