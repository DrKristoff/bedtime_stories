package com.sidegigapps.bedtimestories;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryand on 12/26/2015.
 */
public class AlbumAdapter extends ArrayAdapter<Album> {

    private static final String LOG_TAG = AlbumAdapter.class.getSimpleName();

    List<Album> mAlbumsList = new ArrayList<Album>();
    Context mContext;

    public AlbumAdapter(Activity context, List<Album> movies) {
        super(context, 0, movies);
        this.mAlbumsList = movies;
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Album album = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.album_grid_item, parent, false);
        }

        ImageView iconView = (ImageView) convertView.findViewById(R.id.grid_item_imageView);
        //iconView.setImageResource(album.image);

        Picasso.with(mContext).setLoggingEnabled(true);
        Picasso.with(mContext).load(album.getCoverURL()).error(R.drawable.common_google_signin_btn_icon_dark).into(iconView);
        return convertView;
    }
}