package com.wildLive.secondScreen;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

public class CategoryColorHandler {

    // sets the continent-matching color
    // for not defined categories there is a default color for flexibility
    public int getContinentColor(String continent, Context context) {
        int continentColor;
        switch(continent) {

            case "Arktis":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorArktis)));
                break;
            case "Antarktis":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorAntarktis)));
                break;
            case "Afrika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorAfrika)));
                break;
            case "Australien":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorAustralien)));
                break;
            case "Südamerika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorSüdamerika)));
                break;
            case "Nordamerika":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorNordamerika)));
                break;
            case "Europa":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorEuropa)));
                break;
            case "Asien":
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorAsien)));
                break;

            default:
                continentColor = Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.colorMainBlue)));
                break;
        }
        return continentColor;
    }
}
