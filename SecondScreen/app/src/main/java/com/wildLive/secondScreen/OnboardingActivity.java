package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class OnboardingActivity extends AppCompatActivity {

    private ViewPager viewpager;
    private SlideAdapter slideAdapter;
    LinearLayout onboardingLayout;
    ImageView indicator;
    TextView btnSkip;
    TextView btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_layout);
        getSupportActionBar().hide();
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        final Context context = this;
        btnSkip = (TextView) findViewById(R.id.intro_btn_skip);
        slideAdapter = new SlideAdapter(this);
        viewpager.setAdapter(slideAdapter);
        onboardingLayout = (LinearLayout)findViewById(R.id.onboardingLayout);

        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

            //Set indicator images and button texts according to the slides
            @Override
            public void onPageSelected(int position) {
                indicator = (ImageView) onboardingLayout.findViewById(R.id.imgIndicator);
                btnNext = (TextView) onboardingLayout.findViewById(R.id.intro_btn_next);

                if(position == 0){
                    indicator.setImageResource(R.drawable.ic_indicator1);
                    btnNext.setText("Next");
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            viewpager.setCurrentItem(1);
                        }
                    });
                }
                else if(position == 1){
                    indicator.setImageResource(R.drawable.ic_indicator2);
                    btnNext.setText("Next");
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            viewpager.setCurrentItem(2);
                        }
                    });
                }
                else if(position == 2){
                    indicator.setImageResource(R.drawable.ic_indicator3);
                    btnNext.setText("Next");
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            viewpager.setCurrentItem(3);
                        }
                    });
                }
                else if(position == 3){
                    indicator.setImageResource(R.drawable.ic_indicator4);
                    btnNext.setText("Next");
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            viewpager.setCurrentItem(4);
                        }
                    });
                }
                else if(position == 4){
                    indicator.setImageResource(R.drawable.ic_indicator5);
                    btnNext.setText("Next");
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            viewpager.setCurrentItem(5);
                        }
                    });
                }
                else if(position == 5){
                    indicator.setImageResource(R.drawable.ic_indicator6);
                    btnNext.setText("Start");

                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                        }
                    });

                }
            }
        };

        viewpager.addOnPageChangeListener(onPageChangeListener);

        //Skip Button calls Main Activity
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });


    }



}
