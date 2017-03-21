package com.sidegigapps.bedtimestories;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.AppCompatButton;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by ryand on 3/6/2017.
 */

public class AudioRecordingService extends Service {

    String fileName;

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    MediaPlayer mediaPlayer;

    public boolean isRecording = false;

    private MediaRecorder mRecorder = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public class LocalBinder extends Binder {
        AudioRecordingService getService() {
            return AudioRecordingService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isRecording) stopRecording();
    }

    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setAudioSamplingRate(44100);

        mRecorder.setOutputFile("/" + fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            //Log.e("RCD", e.getLocalizedMessage());
        } catch (IllegalStateException e){
            //Log.e("RCD", e.getLocalizedMessage());
        }

    }

    public void pauseRecording(){
        stopRecording();

    }

    public void resumeRecording(){
        mediaPlayer.start();
        isRecording = true;

    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        isRecording = false;

        Toast.makeText(AudioRecordingService.this, "Recording Completed",
                Toast.LENGTH_LONG).show();
    }

    class RecordButton extends AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }


}
