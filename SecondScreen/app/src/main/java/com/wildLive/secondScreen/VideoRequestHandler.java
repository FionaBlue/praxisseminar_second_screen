package com.wildLive.secondScreen;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

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

// src:
// https://stackoverflow.com/questions/6407324/how-to-display-image-from-url-on-android
// https://stackoverflow.com/questions/17549042/android-asynctask-passing-a-single-string

class VideoRequestHandler {

    // AsyncResponse is used for getting AsyncTask result-output in executing Activity

    // developer key is needed for YouTube http requests
    public static String DEVELOPERKEY = "&key=AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI";

    // **************************************************************************

    public static class GetImage extends AsyncTask<String, Void, Drawable> {

        private String inputURL;
        private AsyncResponse delegate;
        private InputStream is = null;

        GetImage(AsyncResponse delegate){
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

            // get passed URL
            inputURL = params[0];

            // try url-content-request
            try {
                is = (InputStream) new URL(inputURL).getContent();
            } catch (IOException e) { e.printStackTrace(); }

            // get drawable from inputStream
            Drawable d = Drawable.createFromStream(is, "Thumbnail");

            return d;
        }
    }

    // **************************************************************************

    public static class GetVideos extends AsyncTask<String, Void, ArrayList> {

        AsyncResponse delegate = null;
        private Exception exception;

        // parts of request-URL
        private String PLAYLIST_REQUEST = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50";
        private String PLAYLIST_ID_PREFIX = "&playlistId=";

        private String responseAsString;
        private JSONObject responseAsJSONObject = null;
        private JSONArray playlistVideosArray;

        GetVideos(AsyncResponse delegate){ this.delegate = delegate; }

        public interface AsyncResponse { void processFinish(ArrayList output); }

        protected void onPostExecute (ArrayList result) { delegate.processFinish(result); }

        @Override
        protected ArrayList doInBackground(String... params) {

            // creates new ArrayList for return
            final ArrayList videoArray = new ArrayList();

            // get passed id
            String playlistID = params[0];

            // initialise http-request
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(PLAYLIST_REQUEST + PLAYLIST_ID_PREFIX + playlistID + DEVELOPERKEY);
            org.apache.http.HttpResponse response = null;

            // try http-request
            try {
                response = httpclient.execute(httpget);
            } catch (Exception e) {
                this.exception = e;
            }

            // get and convert response to JSON
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

            // get video-id, -description and -title values from JSON
            try {
                playlistVideosArray = responseAsJSONObject.getJSONArray("items");
                for (int i=0; i<playlistVideosArray.length(); i++){
                    // create new VideoInformationModel
                    final VideoInformationModel videoInformationModel = new VideoInformationModel();

                    JSONObject video = playlistVideosArray.getJSONObject(i);
                    JSONObject snippet = video.getJSONObject("snippet");
                    JSONObject resourceId = snippet.getJSONObject("resourceId");

                    // add values to VideoInformationModel
                    videoInformationModel.videoID = resourceId.getString("videoId");
                    videoInformationModel.videoLength = "0:00";
                    videoInformationModel.videoDescription = snippet.getString("description");
                    videoInformationModel.videoTitle = snippet.getString("title");
                    videoArray.add(videoInformationModel);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return videoArray;
        }

        // Model for passing values to executing Activity
        public static class VideoInformationModel {
            String videoTitle;
            String videoDescription;
            String videoLength;
            String videoID;
        }
    }


    // **************************************************************************

    public static class GetPlaylists extends AsyncTask<String, Void, LinkedHashMap> {

        // fixed request with channelID from WildLive YouTube Channel
        private String PLAYLISTS_REQUEST = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=UC2ETsCbgegY8iqQFwS9Xi4w&key=AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI&maxResults=50";

        AsyncResponse delegate = null;
        private Exception exception;

        private String responseAsString;
        private JSONObject responseAsJSONObject = null;
        private JSONArray playlistsArray;

        GetPlaylists(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public interface AsyncResponse {
            void processFinish(LinkedHashMap output);
        }

        protected LinkedHashMap doInBackground(String... string) {

            // creates new LinkedHashMap for return
            LinkedHashMap<String, String> playlistRondell = new LinkedHashMap<String, String>();

            // initialise http request
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(PLAYLISTS_REQUEST);
            org.apache.http.HttpResponse response = null;

            // try http-request
            try {
                response = httpclient.execute(httpget);
            } catch (Exception e) {
                this.exception = e;
            }

            // get and convert response to JSON
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

            // get title and id values from JSON
            try {
                playlistsArray = responseAsJSONObject.getJSONArray("items");
                for (int i=0; i<playlistsArray.length(); i++){
                    JSONObject playlist = playlistsArray.getJSONObject(i);
                    JSONObject snippet = playlist.getJSONObject("snippet");
                    playlistRondell.put(snippet.getString("title"), playlist.getString("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return playlistRondell;
        }

        protected void onPostExecute(LinkedHashMap result) {
            delegate.processFinish(result);
        }
    }

    //**************************************************************************

    public static class GetVideoLength extends AsyncTask<String, Void, String> {

        // parts of request-URL
        private String VIDEO_REQUEST = "https://www.googleapis.com/youtube/v3/videos?id=";
        private String VIDEO_REQUEST_CONTENTDETAILS = "&part=contentDetails";
        private String inputID;

        private AsyncResponse delegate;
        private Exception exception;

        private String videoLength = "0:00";
        private JSONArray videoLengthArray;
        private String responseAsString;
        private JSONObject responseAsJSONObject = null;

        GetVideoLength(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public interface AsyncResponse {
            void processFinish(String output);
        }

        protected void onPostExecute (String result) {
            delegate.processFinish(result);
        }

        @Override
        protected String doInBackground(String... params) {

            // get passed id
            inputID = params[0];

            // initialise http-request
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(VIDEO_REQUEST + inputID + VIDEO_REQUEST_CONTENTDETAILS + DEVELOPERKEY);
            org.apache.http.HttpResponse response = null;

            // try http-request
            try {
                response = httpclient.execute(httpget);
            } catch (Exception e) {
                this.exception = e;
            }

            // get and convert response to JSON
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

            // get duration value from JSON
            try {
                videoLengthArray = responseAsJSONObject.getJSONArray("items");
                for (int i = 0; i < videoLengthArray.length(); i++) {
                    JSONObject videoContent = videoLengthArray.getJSONObject(i);
                    JSONObject contentDetails = videoContent.getJSONObject("contentDetails");
                    String videoDuration = contentDetails.getString("duration");
                    videoLength = videoDuration;
                }
            } catch (JSONException e){
                e.printStackTrace();
            }

            return videoLength;
        }
    }
}
