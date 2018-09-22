package com.wildLive.secondScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class DatabaseHandler extends AsyncTask<String, Void, Void> {

    private StorageReference storageReference = null;                           // firebase storage reference for cloud image storage
    private DatabaseReference databaseReference = null;                         // firebase database reference for information storage
    private Context mainContext;                                                // context from calling activity to convert default-drawable when image could not be retrieved
    public DatabaseHandler.AsyncResponse responseHandler = null;                // interface for response data-usage in calling activity
    private DataCategory dataCategory;                                          // category (video vs quiz) for handling correct database structure

    public ArrayList<VideoTriggerPoint> itemsFromDatabase = new ArrayList<>();  // list for returning retrieved data
    private int currItem;                                                       // for getting current item from list


    // constructor for getting application-context and registering interface for done process
    public DatabaseHandler(Context mainContext, DatabaseHandler.AsyncResponse respHandler) {
        this.mainContext = mainContext;
        this.responseHandler = respHandler;
    }
    public void setDatabaseReference(DatabaseReference databaseReference) { this.databaseReference = databaseReference; }
    public void setStorageReference(StorageReference storageReference) { this.storageReference = storageReference; }

    // interface to register for calling activity (when process is done)
    public interface AsyncResponse {
        void processFinished(ArrayList<VideoTriggerPoint> output);
    }
    // enum for choosing category (retrieving different structured data)
    public enum DataCategory { VIDEO, QUIZ }
    public void setDataCategory(DataCategory dataCategory) { this.dataCategory = dataCategory; }


    @Override
    protected Void doInBackground(String... categories) {
        // choosing between different categories (each category has its own database structure due to different use)
        switch (dataCategory) {
            case VIDEO:
                if (databaseReference != null) {
                    // retrieving video information: video information (timestamps, etc.) and storage images afterwards
                    getVideoDataFromFirebaseDatabase();
                }
                break;
            case QUIZ:
                return null;
            default:
                return null;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        // function is called when database-processes are truly finished
        responseHandler.processFinished(itemsFromDatabase);
    }

    // retrieving video data from database (id, image-url, image-format, timestamp, title as wikipedia-identifier)
    private void getVideoDataFromFirebaseDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // checking if data for given database-ref is existent
                if (dataSnapshot.getChildrenCount() != 0) {

                    // getting all stored firebase items at given database-reference (stored in snapshot)
                    for (DataSnapshot currentSnap : dataSnapshot.getChildren()) {
                        int currentId = Integer.parseInt(currentSnap.getKey().toString());
                        String imageUrl = currentSnap.child("image").getValue().toString();
                        String imageFile = currentSnap.child("imageFile").getValue().toString();
                        String timestamp = currentSnap.child("timestamp").getValue().toString();
                        String title = currentSnap.child("title").getValue().toString();

                        // forming objects (VideoTriggerPoint) for each retrieval
                        VideoTriggerPoint triggerPoint = new VideoTriggerPoint(currentId, imageUrl, imageFile, timestamp, title, null);
                        itemsFromDatabase.add(triggerPoint);

                        // if last database item: now trying to receive images from firebase storage (via url)
                        if (currentId == dataSnapshot.getChildrenCount() - 1) {
                            if (storageReference != null) {
                                getImageDataFromFirebaseStorage();
                            } else {
                                publishProgress();  // finishing async task
                            }
                        }
                    }
                } else {
                    // given database-ref (for video-id) does not exist
                    itemsFromDatabase = null;
                    publishProgress();  // finishing async task
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //itemsFromDatabase.add(null);
                itemsFromDatabase = null;
                publishProgress();  // finishing async task
            }
        });
    }

    // retrieving final image data from storage (converting bytes into bitmap for showing images in ui)
    private void getImageDataFromFirebaseStorage() {

        // getting images from storage for each stored trigger point
        for (int i = 0; i < itemsFromDatabase.size(); i++) {
            currItem = i;

            // getting bytes for each storage-reference-child (identifiable by "imageFile")
            storageReference.child(itemsFromDatabase.get(i).triggerPointImageFile).getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                int currIdx = currItem;

                @Override
                public void onSuccess(byte[] imageBytes) {
                    // decoding retrieved image bytes to bitmap (for showing image in ui)
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    itemsFromDatabase.get(currIdx).triggerPointBitmap = bitmap;

                    // checking if all images were retrieved
                    if (currIdx == itemsFromDatabase.size() - 1) {
                        publishProgress();  // finishing async task
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                int currIdx = currItem;

                @Override
                public void onFailure(@NonNull Exception e) {
                    // handling if image could not be received (incorrect requesting-data)
                    // https://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
                    Bitmap bitmap = BitmapFactory.decodeResource(mainContext.getResources(), R.drawable.wildlivefox);
                    itemsFromDatabase.get(currIdx).triggerPointBitmap = bitmap;

                    // checking if all images were retrieved
                    if (currIdx == itemsFromDatabase.size() - 1) {
                        publishProgress();  // finishing async task
                    }
                }
            });
        }
    }

    // video-data-object for grouping each database-video-item
    public class VideoTriggerPoint {
        int triggerPointId;
        String triggerPointImageUrl;
        String triggerPointImageFile;
        String triggerPointTimestamp;
        String triggerPointTitle;
        Bitmap triggerPointBitmap;

        public VideoTriggerPoint(int id, String imageUrl, String imageFile, String timestamp, String title, Bitmap bitmap) {
            this.triggerPointId = id;
            this.triggerPointImageUrl = imageUrl;
            this.triggerPointImageFile = imageFile;
            this.triggerPointTimestamp = timestamp;
            this.triggerPointTitle = title;
            this.triggerPointBitmap = bitmap;
        }
    }
}
