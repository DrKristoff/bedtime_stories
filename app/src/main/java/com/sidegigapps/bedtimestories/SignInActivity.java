package com.sidegigapps.bedtimestories;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

public class SignInActivity extends BaseSignInActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.forgotPasswordTextView).setOnClickListener(this);
        findViewById(R.id.signUpTextView).setOnClickListener(this);
        findViewById(R.id.emailSignIn).setOnClickListener(this);

        //testing
        findViewById(R.id.button2).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
           case R.id.emailSignIn:
               showComingSoonToast();
                break;
            case R.id.signUpTextView:
                showComingSoonToast();
                break;
            case R.id.forgotPasswordTextView:
                showComingSoonToast();
                break;
            case R.id.button2:
                //Intent intent = new Intent(SignInActivity.this, ChooserActivity.class);
                //startActivity(intent);
                break;
        }
    }

    private void showComingSoonToast() {
        Toast.makeText(this, R.string.in_development_toast_string,Toast.LENGTH_SHORT).show();
    }


}
