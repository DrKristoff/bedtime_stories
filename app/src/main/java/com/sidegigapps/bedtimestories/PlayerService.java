package com.sidegigapps.bedtimestories;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;

import java.io.FileInputStream;
import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    MediaPlayer mediaPlayer;
    int fileDuration;

    private final IBinder binder = new LocalBinder();
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    boolean isPlaying = false;

    public PlayerService() {
        mediaPlayer = new MediaPlayer();
    }

    public void streamMusic(Story story){

        stopStreaming();

        if(story.getUrl()==null) return;

        Log.d("RCD",String.valueOf(story.getDuration()));
        fileDuration = story.getDuration();

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.d("RCD","ERROR: ");
                return false;
            }
        });
        try {

            mediaPlayer.setDataSource(story.getUrl());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    public int getFileDuration(){
        return fileDuration;
    }

    public int getCurrentLocation(){
        return mediaPlayer.getCurrentPosition();
    }

    public void stopStreaming(){

        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void togglePlayPause(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

    }

    public void scrubTo(int seekBarLocation){
        mediaPlayer.seekTo(seekBarLocation*1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        //fileDuration = getFileDuration();


    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }
}
