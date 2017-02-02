package com.sidegigapps.bedtimestories;

import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import com.sidegigapps.bedtimestories.ListActivity;

import jonathanfinerty.once.Once;

public class BaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String SignInActivity = "signInActivity";
    public static final String SetupActivity = "setupActivity";
    public static final String CalendarActivity = "calendarActivity";
    public static final String TodayActivity = "todayActivity";
    BaseActivity baseActivity;

    public static final String setupCompleted = "setupCompleted";
    public static final String userSignedIn = "userSignedIn";

    public static final int RC_SIGN_IN = 0x01;

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = this;

        Once.initialise(this);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(getString(R.string.rcd_debug_tag), getString(R.string.auth_state_signed_in) + user.getUid());

                    saveUserProfileData(user);

                    String uid = user.getUid();

                    //to prevent the auth listener being triggered multiple times
                    if(!Once.beenDone(Once.THIS_APP_SESSION,userSignedIn)){
                        if (!Once.beenDone(Once.THIS_APP_INSTALL, setupCompleted)) {
                            startActivity(new Intent(BaseActivity.this, ListActivity.class));

                            //mark as done on confirmation that setup is complete, not here
                            //Once.markDone(setupCompleted);
                        } else {
                            //startActivity(new Intent(BaseActivity.this, ChoreListActivity.class));
                        }
                        Once.markDone(userSignedIn);
                    }

                } else {
                    // User is signed out
                    Log.d(getString(R.string.rcd_debug_tag), getString(R.string.auth_state_signed_out));
                }
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void saveUserProfileData(FirebaseUser user){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.user_data), 0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        for (UserInfo profile : user.getProviderData()) {
            String name = profile.getDisplayName();
            String email = profile.getEmail();
            Uri photoUrl = profile.getPhotoUrl();

            editor.putString(getString(R.string.prefs_user_name), name);
            if(email!=null) editor.putString(getString(R.string.prefs_user_email), email);
            if(photoUrl!=null) editor.putString(getString(R.string.prefs_user_photo), photoUrl.toString());
            editor.apply();
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    public void signOut() {
        // Firebase sign out
        mAuth.signOut();

        Once.clearDone(userSignedIn);

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });

        navigateTo(SignInActivity);
    }

    public void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                handleSignInResult(result);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(BaseActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            TextView view = (TextView) findViewById(R.id.auth_status);
            view.setText(acct.getDisplayName());

        } else {

        }
    }

    // [START signin]
    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    public void navigateTo(String page){
        Intent intent;
        switch(page){
            case SignInActivity:
                intent = new Intent(BaseActivity.this, SignInActivity.class);
                startActivity(intent);
                break;
/*            case SetupActivity:
                intent = new Intent(BaseActivity.this, SetupActivity.class);
                startActivity(intent);
                break;
            case TodayActivity:
                intent = new Intent(BaseActivity.this, ChoreListActivity.class);
                startActivity(intent);
                break;
            case CalendarActivity:
                intent = new Intent(BaseActivity.this, CalendarActivity.class);
                startActivity(intent);
                break;*/

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.connection_failed,Toast.LENGTH_SHORT).show();
    }
}
