package com.sidegigapps.bedtimestories;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

/**
 * Created by ryand on 2/10/2017.
 */

public class Album {

    public String albumTitle;
    private String coverURL;
    private ArrayList<String> storyList;

    public Album(String albumTitle, String coverURL){

    }

    public Album(DataSnapshot snapshot){
        albumTitle = (String) snapshot.child("albumTitle").getValue();
        coverURL = (String) snapshot.child("coverURL").getValue();
        storyList = (ArrayList<String>) snapshot.child("stories").getValue();
    }

    public String getCoverURL() {
        return coverURL;
    }
}
