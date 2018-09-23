package com.wildLive.secondScreen;

import android.app.Application;

public class WildLive extends Application {
    private SignalRClient srClient;
    private Boolean quizAvailability = false;

    public void setSRClient(SignalRClient srClient){
        System.out.println("WildLive " + srClient);
        this.srClient = srClient;
        System.out.println("WildLive this " + this.srClient);
    }

    public SignalRClient getSRClient() {
        System.out.println("WildLive getter " + this.srClient);
        return this.srClient;
    }

    public void setQuizAvailability(Boolean availability){
        quizAvailability = availability;
    }

    public Boolean getQuizAvailalibity() {
        return quizAvailability;
    }
}
