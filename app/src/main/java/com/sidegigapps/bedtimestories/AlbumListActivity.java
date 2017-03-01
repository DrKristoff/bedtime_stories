package com.sidegigapps.bedtimestories;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

public class AlbumListActivity extends BaseActivity implements
        AlbumsGridViewFragment.AlbumSelectedCallback {
    private boolean mTwoPane;

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new Album dialog

                AlertDialog.Builder builder = new AlertDialog.Builder(AlbumListActivity.this);
                builder.setTitle("Album Title");

                final EditText input = new EditText(AlbumListActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String albumName = input.getText().toString();

                        //create new Album in Firebase
                        Utils.createNewFireBaseAlbum(getApplicationContext(),albumName);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

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
