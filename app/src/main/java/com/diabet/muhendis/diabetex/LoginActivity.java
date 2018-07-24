package com.diabet.muhendis.diabetex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.diabet.muhendis.diabetex.helpers.FirebaseDBHelper;
import com.diabet.muhendis.diabetex.helpers.UIHelper;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by muhendis on 14.11.2017.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private FirebaseDBHelper mFirebaseDBHelper;
    private UIHelper mUIHelper;
    private final String TAG = "LoginActivity";
    private String fcmBody,fcmTitle,fcmMessageId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                if(key=="body")
                {
                    fcmBody = value.toString();
                }
                else if(key=="title"){
                    fcmTitle = value.toString();
                }
                else if(key=="messageId"){
                    fcmMessageId = value.toString();
                }
            }
        }

        fcmTitle = getIntent().getStringExtra("title");
        fcmBody = getIntent().getStringExtra("body");
        fcmMessageId = getIntent().getStringExtra("messageId");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        if(isLoggingOut())
        {
            SharedPreferences sharedPref = this.getSharedPreferences(getResources().getString(R.string.saved_user_file_key),Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(this.getString(R.string.saved_user_isloggedin_key), false);
            editor.apply();
        }
        else if(checkIsLoggedIn())
        {
            Intent intent  = new Intent(this,MainActivity.class);
            intent.putExtra(Keys.NOTIFICATION_MESSAGE_TITLE,fcmTitle);
            intent.putExtra(Keys.NOTIFICATION_MESSAGE_BODY,fcmBody);
            intent.putExtra(Keys.NOTIFICATION_MESSAGE_ID,fcmMessageId);
            startActivity(intent);
            this.finish();
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseDBHelper = new FirebaseDBHelper(getApplicationContext(),this);
        mUIHelper = new UIHelper(this);

        // Get Reference to variables
        etEmail = findViewById(R.id.emailEditText);
        etPassword = findViewById(R.id.passwordEditText);

    }

    public boolean checkIsLoggedIn(){
        SharedPreferences sharedPref = this.getSharedPreferences(getResources().getString(R.string.saved_user_file_key),Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean(getString(R.string.saved_user_isloggedin_key), false);
        return isLoggedIn;
    }



    public boolean isLoggingOut(){
        Intent intent = getIntent();
        return intent.getBooleanExtra(getResources().getString(R.string.logging_out_key),false);
    }

    // Triggers when LOGIN Button clicked
    public void checkLogin(View arg0) {
        if(!mUIHelper.isNetworkAvailable())
        {
            mUIHelper.showAlertNoInternet();
        }
        else{
            // Get text from email and passord field
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            mFirebaseDBHelper.attemptLogin(email,password,this);
        }

    }




}
