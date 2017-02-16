package com.sidegigapps.bedtimestories;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ryand on 2/15/2017.
 */

public class StoryRecyclerViewAdapter extends RecyclerView.Adapter<StoryRecyclerViewAdapter.CustomViewHolder> {
    private List<Story> storyList;
    private Context mContext;

    public StoryRecyclerViewAdapter(Context context, List<Story> storyList) {
        this.storyList = storyList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.story_row_layout, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Story story = storyList.get(i);
        customViewHolder.textView.setText(story.getStoryTitle());
    }

    public void add(Story story){
        storyList.add(story);
    }

    @Override
    public int getItemCount() {
        return (null != storyList ? storyList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        //protected ImageView imageView;
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.title);
        }
    }
}
