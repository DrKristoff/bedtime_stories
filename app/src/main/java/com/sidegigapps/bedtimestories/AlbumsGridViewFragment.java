package com.sidegigapps.bedtimestories;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
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

public class AlbumsGridViewFragment extends Fragment {

    private DatabaseReference mDatabase;

    GridView mGridView;
    private AlbumAdapter mAlbumAdapter;
    private ArrayList<AlbumPrototype> albumArrayList = new ArrayList<AlbumPrototype>();

    private String MOVIE_KEY = "movie_list";

    public AlbumsGridViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            albumArrayList = (ArrayList<AlbumPrototype>)savedInstanceState.get(MOVIE_KEY);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("RCD","CHANGED!");
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("RCD",child.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("RCD","cancelled");

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView);

        mAlbumAdapter = new AlbumAdapter(getActivity(), albumArrayList);
        mGridView.setAdapter(mAlbumAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((MovieSelectedCallback) getActivity()).onItemSelected(mAlbumAdapter.getItem(position));
                Log.d("RCD",String.valueOf("position: " + position));

            }
        });

        updateMovies();
        return rootView;
    }

    @Override
    public void onResume() {
        updateMovies();  //update posters after settings preference changed
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE_KEY, (ArrayList<? extends Parcelable>) albumArrayList);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void updateMovies(){
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //moviesTask.execute();

    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<AlbumPrototype>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private ArrayList<AlbumPrototype> getMovieDataFromJSONString(String movieJSONString)
                throws JSONException {

            final String MOVIE_LIST = "results";

            ArrayList<AlbumPrototype> resultsList = new ArrayList<>();

            JSONObject movieJson = new JSONObject(movieJSONString);
            JSONArray moviesArray = movieJson.getJSONArray(MOVIE_LIST);

            for(int i =0; i < moviesArray.length(); i++){
                JSONObject movie = moviesArray.getJSONObject(i);
                resultsList.add(i, new AlbumPrototype(movie));
            }

            return resultsList;

        }

        @Override
        protected ArrayList<AlbumPrototype> doInBackground(Void... params) {

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
        protected void onPostExecute(ArrayList<AlbumPrototype> result) {
            if (result != null) {
                mAlbumAdapter.clear();
                for(AlbumPrototype album : result) {
                    mAlbumAdapter.add(album);
                }
              // New data is back from the server.  Hooray!
            }
        }
    }

    public interface MovieSelectedCallback {

        public void onItemSelected(AlbumPrototype album);
    }

}
