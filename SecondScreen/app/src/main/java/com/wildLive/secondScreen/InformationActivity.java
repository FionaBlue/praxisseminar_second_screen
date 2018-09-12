package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
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

    private StorageReference firebaseImageStorage;                      // firebase reference for cloud storage (for images)

    private String videoId = "";
    private SignalRClient sRClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) for information activity
        super.onCreate(savedInstanceState);

        // setting layout components after all information was loaded (especially for adapter list view!)
        registerLayoutComponents();

        // registering image-database instance
        firebaseImageStorage = FirebaseStorage.getInstance().getReference();

        // registering array with initial content information
        registerContentInformation();

        // getting all content information from wiki api
        getContentInformationFromWiki();

        // registering ui component listener
        addListenersOnUiComponents();

        WildLive app = (WildLive)getApplication();
        sRClient = app.getSRClient();

        //getting selected VideoID from VideoBibActivity
        Bundle extras = getIntent().getExtras();
        videoId = extras.getString("videoID");
        System.out.println("VideoID through intent: " + videoId);
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
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

        // registering list view (recyclerView) with arrows/chevrons and custom array adapter
        arrowLeft = (ImageView) findViewById(R.id.timelineChevronLeft);
        arrowRight = (ImageView) findViewById(R.id.timelineChevronRight);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);   // setting orientation to horizontal!
        recyclerListView = (RecyclerView) findViewById(R.id.timeline);
        recyclerListView.setLayoutManager(layoutManager);
        recyclerListView.setAdapter(adapter);   // binding data from separate timeline-item layout to activity layout via adapter
    }

    public void registerContentInformation() {
        // appending all initial content information (here temporary defining information)
        contentElements.add(new ContentElement("Kaiserpinguin","", "", true, R.drawable.wiki_kaiserpinguin, R.drawable.wiki_kaiserpinguin, ""));
        contentElements.add(new ContentElement("Delfine", "","", false, R.drawable.wiki_delphin, R.drawable.wiki_delphin, ""));
        contentElements.add(new ContentElement("Wanderameisen", "","", false, R.drawable.wiki_wanderameisen, R.drawable.wiki_wanderameisen, ""));
        contentElements.add(new ContentElement("Wanderfalke", "","", false, R.drawable.wiki_wanderfalke, R.drawable.wiki_wanderfalke, ""));
        contentElements.add(new ContentElement("Monarchfalter", "", "", false, R.drawable.wiki_monarchfalter, R.drawable.wiki_monarchfalter, ""));
        contentElements.add(new ContentElement("Zebra", "", "", false, R.drawable.wiki_zebra, R.drawable.wiki_zebra, ""));
        contentElements.add(new ContentElement("Krokodile", "", "", false, R.drawable.wiki_krokodile, R.drawable.wiki_krokodile, ""));
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
                        integrateInformation(wikiElement.title, wikiElement.extract, wikiElement.wikiImage);
                    }

                    // checking if last content element information was retrieved, than stopping progress bar and refresh adapter
                    if (wikiElement == contentElements.get(contentElements.size() - 1)) {
                        adapter.triggerPointList.clear();           // clearing list of trigger points for rearranging
                        adapter.notifyDataSetChanged();             // updating recycler-view-data (else nothing will show up after progress-bar loaded)
                        progressBar.setVisibility(View.GONE);       // deactivating progress-loader
                    }
                }
            }).execute(wikiElement.identifier);
        }
    }

    public void integrateInformation(String wikiTitle, String wikiExtract, int wikiImage) {
        // loading responded information in card view elements
        wikiContentTitle.setText(wikiTitle);
        wikiContentExtract.setText(wikiExtract);
        // loading responded image in image view (temporary: local image)
        wikiContentImage.setImageResource(wikiImage);
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
            public void onClick(View arg0) {
                // switching to previous timeline item
                adapter.switchTriggerPoint(-1);
            }
        });
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // switching to next timeline item
                adapter.switchTriggerPoint(+1);
            }
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
    }

    // custom adapter for binding list view data
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        public List<CircleImageView> triggerPointList = new ArrayList<>();

        @Override
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
        public class ViewHolder extends RecyclerView.ViewHolder { //implements View.OnClickListener {
            public CircleImageView timelineItem;
            public View timelineItemDivider;

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
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final int position = i;

            // adding each trigger point (one ViewHolder holds one trigger point) to list for further processing
            triggerPointList.add(viewHolder.timelineItem);

            // setting first trigger point highlighting and invisible left-arrow
            if (triggerPointList.size() == 1) {
                setItemActivationState(0);
                arrowLeft.setVisibility(View.INVISIBLE);
            }

            // all widgets and data will be attached to each individual list view item
            triggerPointList.get(i).setImageResource(contentElements.get(i).timelineTriggerImage);

            // registering onclick-listener for content switching
            triggerPointList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // loading information data for specific, clicked trigger point
                    integrateInformation(contentElements.get(position).title, contentElements.get(position).extract, contentElements.get(position).wikiImage);

                    // setting highlighting for current clicked trigger point
                    setItemActivationState(position);

                    // activating/deactivating timeline arrows (on border reached)
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
            });

            // setting divider invisible for last trigger point item
            if (position == contentElements.size()-1) {
                viewHolder.timelineItemDivider.setVisibility(View.INVISIBLE);
            }
        }
        private void setItemActivationState(int position) {
            // scanning timeline-item-list and setting back activation state and highlighting of previously activated
            for (int i = 0; i < getItemCount(); i++) {
                contentElements.get(i).isActive = false;
            }
            for (int j = 0; j < triggerPointList.size(); j++) {
                if (triggerPointList.get(j) != null) {
                    setTriggerPointSaturation(triggerPointList.get(j), 0);
                }
            }
            // setting activation state of current trigger point
            contentElements.get(position).isActive = true;
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
            int switchedToTriggerPoint = getItemActivationState() + direction;

            // checking if previous or next trigger point is in range
            if ((switchedToTriggerPoint >= 0) && (switchedToTriggerPoint <= getItemCount()-1)) {
                recyclerListView.smoothScrollBy(direction*200, 0);
                recyclerListView.smoothScrollToPosition(switchedToTriggerPoint);
                triggerPointList.get(switchedToTriggerPoint).callOnClick();
            }
        }
    }

    // defining content element object for storing specific data (for each trigger/content point)
    class ContentElement {
        String identifier;
        String title;
        String extract;
        Boolean isActive;
        int timelineTriggerImage;    // path to temporary locally stored timeline-point-image
        int wikiImage;               // path to temporary locally stored wiki image
        String extArticle;

        // constructor
        public ContentElement(String identifier, String title, String extract, Boolean isActive, int timelineTriggerImage, int wikiImage, String extArticle) {
            this.identifier = identifier;
            this.title = title;
            this.extract = extract;
            this.isActive = isActive;
            this.timelineTriggerImage = timelineTriggerImage;
            this.wikiImage = wikiImage;
            this.extArticle = extArticle;
        }
    }
}
