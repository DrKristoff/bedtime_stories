package com.sidegigapps.bedtimestories;

import android.app.ActionBar;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailFragment extends BaseFragment implements
        StoryRecyclerViewAdapter.OnItemSelected,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnSeekCompleteListener{

    private DatabaseReference mDatabase;
    private Album albumSelected;

    private Handler mHandler = new Handler();

    HashMap<String, Story> fullStoryMap = new HashMap<>();
    ArrayList<Story> storyArrayList = new ArrayList<>();
    StoryRecyclerViewAdapter storyListAdapter;

    boolean playerServiceReady = false;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    SeekBar seekBar;
    TextView currentTimeLabel, totalTimeLabel;
    RecyclerView recyclerView;

    PlayerService service;

    public AlbumDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        service = baseActivity.getPlaybackService();

        //TODO: this works with a smart phone, but what about tablet?  The seekbar will need to be added to a different activity layout
        seekBar = (SeekBar) getActivity().findViewById(R.id.seek_bar);

        seekBar = (SeekBar) getActivity().findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);

        currentTimeLabel = (TextView) getActivity().findViewById(R.id.currentLocationLabel);
        totalTimeLabel = (TextView) getActivity().findViewById(R.id.totalDurationLabel);

        //TODO: remove seekbar null check
        Log.d("RCD","NULL? " + String.valueOf(seekBar==null));

        if (getArguments().containsKey(AlbumDetailActivity.ALBUM)) {
            albumSelected = getArguments().getParcelable(AlbumDetailActivity.ALBUM);
            if(albumSelected==null) return;

            Activity activity = this.getActivity();
            ActionBar actionBar = activity.getActionBar();

            if (actionBar != null) {
                actionBar.setTitle(albumSelected.getTitle());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initializeRecyclerViewRows() {
        if(albumSelected.getStoryList()==null) return;
        for(String storyID : albumSelected.getStoryList()){

            //storyListAdapter.add(fullStoryMap.get(storyID));
            if(fullStoryMap.containsKey(storyID)) {
                storyArrayList.add(fullStoryMap.get(storyID));
            } else {
                Log.d("RCD","Missing storyID: " + storyID);
            }
        }
        storyListAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_detail, container, false);

        //TODO:  fix the list items so they look better!

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        //recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        storyListAdapter = new StoryRecyclerViewAdapter(getActivity(),storyArrayList, this);
        recyclerView.setAdapter(storyListAdapter);

        return rootView;
    }

    public void onPlayPauseTogglePressed(Story story){

    }

    @Override
    public void onItemSelected(Story story) {

        //ToDo: crashes if you select the same song again
        //play the story
        if(service==null) service=baseActivity.getPlaybackService();
        if(service!=null){
            service.streamMusic(story);
            updateProgressBar();
            storyListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(),"Playback Error",Toast.LENGTH_SHORT).show();
        }

        //Toast.makeText(getActivity(),"Pressed " + String.valueOf(story.getStoryTitle()),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.d("RCD","onSeekComplete");

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //Log.d("RCD","onProgressChanged");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d("RCD","onStartTrackingTouch");
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d("RCD","onStopTrackingTouch");
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = service.getFileDuration();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        service.scrubTo(currentPosition);

        // update timer progress again
        updateProgressBar();

    }

    public void updateProgressBar() {
        seekBar.setMax(service.getFileDuration()/1000);
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            int totalDuration = service.getFileDuration();
            long currentDuration = service.getCurrentLocation();

            totalTimeLabel.setText(Utils.milliSecondsToTimer(totalDuration));
            currentTimeLabel.setText(Utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(Utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            seekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);


            seekBar.setMax(totalDuration/1000);
            final int updateFrequency= 1000;

        }
    };

    public void hidePlayPauseButton(int position) {

        RecyclerView.ViewHolder holder =  recyclerView.findViewHolderForLayoutPosition(position);
        if(holder!=null){
            ((StoryRecyclerViewAdapter.CustomViewHolder)holder).playPause.setVisibility(View.INVISIBLE);
        }
    }
}
