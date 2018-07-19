package com.wildLive.secondScreen;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class WikiRequestHandler extends AsyncTask<String, Void, String> {

    // src:
    // ********
    // https://stackoverflow.com/questions/1625162/get-text-content-from-mediawiki-page-via-api
    // https://developer.android.com/reference/java/net/HttpURLConnection
    // https://developer.android.com/reference/java/io/BufferedReader
    // https://developer.android.com/reference/android/os/AsyncTask
    // https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a

    private Exception e;
    public AsyncResponse responseHandler = null;                    // global interface (for correct response-data usage in information activity)

    // constructor
    public WikiRequestHandler(AsyncResponse respHandler) {
        this.responseHandler = respHandler;
    }

    // registering interface
    public interface AsyncResponse {
        void processFinished(String output);
    }

    @Override
    protected String doInBackground(String... params) {

        // url-parameters for http-requesting
        String endpoint = "https://de.wikipedia.org/w/api.php";     // homepage for mediawiki web service api
        String format = "format=json";                              // returning data in json format
        String action = "action=query";                             // query for getting information
        String properties = "prop=extracts";                        // prop=property1|property2|propertyN
        String firstContent = "exintro=";                           // returning only content before the first section (not full data)
        String plainText = "explaintext=";                          // returning extract as plain text (instead of html)
        String title = "titles=";                                   // title for search term

        // example of full url "https://de.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles=Kaiserpinguin"
        String wikiApiJsonUrl = endpoint + "?" + format + "&" + action + "&" + properties + "&" + firstContent + "&" + plainText + "&" + title + params[0];

        try {
            // setting up predefined http url connection
            HttpURLConnection currentUrlConnection = (HttpURLConnection) new URL(wikiApiJsonUrl).openConnection();

            // getting content (text stream) from wiki-web-service-api via http-request for that url
            BufferedReader inputDataReader = new BufferedReader(new InputStreamReader(currentUrlConnection.getInputStream(), "ISO-8859-1"));

            // collecting and combining all lines of input stream
            String requestResponse = inputDataReader.lines().collect(Collectors.joining());

            // closing input stream after retrieving all information
            inputDataReader.close();

            try {
                // generating json object from string input stream (utf-8 decoding from html iso-8859-1)
                JSONObject json = new JSONObject(requestResponse);
                requestResponse = json.toString();
            } catch (Exception e) {}

            return requestResponse;

        } catch (Exception e) {
            this.e = e;
            return null;
        }
    }

    protected void onPostExecute(String result) {
        // calling defined interface in information activity
        responseHandler.processFinished(result);
    }
}
