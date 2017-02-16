package com.sidegigapps.bedtimestories;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AlbumListActivity extends AppCompatActivity implements AlbumsGridViewFragment.AlbumSelectedCallback {
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getStoryTitle());*/

        if (findViewById(R.id.album_detail_container) != null) {
            mTwoPane = true;
        }
    }

    @Override
    public void onItemSelected(Album album) {
            if(mTwoPane){
                AlbumDetailFragment fragment = new AlbumDetailFragment();
                Bundle args = new Bundle();
                args.putParcelable(AlbumDetailFragment.ALBUM, album);
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.album_detail_container, fragment, "TAG")
                        .commit();

            } else {

                Intent intent = new Intent(this, AlbumDetailActivity.class);
                intent.putExtra(AlbumDetailFragment.ALBUM,album);
                startActivity(intent);

            }

    }
}
