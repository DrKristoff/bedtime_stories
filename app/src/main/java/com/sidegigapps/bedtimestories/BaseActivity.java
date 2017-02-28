package com.sidegigapps.bedtimestories;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BaseActivity extends AppCompatActivity {

    boolean playerServiceReady = false;
    PlayerService service;

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
    };

    public PlayerService getService(){
        return service;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.stopStreaming();
        unbindService(serviceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent playerServiceIntent = new Intent(this, PlayerService.class);
        bindService(playerServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }
}
