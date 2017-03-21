package com.sidegigapps.bedtimestories;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AlbumsGridViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private DatabaseReference mDatabase;
    SwipeRefreshLayout swipeLayout;

    GridView mGridView;
    private AlbumAdapter mAlbumAdapter;
    private ArrayList<Album> albumArrayList = new ArrayList<>();
    private ArrayList<Story> storyArrayList = new ArrayList<>();

    public AlbumsGridViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            //albumArrayList = (ArrayList<Album>)savedInstanceState.get(MOVIE_KEY);
        }

        loadData();

    }

    private void loadData(){
        mAlbumAdapter = new AlbumAdapter(getActivity(), albumArrayList);
        mDatabase = null;

        //Detect max Album key and Story key for use when user adds album
        Utils.findNextAvailableAlbumKey(getActivity(),FirebaseDatabase.getInstance().getReference("albums"));
        Utils.findNextAvailableStoryKey(getActivity(),FirebaseDatabase.getInstance().getReference("stories"));

        ((AlbumListActivity)getActivity()).showProgressDialog();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot albumSnapshot = dataSnapshot.child("albums");
                mAlbumAdapter.clear();
                for(DataSnapshot album : albumSnapshot.getChildren()){
                    Album newAlbum = new Album(album);
                    mAlbumAdapter.add(newAlbum);
                    mAlbumAdapter.notifyDataSetChanged();
                }

                //TODO:  detect if internet is connected.  If not the progress bar shows indefinitely
                ((AlbumListActivity)getActivity()).hideProgressDialog();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("RCD","cancelled");
                ((AlbumListActivity)getActivity()).hideProgressDialog();

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView);

        mGridView.setAdapter(mAlbumAdapter);

        //TODO: not working, fix later
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        swipeLayout.setOnRefreshListener(this);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((AlbumSelectedCallback) getActivity()).onItemSelected(mAlbumAdapter.getItem(position));
                Log.d("RCD",String.valueOf("position: " + position));

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelableArrayList(MOVIE_KEY, (ArrayList<? extends Parcelable>) albumArrayList);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        //((AlbumListActivity)getActivity()).onRefresh();
        mAlbumAdapter.clear();
        mAlbumAdapter.notifyDataSetChanged();

        loadData();

        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
    }

    public interface AlbumSelectedCallback {

        public void onItemSelected(Album album);
    }

}
