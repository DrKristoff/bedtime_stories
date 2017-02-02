package com.sidegigapps.bedtimestories;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ListActivity extends AppCompatActivity {

    boolean playerServiceReady = false;

    ServiceConnection serviceConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            service = binder.getService();
            playerServiceReady = true;
            //service.streamMusic(1);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };;
    PlayerService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        TextView song1 = (TextView) findViewById(R.id.item1);
        TextView song2 = (TextView) findViewById(R.id.item2);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.item1){
                    service.streamMusic(0);
                }
                if(view.getId() == R.id.item2){
                    service.streamMusic(1);
                }

            }
        };

        song1.setOnClickListener(listener);
        song2.setOnClickListener(listener);


        final Intent playerServiceIntent = new Intent(this, PlayerService.class);

        bindService(playerServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
