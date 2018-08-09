package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InformationActivity extends AppCompatActivity {

    // src:
    // ********
    // https://www.codeproject.com/Articles/1152595/Android-Horizontal-ListView-Tutorial
    // https://www.youtube.com/watch?v=94rCjYxvzEE
    // https://stackoverflow.com/questions/26682277/how-do-i-get-the-position-selected-in-a-recyclerview

    public List<ContentElement> contentElements = new ArrayList<>();    // dynamical array for content information
    RecyclerView recyclerListView;                                      // providing option for horizontal list view (= recyclerView)
    CustomAdapter adapter = new CustomAdapter();                        // adapter to fill list view with trigger points (timeline)
    private TextView wikiContentTitle;                                  // text view for showing wiki-requested title content
    private TextView wikiContentExtract;                                // text view for showing wiki-requested extract content
    private ImageView wikiContentImage;                                 // image view for showing wiki-requested image content
    private Button buttonInformationNext;                               // button for switching to next activity (temporary!)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting view (xml-layout) for information activity
        super.onCreate(savedInstanceState);

        // setting layout components after all information was loaded (especially for adapter list view!)
        registerLayoutComponents();

        // registering array with initial content information
        registerContentInformation();

        // getting all content information from wiki api
        getContentInformationFromWiki();

        // registering button listener
        addListenerOnButton();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void registerLayoutComponents() {
        // registering layout and layout-components
        setContentView(R.layout.activity_information);
        wikiContentImage = (ImageView) findViewById(R.id.wikiImage);
        wikiContentTitle = (TextView) findViewById(R.id.currentWikiTitle);
        wikiContentExtract = (TextView) findViewById(R.id.currentWikiExtract);
        wikiContentExtract.setMovementMethod(new ScrollingMovementMethod());        // making extract content scrollable
        buttonInformationNext = (Button) findViewById(R.id.buttonInformationNext);

        // registering list view (recyclerView) with custom array adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);   // setting orientation to horizontal!
        recyclerListView = (RecyclerView) findViewById(R.id.timeline);
        recyclerListView.setLayoutManager(layoutManager);
        recyclerListView.setAdapter(adapter);   // binding data from separate timeline-item layout to activity layout via adapter
    }

    public void registerContentInformation() {
        // appending all initial content information (here temporary defining information)
        contentElements.add(new ContentElement("Kaiserpinguin","", "", true, R.drawable.wiki_kaiserpinguin, R.drawable.wiki_kaiserpinguin));
        contentElements.add(new ContentElement("Delfine", "","", false, R.drawable.wiki_delphin, R.drawable.wiki_delphin));
        contentElements.add(new ContentElement("Wanderameisen", "","", false, R.drawable.wiki_wanderameisen, R.drawable.wiki_wanderameisen));
        contentElements.add(new ContentElement("Wanderfalke", "","", false, R.drawable.wiki_wanderfalke, R.drawable.wiki_wanderfalke));
        contentElements.add(new ContentElement("Zebra", "", "", false, R.drawable.wiki_zebra, R.drawable.wiki_zebra));
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

                    // loading responded information and local image in card view elements
                    if (wikiElement.isActive == true) {
                        integrateInformation(wikiElement.title, wikiElement.extract, wikiElement.wikiImage);
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

    public void addListenerOnButton() {
        final Context context = this;
        // registering button and button-behaviour by on-clicking
        buttonInformationNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // switching to next activity on button click
                Intent intent = new Intent(context, QuizActivity.class);
                startActivity(intent);
            }
        });
    }

    // custom adapter for binding list view data
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // binding the layout file - each individual layout will be inflated/filled
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.timeline_item, viewGroup, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final int position = i;
            // all widgets and data will be attached to each individual list view item
            viewHolder.timelineItem.setImageResource(contentElements.get(i).timelineTriggerImage);
            // registering onclick-listener for content switching
            viewHolder.timelineItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    integrateInformation(contentElements.get(position).title, contentElements.get(position).extract, contentElements.get(position).wikiImage);
                }
            });
        }
        @Override
        public int getItemCount() {
            return contentElements.size();
        }
        // view holder for performance issues with recyclerView (only visible items are handled)
        public class ViewHolder extends RecyclerView.ViewHolder { //implements View.OnClickListener {
            public CircleImageView timelineItem;
            public ViewHolder(@NonNull final View itemView) {
                super(itemView);
                timelineItem = itemView.findViewById(R.id.timelineItem);
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

        // constructor
        public ContentElement(String identifier, String title, String extract, Boolean isActive, int timelineTriggerImage, int wikiImage) {
            this.identifier = identifier;
            this.title = title;
            this.extract = extract;
            this.isActive = isActive;
            this.timelineTriggerImage = timelineTriggerImage;
            this.wikiImage = wikiImage;
        }
    }
}
