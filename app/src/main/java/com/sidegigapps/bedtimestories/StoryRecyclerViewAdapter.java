package com.sidegigapps.bedtimestories;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by ryand on 2/15/2017.
 */

public class StoryRecyclerViewAdapter extends RecyclerView.Adapter<StoryRecyclerViewAdapter.CustomViewHolder> {
    private List<Story> storyList;
    private Context mContext;
    private OnItemSelected onItemSelected;
    private AlbumDetailFragment fragment;
    private CustomViewHolder activeViewHolder;

    public StoryRecyclerViewAdapter(Context context, List<Story> storyList, AlbumDetailFragment albumDetailFragment) {
        this.storyList = storyList;
        this.mContext = context;
        fragment = albumDetailFragment;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.story_row_layout, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final Story story = storyList.get(i);
        customViewHolder.textView.setText(story.getStoryTitle());
        customViewHolder.index = i;

    }

    public void add(Story story){
        storyList.add(story);
    }

    @Override
    public int getItemCount() {
        return (null != storyList ? storyList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        protected TextView textView;
        public ImageView playPause;
        protected boolean isActiveStory;
        protected View view;
        protected int index;

        public CustomViewHolder(View view) {
            super(view);
            this.view = view;
            this.textView = (TextView) view.findViewById(R.id.title);
            this.playPause = (ImageView) view.findViewById(R.id.playPauseToggleButton);
            this.isActiveStory = false;
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            final Story story = storyList.get(getAdapterPosition());
            fragment.onItemSelected(story);
            isActiveStory = true;

            playPause.setVisibility(View.VISIBLE);
            Toast.makeText(mContext,"Clicked Position: " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnItemSelected {
        void onItemSelected(Story story);
    }
}
