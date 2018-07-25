package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

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
}
