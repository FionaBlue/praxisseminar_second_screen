package com.wildLive.secondScreen;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.stream.Collectors;

public class WikiRequestHandler extends AsyncTask<String, Void, Object> {

    // src:
    // ********
    // https://stackoverflow.com/questions/1625162/get-text-content-from-mediawiki-page-via-api
    // https://developer.android.com/reference/java/net/HttpURLConnection
    // https://developer.android.com/reference/java/io/BufferedReader
    // https://developer.android.com/reference/android/os/AsyncTask
    // https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
    // https://stackoverflow.com/questions/9151619/how-to-iterate-over-a-jsonobject

    private Exception e;
    public AsyncResponse responseHandler = null;                    // global interface (for correct response-data usage in information activity)

    // constructor
    public WikiRequestHandler(AsyncResponse respHandler) {
        this.responseHandler = respHandler;
    }

    // registering interface
    public interface AsyncResponse {
        void processFinished(Object output);
    }

    @Override
    protected Object doInBackground(String... params) {

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

            // defining elements (by keys) to extract from text stream
            String titleString = "";
            String extractString = "";

            try {
                // generating json object from string input stream (utf-8 decoding from html iso-8859-1)
                JSONObject wikiJson = new JSONObject(requestResponse);

                // scanning json document structure for specific elements
                titleString = parseJsonStructure(wikiJson, "title");
                extractString = parseJsonStructure(wikiJson, "extract");
            } catch (Exception e) {}

            // forming string to view for user in information activity
            WikiContentElements wikiContent = new WikiContentElements();
            wikiContent.wikiContentTitle = titleString;
            wikiContent.wikiContentExtract = extractString;
            wikiContent.wikiContentImage = null;
            wikiContent.wikiContentArticle = "https://de.wikipedia.org/wiki/" + titleString;

            return wikiContent;

        } catch (Exception e) {
            this.e = e;
            return null;
        }
    }

    // packing object for sending all compressed content information back to information activity
    public class WikiContentElements {
        String wikiContentTitle;
        String wikiContentExtract;
        Drawable wikiContentImage;
        String wikiContentArticle;
    }

    private String parseJsonStructure(JSONObject json, String searchTerm) {
        String searchTermResult = "Der Begriff konnte nicht gefunden werden.";

        // getting key values of wiki-json on specific level (e.g. level#1 = batchcomplete, query; level#2 = pages; ...)
        Iterator<?> levelJsonNodes = json.keys();

        // iterating through specific json object level
        while (levelJsonNodes.hasNext()) {
            String jsonNodeKey = (String) levelJsonNodes.next();

            // checking if actual json-node-key equals searched term
            if (jsonNodeKey.equals(searchTerm)) {
                try {
                    // getting value of json object by key
                    searchTermResult = json.get(jsonNodeKey).toString();
                } catch (JSONException e) { throw new RuntimeException(e); }
            }

            try {
                // extracting the unhandled rest of the json-object
                if (json.get(jsonNodeKey) instanceof JSONObject) {
                    // iterating through other unhandled levels of json-structure to search for search term
                    return parseJsonStructure((JSONObject) json.get(jsonNodeKey), searchTerm);
                }
            } catch (JSONException e) { throw new RuntimeException(e); }
        }
        return searchTermResult;
    }

    protected void onPostExecute(Object result) {
        // calling defined interface in information activity
        responseHandler.processFinished(result);
    }
}
