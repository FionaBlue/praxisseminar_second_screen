package com.wildLive.secondScreen;

import android.app.Application;

// WildLive Application is primary for setting and getting the initialised SignalR Client Instance
public class WildLive extends Application {

    private SignalRClient srClient;
    private Boolean quizAvailability = false;

    // setter for initialised SignalR Client Instance
    public void setSRClient(SignalRClient srClient){
        this.srClient = srClient;
    }

    // getter for initialised SignalR Client Instance
    public SignalRClient getSRClient() {
        return this.srClient;
    }

    // setter for quiz availability during advertisements in first screen
    public void setQuizAvailability(Boolean availability){
        quizAvailability = availability;
    }

    // getter for quiz availability during advertisements in first screen
    public Boolean getQuizAvailalibity() {
        return quizAvailability;
    }
}
