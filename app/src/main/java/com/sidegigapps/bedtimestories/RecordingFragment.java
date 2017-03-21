package com.sidegigapps.bedtimestories;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class RecordingFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Chronometer chrono;
    ImageView cancelImageView, saveImageView;
    FloatingActionButton recordFAB;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {android.Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String albumString;
    private String mStoryTitle;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) getActivity().finish();

    }

    private OnRecordingCompleteListener mListener;

    public RecordingFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static RecordingFragment newInstance(String param1, String param2) {
        RecordingFragment fragment = new RecordingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    Album albumSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(AlbumDetailActivity.ALBUM)) {
            albumSelected = getArguments().getParcelable(AlbumDetailActivity.ALBUM);
            albumString = albumSelected.getTitle();
        }

        service = ((BaseActivity)getActivity()).getRecordingService();
        if(service!=null) readyToRecord = true;
        Log.d("RCD","Recording SERVICE NULL? " + String.valueOf(service==null));
    }

    AudioRecordingService service;
    private boolean readyToRecord = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recording, container, false);
        saveImageView = (ImageView) rootView.findViewById(R.id.imageViewSave);
        cancelImageView = (ImageView) rootView.findViewById(R.id.imageViewCancel);
        chrono = (Chronometer) rootView.findViewById(R.id.chronometer);
        //chrono.setFormat("hh:mm:ss");

        recordFAB = (FloatingActionButton) rootView.findViewById(R.id.fab);

        recordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service==null || !readyToRecord) return;
                //change record icon
                if(service.isRecording){
                    //stop the recording
                    recordFAB.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_mic_black_24dp));
                    service.stopRecording();

                    //TODO:  Would be cool if the buttons animated out from the center of the fab
                    saveImageView.setVisibility(View.VISIBLE);
                    cancelImageView.setVisibility(View.VISIBLE);
                    chrono.stop();

                    //show Rename Dialog
                    showRenameDialog();

                } else {
                    //start recording
                    recordFAB.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_stop_black_24dp));
                    startRecording();
                    chrono.setBase(SystemClock.elapsedRealtime());
                    chrono.start();
                }

                //begin updating timer


            }
        });

        saveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save file to Firebase
                uploadFile();

            }
        });

        cancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete file and return to album fragment
                File file = new File(mFileName);
                Log.d("RCD","file null? " + String.valueOf(file==null));
                file.delete();
                ((AlbumDetailActivity)getActivity()).showAlbumDetailFragment();
            }
        });

        // Record to the external cache directory for visibility
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/";
        mFileName += Utils.generateFileNameByCurrentTimeStamp();

        Log.d("RCD",mFileName);

        service.setFileName(mFileName);

        //TODO: How is this handled if the user denies the permission?
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        return rootView;
}

    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Story Title");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mStoryTitle = input.getText().toString();

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

    private void startRecording() {
        service.startRecording();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onRecordingComplete(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordingCompleteListener) {
            mListener = (OnRecordingCompleteListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRecordingCompleteListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

    private void uploadFile(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri file = Uri.fromFile(new File(mFileName));

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getActivity().getApplication(),file);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        final int milliSeconds = Integer.parseInt(durationStr);

        StorageReference fileRef = storageRef.child(albumString + "/"+file.getLastPathSegment());
        UploadTask uploadTask = fileRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //update realtime database with meta data about new recorded file
                Utils.addFileMetaDataToFirebase(getActivity(), mStoryTitle, albumSelected, milliSeconds, taskSnapshot);
                //TODO:  Delete file from local
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRecordingCompleteListener {
        // TODO: Update argument type and name
        void onRecordingComplete(Uri uri);
    }
}
