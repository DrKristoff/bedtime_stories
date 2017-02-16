package com.sidegigapps.bedtimestories;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailFragment extends Fragment {

    private DatabaseReference mDatabase;
    public static final String ALBUM = "Album_Selected";
    private Album albumSelected;

    HashMap<String, Story> fullStoryMap = new HashMap<>();
    ArrayList<Story> storyArrayList = new ArrayList<>();
    StoryRecyclerViewAdapter storyListAdapter;

    public AlbumDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ALBUM)) {
            albumSelected = getArguments().getParcelable(ALBUM);
            if(albumSelected==null) return;

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            ImageView coverImage = (ImageView) activity.findViewById(R.id.header_logo);

            Picasso.with(getActivity()).load(albumSelected.getCoverURL())
                    .error(R.drawable.common_google_signin_btn_icon_dark).into(coverImage);


            if (appBarLayout != null) {
                appBarLayout.setTitle(albumSelected.getTitle());
            }
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot albumSnapshot = dataSnapshot.child("stories");
                for(DataSnapshot story : albumSnapshot.getChildren()){
                    Story newStory = new Story(story);
                    fullStoryMap.put(story.getKey(),newStory);
                }

                initializeRecyclerViewRows();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("RCD","cancelled");

            }
        });
    }

    private void initializeRecyclerViewRows() {
        for(String storyID : albumSelected.getStoryList()){
            //storyListAdapter.add(fullStoryMap.get(storyID));
            storyArrayList.add(fullStoryMap.get(storyID));
        }
        storyListAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.album_detail, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        storyListAdapter = new StoryRecyclerViewAdapter(getActivity(),storyArrayList);
        recyclerView.setAdapter(storyListAdapter);

        return rootView;
    }
}
