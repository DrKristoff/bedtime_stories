package com.sidegigapps.bedtimestories;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileInputStream;
import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener{

    MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    boolean isPlaying = false;

    String [] songArray = new String[]{"https://firebasestorage.googleapis.com/v0/b/bedtime-stories.appspot.com/o/Ryan%20Dymock%20-%20Chapter%200%20Intro.mp3?alt=media&token=38c05ddf-94b8-442d-9d7b-64e6e701340b",
            "https://firebasestorage.googleapis.com/v0/b/bedtime-stories.appspot.com/o/Ryan%20Dymock%20-%20Chapter%201.mp3?alt=media&token=53ed253f-d01b-4b4a-a45a-655bb4ab200c"};

    public PlayerService() {

        mediaPlayer = new MediaPlayer();
    }

    public void streamMusic(int input){

        String url = songArray[input];

        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.d("RCD","ERROR: ");
                return false;
            }
        });
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }
}
