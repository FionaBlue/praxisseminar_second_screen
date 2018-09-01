package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
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

    private ProgressBar progressBar;

    ArrayList<VideoDataModel> arrayOfVideos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) on creating app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videobib);
        progressBar = findViewById(R.id.load_videos);
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

    private void resetVideoView(){
        arrayOfVideos.clear();
        LinkedHashMap emptyMap = new LinkedHashMap();
        setVideoList(emptyMap);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void updateContinentTitle(LinkedHashMap map, Integer index) {
        continentTitle.setText((String) getContinentByIndex(map, index));
        String continentName = (String) continentTitle.getText();
        String continentID = (String) map.get(continentName);
        arrayOfVideos.clear();
        getContinentVideos(continentID);
    }

    private void getContinentVideos(String continent) {
        GetVideos videoAsyncTask = (GetVideos) new GetVideos(new GetVideos.AsyncResponse(){

            @Override
            public void processFinish(LinkedHashMap output){
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                System.out.println("Videos " + output);
                setVideoList(output);
            }
        }).execute(continent);
    }

    private void setVideoList(LinkedHashMap map){

        List videotitles = new ArrayList(map.keySet());
        System.out.println("VT " + videotitles.toString());

        for (int i=0; i<videotitles.size(); i++){
            String actualTitle = videotitles.get(i).toString();
            VideoDataModel newVideoData = new VideoDataModel(actualTitle, map.get(actualTitle).toString(), "0:00");
            arrayOfVideos.add(newVideoData);
        }

        VideoListAdapter videoListAdapter = new VideoListAdapter(this, arrayOfVideos);
        ListView videoListView = (ListView) findViewById(R.id.videoList);
        videoListView.setAdapter(videoListAdapter);
        //https://stackoverflow.com/questions/18708955/invisible-components-still-take-up-space
        progressBar.setVisibility(View.GONE);
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
                    resetVideoView();
                    getContinentVideos((String) continents.get(lastContinent));
                } else {
                    String lastContinent = getContinentByIndex(continents, continentIndex -1);
                    continentTitle.setText(lastContinent);
                    resetVideoView();
                    getContinentVideos((String) continents.get(lastContinent));
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
                    resetVideoView();
                    getContinentVideos((String) continents.get(nextContinent));
                } else {
                    String nextContinent = getContinentByIndex(continents, continentIndex+1);
                    continentTitle.setText(nextContinent);
                    resetVideoView();
                    getContinentVideos((String) continents.get(nextContinent));
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
    //https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
    //https://stackoverflow.com/questions/35606368/java-lang-illegalstateexception-not-connected-call-connect-youtube-api

    public class VideoListAdapter extends ArrayAdapter<VideoDataModel> {

        public static final String DEVELOPER_KEY = "AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI";
        public boolean loadThumbnail = true;
        private String PRE_REQUEST = "https://i.ytimg.com/vi/";
        private String POST_REQUEST = "/mqdefault.jpg";

        private class VideoViewHolder {
            TextView videoTitle;
            TextView videoLength;
            YouTubeThumbnailView videoThumbnail;
        }

        public VideoListAdapter(Context context, ArrayList<VideoDataModel> videoDataModels) {
            super(context, R.layout.videolist_item, videoDataModels);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            VideoDataModel videoDataModel = getItem(position);
            final VideoViewHolder videoViewHolder;
            if (convertView == null) {
                videoViewHolder = new VideoViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.videolist_item, parent, false);

                videoViewHolder.videoTitle = (TextView) convertView.findViewById(R.id.titleView);
                videoViewHolder.videoLength = (TextView) convertView.findViewById(R.id.lengthView);
                videoViewHolder.videoThumbnail = (YouTubeThumbnailView) convertView.findViewById(R.id.thumbnailView);
                convertView.setTag(videoViewHolder);
            } else {
                videoViewHolder = (VideoViewHolder) convertView.getTag();
            }
            videoViewHolder.videoTitle.setText(videoDataModel.videoTitle);
            videoViewHolder.videoLength.setText(videoDataModel.videoLength);
            videoViewHolder.videoThumbnail.setTag(videoDataModel.videoID);
            System.out.println("TAG " + videoViewHolder.videoThumbnail.getTag().toString());
            System.out.println("thumbnails" + videoDataModel.videoID);
            //GetThumbnails asyncTask = (GetThumbnails) new GetThumbnails().execute(videoViewHolder.videoThumbnail);
            String urlRequest = PRE_REQUEST + videoDataModel.videoID + POST_REQUEST;
            GetImage imageAsyncTask = (GetImage) new GetImage(new GetImage.AsyncResponse(){

                @Override
                public void processFinish(Drawable output) {
                    videoViewHolder.videoThumbnail.setImageDrawable(output);
                }
            }).execute(urlRequest);
            return convertView;
        }
    }

    // **************************************************************************
    //https://stackoverflow.com/questions/6407324/how-to-display-image-from-url-on-android
    private static class GetImage extends AsyncTask<String, Void, Drawable> {

        private String inputURL;
        private AsyncResponse delegate;

        public GetImage(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public interface AsyncResponse {
            void processFinish(Drawable output);
        }

        protected void onPostExecute (Drawable result) {
            delegate.processFinish(result);
        }


        @Override
        protected Drawable doInBackground(String... params) {
            inputURL = params[0];
            InputStream is = null;
            try {
                is = (InputStream) new URL(inputURL).getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Drawable d = Drawable.createFromStream(is, "Thumbnail");
            return d;
        }
    }

    // **************************************************************************

    public class VideoDataModel {
        String videoTitle;
        String videoID;
        String videoLength;

        public VideoDataModel(String videoTitle, String videoID, String videoLength) {
            this.videoTitle = videoTitle;
            this.videoID = videoID;
            this.videoLength = videoLength;
        }
    }

    // **************************************************************************
    //https://stackoverflow.com/questions/17549042/android-asynctask-passing-a-single-string
    private static class GetVideos extends AsyncTask<String, Void, LinkedHashMap> {
        public AsyncResponse delegate = null;
        private Exception exception;
        private String PLAYLIST_REQUEST = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50";
        private String PLAYLIST_PREFIX = "&playlistId=";
        private String DEVELOPERKEY = "&key=AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI";
        private String TEST_REQUEST = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=PLrEmzVduftH1S9XyRT5F9AZNt9ZzopwE5&key=AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI";

        public GetVideos(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public interface AsyncResponse {
            void processFinish(LinkedHashMap output);
        }

        protected void onPostExecute (LinkedHashMap result) {
            delegate.processFinish(result);
        }

        @Override
        protected LinkedHashMap doInBackground(String... params) {

            LinkedHashMap <String, String> playlistVideos = new LinkedHashMap<>();

            HttpClient httpclient = new DefaultHttpClient();
            String playlistID = params[0];
            //System.out.println("playlistID" + playlistID);
            HttpGet httpget = new HttpGet(PLAYLIST_REQUEST + PLAYLIST_PREFIX + playlistID + DEVELOPERKEY);
            org.apache.http.HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
            } catch (Exception e) {
                this.exception = e;
            }
            String responseAsString;
            JSONObject responseAsJSONObject = null;
            JSONArray playlistVideosArray;

            try {
                responseAsString = EntityUtils.toString(response.getEntity());
                //System.out.println(responseAsString);
                try {
                    responseAsJSONObject = new JSONObject(responseAsString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                playlistVideosArray = responseAsJSONObject.getJSONArray("items");
                //System.out.println(playlistVideosArray);
                for (int i=0; i<playlistVideosArray.length(); i++){
                    JSONObject video = playlistVideosArray.getJSONObject(i);
                    //System.out.println(video);
                    JSONObject snippet = video.getJSONObject("snippet");
                    JSONObject resourceId = snippet.getJSONObject("resourceId");
                    playlistVideos.put(snippet.getString("title"), resourceId.getString("videoId"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //System.out.println("PV" + playlistVideos);
            return playlistVideos;
        }
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
