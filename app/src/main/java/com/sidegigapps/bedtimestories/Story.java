package com.sidegigapps.bedtimestories;

import android.provider.ContactsContract;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

/**
 * Created by ryand on 2/15/2017.
 */

public class Story {
    String id;
    String url;
    String storyTitle;

    public Story(DataSnapshot storySnapshot){
        id = storySnapshot.getKey();
        url = (String) storySnapshot.child("URL").getValue();
        storyTitle = (String) storySnapshot.child("storyTitle").getValue();
        //TODO: error handling if database data is malformed
    }

    public String getStoryTitle() {
        return storyTitle;
    }
}
