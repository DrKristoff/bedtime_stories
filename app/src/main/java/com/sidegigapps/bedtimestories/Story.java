package com.sidegigapps.bedtimestories;

import android.provider.ContactsContract;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

/**
 * Created by ryand on 2/15/2017.
 */

public class Story {
    private String id;
    private String url;
    private String coverURL;

    public int getDuration() {
        return duration;
    }

    private int duration;
    private String storyTitle;

    public String getUrl() {
        return url;
    }

    public Story(DataSnapshot storySnapshot){
        id = storySnapshot.getKey();
        url = (String) storySnapshot.child("URL").getValue();
        storyTitle = (String) storySnapshot.child("storyTitle").getValue();
        duration = Integer.parseInt((String)storySnapshot.child("duration_ms").getValue());
        //TODO: error handling if database data is malformed
    }

    public String getStoryTitle() {
        return storyTitle;
    }
}
