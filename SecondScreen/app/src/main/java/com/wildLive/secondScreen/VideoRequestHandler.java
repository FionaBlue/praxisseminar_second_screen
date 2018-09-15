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

public class VideoRequestHandler {


    // **************************************************************************
    //https://stackoverflow.com/questions/6407324/how-to-display-image-from-url-on-android

    public static class GetImage extends AsyncTask<String, Void, Drawable> {

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
    //https://stackoverflow.com/questions/17549042/android-asynctask-passing-a-single-string

    public static class GetVideos extends AsyncTask<String, Void, ArrayList> {
        public AsyncResponse delegate = null;
        private Exception exception;
        private String PLAYLIST_REQUEST = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50";
        private String PLAYLIST_PREFIX = "&playlistId=";
        private String DEVELOPERKEY = "&key=AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI";

        public GetVideos(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public interface AsyncResponse {
            void processFinish(ArrayList output);
        }

        protected void onPostExecute (ArrayList result) {
            delegate.processFinish(result);
        }

        @Override
        protected ArrayList doInBackground(String... params) {


            final ArrayList videoArray = new ArrayList();

            HttpClient httpclient = new DefaultHttpClient();
            String playlistID = params[0];
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
                for (int i=0; i<playlistVideosArray.length(); i++){
                    final VideoInformationModel videoInformationModel = new VideoInformationModel();
                    JSONObject video = playlistVideosArray.getJSONObject(i);
                    JSONObject snippet = video.getJSONObject("snippet");
                    JSONObject resourceId = snippet.getJSONObject("resourceId");
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

        public static class VideoInformationModel {
            String videoTitle;
            String videoDescription;
            String videoLength;
            String videoID;
        }
    }


    // **************************************************************************

    public static class GetPlaylists extends AsyncTask<String, Void, LinkedHashMap> {

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

        private String DEVELOPERKEY = "&key=AIzaSyBlkMtESdOPSEVaSDGU9z5BhFJ5NbBLBmI";
        private String VIDEO_REQUEST = "https://www.googleapis.com/youtube/v3/videos?id=";
        private String VIDEO_REQUEST_CONTENTDETAILS = "&part=contentDetails";

        private String inputID;
        private AsyncResponse delegate;
        private Exception exception;

        String videoLength = "0:00";

        public GetVideoLength(AsyncResponse delegate){
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

            inputID = params[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(VIDEO_REQUEST + inputID + VIDEO_REQUEST_CONTENTDETAILS + DEVELOPERKEY);
            org.apache.http.HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
            } catch (Exception e) {
                this.exception = e;
            }
            String responseAsString;
            JSONObject responseAsJSONObject = null;
            JSONArray videoLengthArray;
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
            try {
                videoLengthArray = responseAsJSONObject.getJSONArray("items");
                for (int i = 0; i < videoLengthArray.length(); i++) {
                    JSONObject videoContent = videoLengthArray.getJSONObject(i);
                    JSONObject contentDetails = videoContent.getJSONObject("contentDetails");
                    String videoDuration = contentDetails.getString("duration");
                    videoLength = videoDuration;
                }
            } catch (JSONException e){

            }
            return videoLength;
        }
    }
}
