package com.sidegigapps.bedtimestories;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ryand on 2/28/2017.
 */

public class Utils {
    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration
     * @param totalDuration
     */
    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static void createNewFireBaseAlbum(Context context, String albumName) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = String.valueOf(Utils.getMaxAlbumKey(context)+1);

        Album newAlbum = new Album(albumName);
        Map<String, Object> albumValues = newAlbum.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/albums/" + key, albumValues);

        mDatabase.updateChildren(childUpdates);
    }

    public static void createNewFireBaseStory(Context context, String storyName, int duration_ms, String downloadURL) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        String key = String.valueOf(Utils.getMaxStoryKey(context)+1);

        Story newStory = new Story(key, storyName, duration_ms, downloadURL);
        Map<String, Object> albumValues = newStory.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/stories/" + key, albumValues);

        mDatabase.updateChildren(childUpdates);
    }

    public static int getMaxAlbumKey(Context context){
        SharedPreferences prefs = context.getSharedPreferences("maxKeys",Context.MODE_PRIVATE);
        return prefs.getInt("maxAlbumKey",0);
    }

    public static int getMaxStoryKey(Context context){
        SharedPreferences prefs = context.getSharedPreferences("maxKeys",Context.MODE_PRIVATE);
        return prefs.getInt("maxStoryKey",0);
    }

    private static void updateMaxAlbumKey(Context context, int maxValue) {
        SharedPreferences prefs = context.getSharedPreferences("maxKeys",Context.MODE_PRIVATE);
        prefs.edit().putInt("maxAlbumKey",maxValue).apply();
    }

    private static void updateMaxStoryKey(Context context, int maxValue) {
        SharedPreferences prefs = context.getSharedPreferences("maxKeys",Context.MODE_PRIVATE);
        prefs.edit().putInt("maxStoryKey",maxValue).apply();
    }

    public static void findNextAvailableStoryKey(final Context context, DatabaseReference reference){

        reference.orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                int maxValue = getMaxStoryKey(context);
                int value = Integer.parseInt(dataSnapshot.getKey());
                if(value > maxValue) updateMaxStoryKey(context, value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public static void findNextAvailableAlbumKey(final Context context, DatabaseReference reference){

        reference.orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                int maxValue = getMaxAlbumKey(context);
                int value = Integer.parseInt(dataSnapshot.getKey());
                if(value > maxValue) updateMaxAlbumKey(context, value);
                Log.d("RCD",String.valueOf(value));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public static String generateFileNameByCurrentTimeStamp(){
        return new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new java.util.Date()) + ".3gp";
    }

    public static void addFileMetaDataToFirebase(Context context, String storyName, Album albumSelected, int duration_ms, UploadTask.TaskSnapshot taskSnapshot) {
        @SuppressWarnings("VisibleForTests")
        Uri downloadUrl = taskSnapshot.getDownloadUrl();
        String urlString = downloadUrl.toString();

        createNewFireBaseStory(context,storyName,duration_ms,urlString);
    }
}