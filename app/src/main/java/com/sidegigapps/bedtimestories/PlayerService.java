package com.sidegigapps.bedtimestories;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

public class PlayerService extends Service {

    MediaPlayer mediaPlayer;


    public PlayerService() {

        mediaPlayer = new MediaPlayer();
    }

    public void streamMusic(String url){

        url = "https://firebasestorage.googleapis.com/v0/b/bedtime-stories.appspot.com/o/Ryan%20Dymock%20-%20Chapter%200%20Intro.mp3?alt=media&token=38c05ddf-94b8-442d-9d7b-64e6e701340b"; // your URL here

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        mediaPlayer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
