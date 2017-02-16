package com.sidegigapps.bedtimestories;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

/**
 * Created by ryand on 2/10/2017.
 */

public class Album implements Parcelable {

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

    protected Album(Parcel in) {
        albumTitle = in.readString();
        coverURL = in.readString();
        if (in.readByte() == 0x01) {
            storyList = new ArrayList<String>();
            in.readList(storyList, String.class.getClassLoader());
        } else {
            storyList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(albumTitle);
        dest.writeString(coverURL);
        if (storyList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(storyList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public String getTitle() {
        return albumTitle;
    }

    public ArrayList<String> getStoryList() {
        return storyList;
    }
}
