package com.sidegigapps.bedtimestories;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.SeekBar;

import com.sidegigapps.bedtimestories.RecordingFragment.OnRecordingCompleteListener;

/**
 * An activity representing a single AlbumPrototype detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link AlbumListActivity}.
 */
public class AlbumDetailActivity extends BaseActivity implements
        OnRecordingCompleteListener {

    Album album;
    public BaseActivity baseActivity;
    SeekBar seekBar;
    FloatingActionButton fab;

    public static final String ALBUM = "Album_Selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        baseActivity = (BaseActivity)this;
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecordingFragment();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        album = (Album)bundle.get(AlbumDetailActivity.ALBUM);

        if (savedInstanceState == null) {
            showAlbumDetailFragment();
        }
    }

    public void showRecordingFragment(){
        //TODO: for now, just hide the FAB, but in the future, add animation from the right side to the middle and then back
        fab.setVisibility(View.INVISIBLE);
        Bundle arguments = new Bundle();
        arguments.putParcelable(AlbumDetailActivity.ALBUM, album);
        Fragment detailFragment = getSupportFragmentManager().findFragmentByTag("DETAIL_TAG");
        RecordingFragment fragment = new RecordingFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .remove(detailFragment)
                .add(R.id.album_detail_container, fragment,"RECORD_TAG")
                .commit();

    }

    public void showAlbumDetailFragment(){
        //TODO: for now, just hide the FAB, but in the future, add animation from the right side to the middle and then back
        fab.setVisibility(View.VISIBLE);
        Bundle arguments = new Bundle();
        arguments.putParcelable(AlbumDetailActivity.ALBUM, album);
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Fragment recordingFragment = getSupportFragmentManager().findFragmentByTag("RECORD_TAG");
        fragment.setArguments(arguments);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(recordingFragment!=null) transaction.remove(recordingFragment);
        transaction.add(R.id.album_detail_container, fragment,"DETAIL_TAG")
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, AlbumListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRecordingComplete(Uri uri) {

    }
}
