package com.wildLive.secondScreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;


// Source from Aws Rh "How to create a slider using ViewPager"
// https://www.youtube.com/watch?v=R_AIUy7tFVA

public class SlideAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;

    //list of images
    public int[] lst_images = {
            R.drawable.img_onboarding1,
            R.drawable.img_onboarding2,
            R.drawable.img_onboarding3,
            R.drawable.img_onboarding4,
            R.drawable.img_onboarding5,
            R.drawable.img_onboarding6
    };

    //list of titles
    public String [] lst_title = {
            "Willkommen",
            "Verbinden",
            "Video Playlist",
            "Informationen",
            "Quiz",
            "Los geht's!"
    };

    //list of descriptions
    public String [] lst_description = {
            "zu Wildlive, unserer interaktiven SecondScreen Anwendung. \n" +
                    "Die folgende Einleitung bietet dir einen Überblick unserer App und ihrer Benutzbarkeit. Viel Spaß!\n",
            "Für die Kommunikation zwischen Smartphone und Laptop wird Internet benötigt. \n" + "Achte daher bitte darauf, WLAN oder Mobile Daten einzuschalten.\n",
            "In der Video Playlist findest du eine Auswahl an tollen Tierdokumentationen, die nach Kontinenten gefiltert sind.\n" + "Wenn dir ein Video gefällt, kannst du es bei Auswahl auf deinem Laptop ansehen.\n",
            "Zusätzlich hast du die Möglichkeit, während dem Video interessante Fakten und Informationen zu lesen,\n"+ "die du gerne auf Wikipedia mit einem Klick weiterverfolgen kannst.\n",
            "Nerven dich Werbeeinblendungen auch? Wir sorgen dafür, dass du während einer Werbepause dein Wissen über Tiere erweitern\n"+ "und in einem spannenden Quiz testen kannst! Sammle Punkte mit jeder richtigen Antwort!\n",
            "Viel Spaß mit unserer App und beim Entdecken der fantastischen Tierwelt!"
    };


    public SlideAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return lst_title.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view==(LinearLayout)o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide, container,false);

        LinearLayout layoutslide = view.findViewById(R.id.slidelinearlayout);
        ImageView imgslide = (ImageView) view.findViewById(R.id.slideimg);
        TextView textTitle = (TextView) view.findViewById(R.id.txttitle);
        TextView description = (TextView) view.findViewById(R.id.txtdescription);

        layoutslide.setBackgroundColor(Color.WHITE);
        imgslide.setImageResource(lst_images[position]);
        textTitle.setText(lst_title[position]);
        description.setText(lst_description[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }

}
