package com.wildLive.secondScreen;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;


// src: https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
//      https://stackoverflow.com/questions/11411395/how-to-get-current-foreground-activity-context-in-android/28423385#28423385
//      https://stackoverflow.com/questions/15874117/how-to-set-delay-in-android
public class InternetConnectionHandler {

    public static Boolean connectionToInternet = false;

    public static void setConnectionToInternet(Boolean connection){
        connectionToInternet = connection;
    }

    public Boolean getConnectionToInternet(){
        return connectionToInternet;
    }

    public static Activity getActivity() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Object activityThread = null;
        try {
            activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Field activitiesField = null;
        try {
            activitiesField = activityThreadClass.getDeclaredField("mActivities");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        activitiesField.setAccessible(true);

        Map<Object, Object> activities = null;
        try {
            activities = (Map<Object, Object>) activitiesField.get(activityThread);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (activities == null)
            return null;

        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = null;
            try {
                pausedField = activityRecordClass.getDeclaredField("paused");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            pausedField.setAccessible(true);
            try {
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


        static class getInternetConnectionState extends AsyncTask<Void, Void, Boolean> {

            private Activity currentActivity;

            @Override
            protected Boolean doInBackground(Void... voids) {
                currentActivity = getActivity();
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
                    socket.close();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                setConnectionToInternet(result);
                if (result == false && currentActivity != null) {
                    Intent intent = new Intent(currentActivity, AlertActivity.class);
                    currentActivity.startActivity(intent);
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new getInternetConnectionState().execute();
                    }
                }, 3000);
            }
        }
}
