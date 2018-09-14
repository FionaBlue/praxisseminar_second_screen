package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InformationActivity extends AppCompatActivity {

    // src:
    // ********
    // https://www.codeproject.com/Articles/1152595/Android-Horizontal-ListView-Tutorial
    // https://www.youtube.com/watch?v=94rCjYxvzEE
    // https://stackoverflow.com/questions/26682277/how-do-i-get-the-position-selected-in-a-recyclerview
    // https://stackoverflow.com/questions/30340591/changing-an-imageview-to-black-and-white

    public List<ContentElement> contentElements = new ArrayList<>();    // dynamical array for content information
    RecyclerView recyclerListView;                                      // providing option for horizontal list view (= recyclerView)
    CustomAdapter adapter = new CustomAdapter();                        // adapter to fill list view with trigger points (timeline)
    private TextView wikiContentTitle;                                  // text view for showing wiki-requested title content
    private TextView wikiContentExtract;                                // text view for showing wiki-requested extract content
    private ImageView wikiContentImage;                                 // image view for showing wiki-requested image content
    private ImageButton buttonInformationNext;                          // button for switching to next activity (temporary!)
    private Button buttonReadArticle;                                   // button for opening up article in new external web browser
    private ProgressBar progressBar;                                    // progress loader which waits for information content to be loaded
    private ImageView arrowLeft, arrowRight;                            // arrows/chevrons for switching to next/previous timeline point
    // icons for controlling current video
    private ImageView playIcon, pauseIcon, forwardIcon, backwardIcon, volumeUpIcon, volumeDownIcon;

    // transferred data (video-id, signalR-client)
    private String videoId = "Nbrx5tFJzyQ";                             // id of currently loaded video (for retrieving specific information)
    private SignalRClient sRClient;                                     // signalR-client for communication between devices (first screen/second screen)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) for information activity
        super.onCreate(savedInstanceState);

        // getting selected video-id from previous activity (via registered tag)
        //registerCurrentVideoId();

        // getting signalR-client from application (so, preventing newly instantiating new signalR-client)
        getSRClient();

        // setting layout components after all information was loaded (especially for adapter list view!)
        registerLayoutComponents();

        // getting all content information (from firebase and wikipedia afterwards)
        getInformationContent();

        // registering ui component listener
        addListenersOnUiComponents();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void registerCurrentVideoId() {
        // getting selected video-id (registered tag/key) from previous activity via intent-extras
        Bundle extras = getIntent().getExtras();
        videoId = extras.getString("videoID");
        System.out.println("VideoID through intent: " + videoId);
    }

    private void getSRClient() {
        WildLive app = (WildLive)getApplication();
        sRClient = app.getSRClient();
        System.out.println("Information Client " + sRClient);
    }

    private void registerLayoutComponents() {
        // defining actual layout group
        setContentView(R.layout.activity_information);

        // registering and starting progress bar loader that waits for wiki information content to be loaded
        progressBar = (ProgressBar) findViewById(R.id.informationLoader);
        progressBar.setVisibility(View.VISIBLE);

        // registering layout and layout-components
        wikiContentImage = (ImageView) findViewById(R.id.wikiImage);
        wikiContentTitle = (TextView) findViewById(R.id.currentWikiTitle);
        wikiContentExtract = (TextView) findViewById(R.id.currentWikiExtract);
        wikiContentExtract.setMovementMethod(new ScrollingMovementMethod());        // making extract content scrollable
        buttonInformationNext = (ImageButton) findViewById(R.id.buttonInformationNext);
        buttonReadArticle = (Button) findViewById(R.id.buttonReadArticle);

        // registering icons for video-control-function
        playIcon = findViewById(R.id.video_play);
        pauseIcon = findViewById(R.id.video_pause);
        forwardIcon = findViewById(R.id.video_forward);
        backwardIcon = findViewById(R.id.video_replay);
        volumeUpIcon = findViewById(R.id.video_volumeUp);
        volumeDownIcon = findViewById(R.id.video_volumeDown);

        // registering list view (recyclerView) with arrows/chevrons and custom array adapter
        arrowLeft = (ImageView) findViewById(R.id.timelineChevronLeft);
        arrowRight = (ImageView) findViewById(R.id.timelineChevronRight);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);   // setting orientation to horizontal!
        recyclerListView = (RecyclerView) findViewById(R.id.timeline);
        recyclerListView.setLayoutManager(layoutManager);
        recyclerListView.setAdapter(adapter);   // binding data from separate timeline-item layout to activity layout via adapter
    }

    private void getInformationContent() {
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext(), new DatabaseHandler.AsyncResponse() {
            @Override
            public void processFinished(ArrayList<DatabaseHandler.VideoTriggerPoint> output) {
                for (DatabaseHandler.VideoTriggerPoint currItem : output) {
                    // adding new item filled with retrieved database content (wikipedia-content will be loaded later on)
                    contentElements.add(new ContentElement(currItem.triggerPointTitle,"", "", (currItem.triggerPointId == 0) ? true : false, "", currItem.triggerPointImageUrl, currItem.triggerPointTimestamp, currItem.triggerPointBitmap));
                }
                getContentInformationFromWiki();
            }
        });
        // setting entry point for getting all information (image-storage-link, title, timestamp) from firebase database
        databaseHandler.setDatabaseReference(FirebaseDatabase.getInstance().getReference("video/" + videoId + "/trigger_point"));
        // setting entry point for getting image file (title.jpg) from firebase storage
        databaseHandler.setStorageReference(FirebaseStorage.getInstance().getReference("video/" + videoId + "/images"));
        // setting category (video vs. quiz) for handling different firebase structures
        databaseHandler.setDataCategory(DatabaseHandler.DataCategory.VIDEO);

        // starting parallel process for retrieving database-data (asynctask)
        databaseHandler.execute();
    }

    private void getContentInformationFromWiki() {
        for (final ContentElement wikiElement : contentElements) {
            // calling url-request and getting each wiki content (in array) via async task
            new WikiRequestHandler(new WikiRequestHandler.AsyncResponse() {
                @Override
                public void processFinished(Object output) {
                    WikiRequestHandler.WikiContentElements contentOutput = (WikiRequestHandler.WikiContentElements) output;

                    // refreshing all retrieved information in array list
                    wikiElement.title = contentOutput.wikiContentTitle;
                    wikiElement.extract = contentOutput.wikiContentExtract;
                    wikiElement.extArticle = contentOutput.wikiContentArticle;

                    // loading responded information and local image in card view elements
                    if (wikiElement.isActive == true) {
                        integrateInformation(wikiElement.title, wikiElement.extract, wikiElement.imageBitmap);
                    }

                    // checking if last content element information was retrieved, than stopping progress bar and refresh adapter
                    if (wikiElement == contentElements.get(contentElements.size() - 1)) {
                        adapter.triggerPointList.clear();           // clearing list of trigger points for rearranging
                        adapter.notifyDataSetChanged();             // updating recycler-view-data (else nothing will show up after progress-bar loaded)
                        progressBar.setVisibility(View.GONE);       // deactivating progress-loader

                        //sendVideoId to First Screen to start playing the specific video
                        if(sRClient != null){
                            sRClient.sendMsg("playVideo" + videoId);
                        }
                    }
                }
            }).execute(wikiElement.identifier);
        }
    }

    public void integrateInformation(String wikiTitle, String wikiExtract, Bitmap imageBitmap) {
        // loading responded information in card view elements
        wikiContentTitle.setText(wikiTitle);
        wikiContentExtract.setText(wikiExtract);
        // loading responded image in image view (from retrieved database bitmap)
        wikiContentImage.setImageBitmap(imageBitmap);
    }

    public void addListenersOnUiComponents() {
        final Context context = this;
        // registering button and button-behaviour by on-clicking
        buttonInformationNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                //marker must be set inactive if no ad is shown, after ad started and the
                //quiz is being closed, the marker must be set active so the user can start
                //the quiz again and continue the quiz
                Intent intent = new Intent(context, QuizActivity.class);
                startActivity(intent);
            }
        });

        // registering arrow-behaviour by on-clicking
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            // switching to previous timeline item
            public void onClick(View arg0) { adapter.switchTriggerPoint(-1); }
        });
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            // switching to next timeline item
            public void onClick(View arg0) { adapter.switchTriggerPoint(+1); }
        });

        // registering button for switching to external web browser (for reading further information, article)
        buttonReadArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentArticleHttps = contentElements.get(adapter.getItemActivationState()).extArticle;
                // opening up new external browser with current wikipedia link
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentArticleHttps));
                startActivity(browserIntent);
            }
        });

        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { playIcon.setVisibility(View.GONE); pauseIcon.setVisibility(View.VISIBLE); if(sRClient != null){ sRClient.sendMsg("icon play"); }}
        });
        pauseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { pauseIcon.setVisibility(View.GONE); playIcon.setVisibility(View.VISIBLE); if(sRClient != null){ sRClient.sendMsg("icon pause"); }}
        });
        forwardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { if(sRClient != null){ sRClient.sendMsg("icon forward"); System.out.println("forward"); }}
        });
        backwardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { if(sRClient != null){ sRClient.sendMsg("icon backward"); }}
        });
        volumeUpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { if(sRClient != null){ sRClient.sendMsg("icon volumeUp"); }}
        });
        volumeDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { if(sRClient != null){ sRClient.sendMsg("icon volumeDown"); }}
        });
    }

    // custom adapter for binding list view data
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        public List<CircleImageView> triggerPointList = new ArrayList<>();      // list of registered trigger-points for handling highlighting
        private List<Integer> prevPositions = new ArrayList<>();                // list for handling trigger-point registration
        public int prevPosition;                                                // previous item for setting back highlighting

        @Override
        // defining recycler-view item count
        public int getItemCount() {
            return contentElements.size();
        }

        @NonNull
        @Override
        // view holder will be created for each item in recycler-view
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // binding the layout file - each individual layout will be inflated/filled
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.timeline_item, viewGroup, false);
            return new ViewHolder(view);
        }

        // view holder for performance issues with recyclerView (only visible items are handled)
        public class ViewHolder extends RecyclerView.ViewHolder {
            public CircleImageView timelineItem;        // substitute for circled item image
            public View timelineItemDivider;            // substitute for dotted divider

            public ViewHolder(@NonNull final View itemView) {
                super(itemView);

                // defining trigger-point-image and setting saturation to gray-scale
                timelineItem = itemView.findViewById(R.id.timelineItem);
                setTriggerPointSaturation(timelineItem, 0);

                // defining trigger-point-divider
                timelineItemDivider = itemView.findViewById(R.id.timelineItemDivider);
            }
        }

        @Override
        // preventing loop of paired-objects
        public int getItemViewType(int position) {
            // https://stackoverflow.com/questions/48845814/recycler-view-adapter-looping-objects-in-pairs
            return position;
        }

        @Override
        // binding each view holder (timeline-item substitutes)
        public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
            final int position = i;

            // checking if position was already registered for adding timeline-items to list
            if ((position < getItemCount()) && (prevPositions.contains(position) == false)) {
                triggerPointList.add(viewHolder.timelineItem);
                prevPositions.add(position);

                // handling first timeline-item in list
                if (triggerPointList.size() == 1) {
                    // setting activated highlighting for first item in timeline-item-list
                    prevPosition = 0;
                    setItemActivationState(prevPosition);

                    // setting arrow for scrolling left invisible to emphasize possible scroll-direction
                    arrowLeft.setVisibility(View.INVISIBLE);
                }
            }
            // setting image for current timeline-item
            viewHolder.timelineItem.setImageBitmap(contentElements.get(position).imageBitmap);

            // handling on-click behaviour (loading information and setting highlighting for timeline-item)
            viewHolder.timelineItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClicked(position);
                }
            });

            // setting divider invisible for last trigger point item
            if (position == contentElements.size()-1) {
                viewHolder.timelineItemDivider.setVisibility(View.INVISIBLE);
            }
        }

        private void onItemClicked(int position) {
            // loading information data for specific, clicked trigger point
            integrateInformation(contentElements.get(position).title, contentElements.get(position).extract, contentElements.get(position).imageBitmap);

            // setting highlighting for current clicked trigger point
            setItemActivationState(position);
            prevPosition = position;

            // activating/deactivating timeline arrows (on border reached) for emphasizing possible scroll-directions
            if (position == 0) {
                arrowLeft.setVisibility(View.INVISIBLE);
                arrowRight.setVisibility(View.VISIBLE);
            } else if (position == getItemCount()-1) {
                arrowRight.setVisibility(View.INVISIBLE);
                arrowLeft.setVisibility(View.VISIBLE);
            } else {
                arrowLeft.setVisibility(View.VISIBLE);
                arrowRight.setVisibility(View.VISIBLE);
            }
        }

        private void setItemActivationState(int position) {
            // changing activation state
            contentElements.get(prevPosition).isActive = false;
            contentElements.get(position).isActive = true;

            // setting highlighting
            setTriggerPointSaturation(triggerPointList.get(prevPosition), 0);
            setTriggerPointSaturation(triggerPointList.get(position), 1);
        }

        private int getItemActivationState() {
            int activatedPosition = -1;
            for (int i = 0; i < getItemCount(); i++) {
                if (contentElements.get(i).isActive == true) {
                    activatedPosition = i;
                }
            }
            return activatedPosition;
        }

        // setting saturation value for images
        private void setTriggerPointSaturation(CircleImageView triggerPoint, int saturation) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(saturation);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            triggerPoint.setColorFilter(filter);
        }

        // switching trigger point after arrow (left or right) was clicked
        private void switchTriggerPoint(int direction) {
            // getting position of previous or next trigger point
            int switchToTriggerPointPosition = getItemActivationState() + direction;

            // checking if previous or next trigger point is in range
            if ((switchToTriggerPointPosition >= 0) && (switchToTriggerPointPosition <= getItemCount()-1)) {
                // scrolling timeline automatically
                recyclerListView.smoothScrollBy(direction*200, 0);
                recyclerListView.smoothScrollToPosition(switchToTriggerPointPosition);

                // calling on-click for previous/next timeline-item on arrow-clicked
                triggerPointList.get(switchToTriggerPointPosition).callOnClick();
            }
        }
    }

    // defining content element object for storing specific data (for each trigger/content point)
    class ContentElement {
        String identifier;
        String title;
        String extract;
        Boolean isActive;
        String extArticle;
        String imageUrl;
        String timestamp;
        Bitmap imageBitmap;

        // constructor
        public ContentElement(String identifier, String title, String extract, Boolean isActive, String extArticle, String imageUrl, String timestamp, Bitmap imageBitmap) {
            this.identifier = identifier;
            this.title = title;
            this.extract = extract;
            this.isActive = isActive;
            this.extArticle = extArticle;
            this.imageUrl = imageUrl;
            this.timestamp = timestamp;
            this.imageBitmap = imageBitmap;
        }
    }
}
