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


        mAlbumAdapter = new AlbumAdapter(getActivity(), albumArrayList);

        ((AlbumListActivity)getActivity()).showProgressDialog();

        //TODO: add progress bar over screen as Firebase is loading data into grid view
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot albumSnapshot = dataSnapshot.child("albums");
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
        //SwipeRefreshLayout layout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
       //layout.setOnRefreshListener(this);

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
        ((AlbumListActivity)getActivity()).onRefresh();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<Album>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private ArrayList<Album> getMovieDataFromJSONString(String movieJSONString)
                throws JSONException {

            final String MOVIE_LIST = "results";

            ArrayList<Album> resultsList = new ArrayList<>();

            JSONObject movieJson = new JSONObject(movieJSONString);
            JSONArray moviesArray = movieJson.getJSONArray(MOVIE_LIST);

            for(int i =0; i < moviesArray.length(); i++){
                JSONObject movie = moviesArray.getJSONObject(i);
                //resultsList.add(i, new Album(movie));
            }

            return resultsList;

        }

        @Override
        protected ArrayList<Album> doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String popularMoviesJsonString = null;

            String format = "json";

            /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));

            if(sortBy.equals(getString(R.string.pref_sort_favorites))){
                return FavoritesHelper.loadFavorites(getActivity());
            }*/

            //sortBy = sortBy + ".desc"; //vote_average.desc

            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
                final String APPID_PARAM = "api_key";
                final String SORT_BY_PARAM = "sort_by";
                    final String MIN_VOTES_PARAM = "vote_count.gte";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        //.appendQueryParameter(SORT_BY_PARAM, sortBy)
                        .appendQueryParameter(MIN_VOTES_PARAM, "200")
                        //.appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DATABASE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                popularMoviesJsonString = buffer.toString();

                Log.v(LOG_TAG, "AlbumPrototype string: " + popularMoviesJsonString);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJSONString(popularMoviesJsonString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Album> result) {
            if (result != null) {
                mAlbumAdapter.clear();
                for(Album album : result) {
                    mAlbumAdapter.add(album);
                }
              // New data is back from the server.  Hooray!
            }
        }
    }

    public interface AlbumSelectedCallback {

        public void onItemSelected(Album album);
    }

}
