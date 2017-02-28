package com.sidegigapps.bedtimestories;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

public class AlbumListActivity extends BaseActivity implements
        AlbumsGridViewFragment.AlbumSelectedCallback {
    private boolean mTwoPane;

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list);

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

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void onRefresh() {
        //TODO: not working, fix later
        /*Fragment fragment = getSupportFragmentManager().findFragmentByTag("gridView");
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(fragment);
        ft.attach(new AlbumsGridViewFragment());
        ft.commit();*/

    }
}
