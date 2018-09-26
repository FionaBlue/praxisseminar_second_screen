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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
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
    // https://stackoverflow.com/questions/50668810/textview-settext-does-nothing
    // https://stackoverflow.com/questions/48845814/recycler-view-adapter-looping-objects-in-pairs
    // https://stackoverflow.com/questions/2887410/unclickable-seekbar-in-android-listview
    // https://stackoverflow.com/questions/14355731/killing-one-activity-from-another/14356774

    private WildLive app = (WildLive)getApplication();

    public List<ContentElement> contentElements = new ArrayList<>();        // dynamical array for content information
    RecyclerView recyclerListView;                                          // providing option for horizontal list view (= recyclerView)
    CustomAdapter adapter = new CustomAdapter();                            // adapter to fill list view with trigger points (timeline)
    private LinearLayout timelineRow;                                       // row for recyclerview and chevrons (for setting visibility when no data could be retrieved from database)
    private LinearLayout datalessCardView, guideCardView, contentCardView;  // card view layouts for showing guide or info content
    private TextView wikiContentTitle;                                      // text view for showing wiki-requested title content
    private TextView wikiContentExtract;                                    // text view for showing wiki-requested extract content
    private ImageView wikiContentImage;                                     // image view for showing wiki-requested image content
    private ImageButton quizStartButton;                                    // button for switching to next activity (temporary!)
    private Button buttonReadArticle;                                       // button for opening up article in new external web browser
    private ProgressBar progressBar;                                        // progress loader which waits for information content to be loaded
    private ImageView arrowLeft, arrowRight;                                // arrows/chevrons for switching to next/previous timeline point
    // icons for controlling current video
    private ImageView playIcon, pauseIcon, forwardIcon, backwardIcon, volumeUpIcon, volumeDownIcon, advertisementPauseIcon;
    // SeekBar for showing video progress
    private SeekBar videoProgress;

    // transferred data (video-length, video-id, signalR-client)
    private String videoLength;                                             // length of currently loaded video (for calculating video progress, see SeekBar)
    private String videoId = "";                                            // id of currently loaded video (for retrieving specific information)
    private SignalRClient sRClient;                                         // signalR-client for communication between devices (first screen/second screen)

    // transfer data to QuizActivity
    private int currentVideoProgress = 0;

    public enum InformationViewState { DATALESS, GUIDE, CONTENT }           // enum for choosing current layout state (guide or info content)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) for information activity
        super.onCreate(savedInstanceState);

        // getting selected video-id from previous activity (via registered tag)
        registerCurrentVideoInformation();

        // getting signalR-client from application (so, preventing newly instantiating new signalR-client)
        getSRClient();

        // setting layout components after all information was loaded (especially for adapter list view!)
        registerLayoutComponents();

        // getting all content information (from firebase and wikipedia afterwards)
        getInformationContent();

        // registering ui component listener
        addListenersOnUiComponents();

        // checking if quiz is available
        checkQuizActivityState();
    }

    @Override
    protected void onResume() {
        WildLive app = (WildLive)getApplication();
        playIcon.setVisibility(View.GONE);
        if(app.getQuizAvailalibity()){
            quizStartButton.setVisibility(View.VISIBLE);
            pauseIcon.setVisibility(View.GONE);
            advertisementPauseIcon.setVisibility(View.VISIBLE);
            sRClient.sendMsg("playAdvertisement");
        } else {
            quizStartButton.setVisibility(View.INVISIBLE);
            advertisementPauseIcon.setVisibility(View.GONE);
            pauseIcon.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        sRClient.sendMsg("stopVideo");
        super.onBackPressed();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void registerCurrentVideoInformation() {
        // getting selected video-id (registered tag/key) from previous activity via intent-extras
        Bundle extras = getIntent().getExtras();
        videoId = extras.getString("videoID");
        videoLength = extras.getString("videoLength");
    }

    private void getSRClient() {
        app = (WildLive)getApplication();
        sRClient = app.getSRClient();

        // receiving and handling messages from signalR-client (transferring data)
        if (sRClient != null) {
            sRClient.setMessageListener(new SignalRClient.SignalRCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    //Log.d("LOG_INFOACT", "received :: " + message);

                    if (message.toString().contains("index")) {
                        // converting message for right usage
                        final int messagePos = Integer.parseInt(message.substring(5, message.length()));

                        // returning to original ui-thread for handling ui-element (textViews, imageViews, etc.)
                        InformationActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // activate new trigger-point
                                adapter.revealNewTriggerPoint(messagePos);
                            }
                        });
                    }

                    // updating video-progress-SeekBar
                    if(message.toString().contains("time")){
                        //get video-length from intent
                        Bundle extras = getIntent().getExtras();
                        videoLength = extras.getString("videoLength");
                        //convert video-length to seconds
                        int videoLengthInSeconds;
                        videoLengthInSeconds = timeInSeconds(videoLength);
                        //get current video time from message as Double
                        String messageTime = message.substring(5, message.length());
                        Double timeAsDouble = Double.parseDouble(messageTime);
                        //calculate and set current video-progress in SeekBar
                        Double currentProgress = 100/(videoLengthInSeconds/timeAsDouble);
                        currentVideoProgress = Math.toIntExact(Math.round(currentProgress));
                        videoProgress.setProgress(currentVideoProgress);
                    }

                    // starting quiz while advertisement
                    if(message.toString().contains("start Quiz")){
                        app.setQuizAvailability(true);
                        final Context context = getApplicationContext();
                        Intent intent = new Intent(context, QuizActivity.class);
                        intent.putExtra("videoProgress", currentVideoProgress);
                        startActivity(intent);
                    }

                    // finishing quiz after advertisement
                    if(message.toString().contains("finish Quiz")){
                        QuizActivity.getInstance().finish();
                        app.setQuizAvailability(false);
                        InformationActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                quizStartButton.setVisibility(View.INVISIBLE);
                                advertisementPauseIcon.setVisibility(View.GONE);
                                pauseIcon.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    if(message.toString().contains("disable Quiz-answers")){
                        QuizActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                QuizActivity.getInstance().buttonA.setClickable(false);
                                QuizActivity.getInstance().buttonB.setClickable(false);
                                QuizActivity.getInstance().buttonC.setClickable(false);
                                QuizActivity.getInstance().buttonD.setClickable(false);

                                QuizActivity.getInstance().buttonA.setTextColor(getResources().getColor(R.color.colorLightGrey));
                                QuizActivity.getInstance().buttonB.setTextColor(getResources().getColor(R.color.colorLightGrey));
                                QuizActivity.getInstance().buttonC.setTextColor(getResources().getColor(R.color.colorLightGrey));
                                QuizActivity.getInstance().buttonD.setTextColor(getResources().getColor(R.color.colorLightGrey));
                            }
                        });
                    }

                    if(message.toString().contains("enable Quiz-answers")){
                        QuizActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                QuizActivity.getInstance().buttonA.setClickable(true);
                                QuizActivity.getInstance().buttonB.setClickable(true);
                                QuizActivity.getInstance().buttonC.setClickable(true);
                                QuizActivity.getInstance().buttonD.setClickable(true);

                                QuizActivity.getInstance().buttonA.setTextColor(getResources().getColor(R.color.colorBlack));
                                QuizActivity.getInstance().buttonB.setTextColor(getResources().getColor(R.color.colorBlack));
                                QuizActivity.getInstance().buttonC.setTextColor(getResources().getColor(R.color.colorBlack));
                                QuizActivity.getInstance().buttonD.setTextColor(getResources().getColor(R.color.colorBlack));
                            }
                        });
                    }
                }
                @Override
                public void onError(String message) { }
            });
        }
    }

    private int timeInSeconds(String time) {
        int inSeconds = 0;
        String[] timeUnits = time.split(":");
        if(timeUnits.length == 3){
            inSeconds = Integer.parseInt(timeUnits[0]) * 60 * 60 + Integer.parseInt(timeUnits[1]) * 60 + Integer.parseInt(timeUnits[2]);
        } else if(timeUnits.length == 2) {
            inSeconds = Integer.parseInt(timeUnits[0]) * 60 + Integer.parseInt(timeUnits[1]);
        } else if(timeUnits.length == 1) {
            inSeconds = Integer.parseInt(timeUnits[0]);
        }
        return inSeconds;
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
        quizStartButton = (ImageButton) findViewById(R.id.quizStartButton);
        buttonReadArticle = (Button) findViewById(R.id.buttonReadArticle);
        
        // registering timeline/videoprogress and prevent interaction
        videoProgress = findViewById(R.id.videoProgress);
        
        // registering icons for video-control-function
        playIcon = findViewById(R.id.video_play);
        pauseIcon = findViewById(R.id.video_pause);
        forwardIcon = findViewById(R.id.video_forward);
        backwardIcon = findViewById(R.id.video_replay);
        volumeUpIcon = findViewById(R.id.video_volumeUp);
        volumeDownIcon = findViewById(R.id.video_volumeDown);
        advertisementPauseIcon = findViewById(R.id.video_pause_Advertisement);

        // registering list view (recyclerView) with arrows/chevrons and custom array adapter
        arrowLeft = (ImageView) findViewById(R.id.timelineChevronLeft);
        arrowRight = (ImageView) findViewById(R.id.timelineChevronRight);
        arrowLeft.setVisibility(View.INVISIBLE);
        arrowRight.setVisibility(View.INVISIBLE);

        timelineRow = (LinearLayout) findViewById(R.id.timelineRow);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);   // setting orientation to horizontal!
        recyclerListView = (RecyclerView) findViewById(R.id.timeline);
        recyclerListView.setLayoutManager(layoutManager);
        recyclerListView.setAdapter(adapter);   // binding data from separate timeline-item layout to activity layout via adapter

        // registering different card view layouts (for initial guide and for revealed placeholder)
        datalessCardView = (LinearLayout) findViewById(R.id.datalessCardView);
        guideCardView = (LinearLayout) findViewById(R.id.guideCardView);
        contentCardView = (LinearLayout) findViewById(R.id.contentCardView);
        activateInformationState(InformationViewState.GUIDE);   // initially registering guide state
    }

    public void activateInformationState(InformationViewState state) {
        switch (state) {
            case GUIDE:
                guideCardView.setVisibility(View.VISIBLE);
                contentCardView.setVisibility(View.GONE);
                datalessCardView.setVisibility(View.GONE);

                buttonReadArticle.setVisibility(View.INVISIBLE);
                timelineRow.setVisibility(View.VISIBLE);
                break;
            case CONTENT:
                contentCardView.setVisibility(View.VISIBLE);
                guideCardView.setVisibility(View.GONE);
                datalessCardView.setVisibility(View.GONE);

                buttonReadArticle.setVisibility(View.VISIBLE);
                timelineRow.setVisibility(View.VISIBLE);
                break;
            case DATALESS:
                datalessCardView.setVisibility(View.VISIBLE);
                contentCardView.setVisibility(View.GONE);
                guideCardView.setVisibility(View.GONE);

                buttonReadArticle.setVisibility(View.INVISIBLE);
                timelineRow.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void getInformationContent() {
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext(), new DatabaseHandler.AsyncResponse() {
            @Override
            public void processFinished(ArrayList<DatabaseHandler.VideoTriggerPoint> output) {
                if (output != null) {
                    for (DatabaseHandler.VideoTriggerPoint currItem : output) {
                        // adding new item filled with retrieved database content (wikipedia-content will be loaded later on)
                        contentElements.add(new ContentElement(currItem.triggerPointTitle, "", "", true, false, "", currItem.triggerPointImageUrl, currItem.triggerPointTimestamp, currItem.triggerPointBitmap));
                    }
                    getContentInformationFromWiki();
                } else {
                    // no data could be retrieved from database, show specific guide note
                    activateInformationState(InformationViewState.DATALESS);
                    progressBar.setVisibility(View.GONE);       // deactivating progress-loader
                }
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

        // registering video-progress-bar behaviour
        videoProgress.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            int originalProgress;
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Nothing here..
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                originalProgress = seekBar.getProgress();
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int arg1, boolean fromUser) {
                if( fromUser == true){
                    seekBar.setProgress( originalProgress);
                }
            }
        });

        // registering button and button-behaviour by on-clicking
        quizStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                //marker must be set inactive if no ad is shown, after ad started and the
                //quiz is being closed, the marker must be set active so the user can start
                //the quiz again and continue the quiz
                Intent intent = new Intent(context, QuizActivity.class);
                intent.putExtra("videoProgress", currentVideoProgress);
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

    public void checkQuizActivityState() {
        if(app.getQuizAvailalibity()){
            quizStartButton.setVisibility(View.VISIBLE);
        } else {
            quizStartButton.setVisibility(View.INVISIBLE);
        }
    }

    // custom adapter for binding list view data
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        public List<CircleImageView> triggerPointList = new ArrayList<>();      // list of registered trigger-points for handling highlighting
        private List<Integer> prevPositions = new ArrayList<>();                // list for handling trigger-point registration
        public int prevPosition;                                                // previous item for setting back highlighting
        public int activatedPosition;                                           // if trigger-point-item is activated (colored highlighting)

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
                }
            }
            // setting image for current timeline-item
            if (contentElements.get(position).isPlaceholder == true) {
                viewHolder.timelineItem.setImageResource(R.drawable.placeholder);
            } else {
                viewHolder.timelineItem.setImageBitmap(contentElements.get(position).imageBitmap);
            }

            // handling on-click behaviour (loading information and setting highlighting for timeline-item)
            viewHolder.timelineItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // only clicking if item was revealed (no placeholder left)
                    if (contentElements.get(position).isPlaceholder == false) {
                        onItemClicked(position);
                    }
                }
            });

            // setting divider invisible for last trigger point item
            if (position == contentElements.size()-1) {
                viewHolder.timelineItemDivider.setVisibility(View.INVISIBLE);
            }
        }

        private void onItemClicked(int position) {
            // setting activated position for highlighting
            activatedPosition = position;

            // loading information data for specific, clicked trigger point
            integrateInformation(contentElements.get(position).title, contentElements.get(position).extract, contentElements.get(position).imageBitmap);

            // sending message to first screen (for synchronously activating trigger-point)
            sRClient.sendMsg("activateItem" + position);

            // setting highlighting for current clicked trigger point
            setItemActivationState(position);
            prevPosition = position;

            setNavigationArrowVisibility(position);
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

        private void setNavigationArrowVisibility(int position) {
            // activating/deactivating timeline arrows (on border reached) for emphasizing possible scroll-directions
            int placeholderCount = getItemPlaceholderCount();
            if (placeholderCount == getItemCount()) {
                // checking if all placeholders are visible
                arrowLeft.setVisibility(View.INVISIBLE);
                arrowRight.setVisibility(View.INVISIBLE);
            } else if (placeholderCount < getItemCount()) {
                if (placeholderCount == getItemCount()-1) {
                    // checking if single placeholder was revealed
                    arrowLeft.setVisibility(View.INVISIBLE);
                    arrowRight.setVisibility(View.INVISIBLE);
                } else if (placeholderCount < getItemCount()-1) {
                    if (position == (getItemCount()-placeholderCount)-1) {
                        // checking if more than 1 placeholder was revealed and last available position was clicked
                        arrowLeft.setVisibility(View.VISIBLE);
                        arrowRight.setVisibility(View.INVISIBLE);
                    } else if (position == 0) {
                        // checking if more than 1 placeholder was revealed and first available position was clicked
                        arrowLeft.setVisibility(View.INVISIBLE);
                        arrowRight.setVisibility(View.VISIBLE);
                    } else {
                        // checking if more than 1 placeholder was revealed and any other available position was clicked
                        arrowLeft.setVisibility(View.VISIBLE);
                        arrowRight.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        private int getItemPlaceholderCount() {
            int placeholderCounter = 0;
            for (int i = 0; i < getItemCount(); i++) {
                if (contentElements.get(i).isPlaceholder == true) {
                    placeholderCounter++;
                }
            }
            return placeholderCounter;
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
            setTriggerPoint(switchToTriggerPointPosition, direction);
        }

        private void setTriggerPoint(int position, int direction) {
            // checking if trigger point id is in range
            if ((position >= 0) && (position <= getItemCount()-1)) {
                // scrolling timeline automatically
                if (direction != 0) {
                    recyclerListView.smoothScrollBy(direction * 200, 0);
                    recyclerListView.smoothScrollToPosition(position);
                }

                // calling on-click for previous/next timeline-item on arrow-clicked
                triggerPointList.get(position).callOnClick();
            }
        }

        private void revealNewTriggerPoint(int position) {
            // activating new trigger-point for showing real animal-picture
            if ((position >= 0) && (position <= getItemCount()-1)) {
                contentElements.get(position).isPlaceholder = false;
                triggerPointList.get(position).setImageBitmap(contentElements.get(position).imageBitmap);
                setNavigationArrowVisibility(activatedPosition);

                if (position == 0) {
                    activateInformationState(InformationViewState.CONTENT);
                    setTriggerPoint(position, 0);
                }
            }
        }
    }

    // defining content element object for storing specific data (for each trigger/content point)
    class ContentElement {
        String identifier;
        String title;
        String extract;
        Boolean isPlaceholder;
        Boolean isActive;
        String extArticle;
        String imageUrl;
        String timestamp;
        Bitmap imageBitmap;

        // constructor
        public ContentElement(String identifier, String title, String extract, Boolean isPlaceholder, Boolean isActive, String extArticle, String imageUrl, String timestamp, Bitmap imageBitmap) {
            this.identifier = identifier;
            this.title = title;
            this.extract = extract;
            this.isPlaceholder = isPlaceholder;
            this.isActive = isActive;
            this.extArticle = extArticle;
            this.imageUrl = imageUrl;
            this.timestamp = timestamp;
            this.imageBitmap = imageBitmap;
        }
    }
}
