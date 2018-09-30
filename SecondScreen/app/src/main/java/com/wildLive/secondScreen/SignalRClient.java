package com.wildLive.secondScreen;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.LongPollingTransport;

public class SignalRClient {

    // src:
    // ********
    // https://msdn.microsoft.com/de-de/library/microsoft.aspnet.signalr.client.hubs.hubconnection(v=vs.100).aspx
    // https://stackoverflow.com/questions/23375043/best-practice-for-reconnecting-signalr-2-0-net-client-to-server-hub
    // https://github.com/SignalR/java-client/issues/61
    // https://stackoverflow.com/questions/25817303/how-to-put-delay-in-android-async-task

    private static final String TAG = "MEK_Plugin_SignalR";                     // tags for logging

    //SignalR Variables
    private String host = "http://pk029-audi-2nds.tvapp-server.de/SecondScreen";// example server
    private static HubConnection _connection;
    private static HubProxy _hub = null;
    private static SubscriptionHandler1<String> handlerCon;
    private static SignalRFuture<Void> _awaitConnection;
    private static String hubName = "secondScreenHub";                          // name from hub class in server
    private static String sessionID = "1234567890";                             // id for connecting devices
    boolean isReconnecting = false;                                             // status for checking connection state (and reacting if is "reconnecting" error)
    public Boolean isConnectedToFS = false;                                     // status for checking internet connection state
    private int connectionFalseDelay = 0;                                       // delay for setting First Screen connection to false

    // constructor
    public SignalRClient(){
        // establishing signalR connection via hub
        _connection = new HubConnection( host );
        _hub = _connection.createHubProxy( hubName );                           // name from hub class in server

        // starting hub connection
        startConnection();
        disconnect();

        // starting session and generating session-id
        _hub.invoke(String.class, "StartSession").done(new Action<String>() {
            @Override
            public void run(String SessionID) {
                Log.d(TAG, "session id  " + sessionID);
                try {
                    _hub.invoke("JoinSession", sessionID).get();

                    // calling asyncTask for checking if permanent connection status fires "reconnecting" error
                    new ReconnectHandler().execute();
                    new checkCastConnection().execute();

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // interface for handling message-receiving
    public interface SignalRCallback<T> {
        void onSuccess(T message);
        void onError(T message);
    }

    // asyncTask behaviour for instantiating new signalR client on reconnecting delay/error
    public class ReconnectHandler extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // checking for state as long as not reconnecting (error)
            if (isReconnecting == false) {
                if (_connection.getState().toString() == "Reconnecting") {
                    isReconnecting = true;
                } else {
                    isReconnecting = false;
                }
            }
            return null;
        }
        protected void onPostExecute(Void voids) {
            sendMsg("secondScreenConnected"); //send castConnected Msg as "ping" and for CastButton indication
            new ReconnectHandler().execute();
        }
    }

    // sending messages from android device (if connection status is not "reconnecting")
    public void sendMsg(String messageData)  {
        if (isReconnecting == false) {
            if (_hub != null && sessionID != null) {
                try {
                    _hub.invoke("sendMessage", sessionID, messageData).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // receiving messages (if connection status is not "reconnecting")
    public void setMessageListener(final SignalRCallback<String> messageCallback){
        handlerCon = new SubscriptionHandler1<String>() {
            @Override
            public void run(String receivedMessage) {
                // return all received messages (on success)
                if (isReconnecting == false) {
                    messageCallback.onSuccess(receivedMessage);
                    messageCallback.onError(receivedMessage);
                }
            }
        };
        if (isReconnecting == false) {
            _hub.on("receiveMessage",handlerCon,String.class);
        }
    }

    // disconnecting hub connection
    public void disconnect() {
        try {
            _hub.invoke("LeaveSession", sessionID).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // starting signalR connection
    private void startConnection(){
        _awaitConnection = _connection.start(new LongPollingTransport(_connection.getLogger()));
        try {
            _awaitConnection.get(4000, TimeUnit.MILLISECONDS);
            Log.d(TAG,"Connection done");
        } catch (InterruptedException e) {
            Log.d(TAG,"SignalR StartConnection Disconnect . . ." + e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG,"SignalR StartConnection Error . . ." + e);
            e.printStackTrace();
        } catch(Exception e){
            Log.d(TAG,"SignalR StartConnection Exception " + e);
            e.printStackTrace();
        }
    }

    // invalidates the OptionsMenu of current activity to check if First Screen is connected
    // via isConnectedToFS == true -> updated in message listeners of all Activities after MainActivity
    public class checkCastConnection extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            final Activity currentActivity = InternetConnectionHandler.getActivity();
            if(currentActivity != null) {
                currentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentActivity.invalidateOptionsMenu();
                    }
                });
            }
            // falseDelay waits for message listeners to maybe update isConnectedToFS to true
            if(connectionFalseDelay < 3){
                connectionFalseDelay ++;
            } else {
                connectionFalseDelay = 0;
                // sleepDelay waits for invalidateOptionsMenu to conduct
                // setting isConnectedToFS to false would cause blinking of cast-button
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                isConnectedToFS = false;
            }
            return null;
        }

        // loops for checking First Screen connection persistently
        protected void onPostExecute(Void result){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new checkCastConnection().execute();
                }
            }, 3000);
        }
    }
}