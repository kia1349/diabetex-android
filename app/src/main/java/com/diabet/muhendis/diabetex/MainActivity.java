package com.diabet.muhendis.diabetex;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.diabet.muhendis.diabetex.adapters.ProgramRecylerViewAdapter;
import com.diabet.muhendis.diabetex.db.DiabetWatchDbHelper;
import com.diabet.muhendis.diabetex.helpers.AlarmManagerHelper;
import com.diabet.muhendis.diabetex.helpers.FirebaseDBHelper;
import com.diabet.muhendis.diabetex.helpers.LocalDBHelper;
import com.diabet.muhendis.diabetex.helpers.ProgressBack;
import com.diabet.muhendis.diabetex.helpers.UIHelper;
import com.diabet.muhendis.diabetex.model.ProgramFirebaseDb;
import com.diabet.muhendis.diabetex.model.StatisticsProgramFirebaseDb;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private String notificationBody,notificationTitle,notificationMessageId;
    private LocalDBHelper mLocalDBHelper;
    private DiabetWatchDbHelper mDbHelper;
    private ProgramFirebaseDb[] mAllPrograms;
    private FirebaseDBHelper mFirebaseDBHelper;
    private ProgramRecylerViewAdapter adapter;
    private UIHelper mUIHelper;
    private AlarmManagerHelper alarmManagerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        isStoragePermissionGranted();
        isAccesNetworkPermissionGranted();
        updateToken();
        Intent intent = getIntent();
        notificationBody = intent.getStringExtra(Keys.NOTIFICATION_MESSAGE_BODY);
        notificationTitle = intent.getStringExtra(Keys.NOTIFICATION_MESSAGE_TITLE);
        notificationMessageId = intent.getStringExtra(Keys.NOTIFICATION_MESSAGE_ID);

        if(notificationBody!=null && notificationTitle!=null){
            showNotificationAlertFromFcm(notificationTitle,notificationBody);
        }

        alarmManagerHelper = new AlarmManagerHelper();
        alarmManagerHelper.setAlarms(getApplicationContext());
        mUIHelper = new UIHelper(this);
        mFirebaseDBHelper = new FirebaseDBHelper(getApplicationContext(),this);
        mDbHelper = new DiabetWatchDbHelper(getApplicationContext());
        mLocalDBHelper = new LocalDBHelper(mDbHelper);
        //mUIHelper.syncMediaFiles(mLocalDBHelper,this);


        mAllPrograms = mLocalDBHelper.getAllPrograms();
        mFirebaseDBHelper.syncLocalStatisticsWithFirebase(getUid());

        /*
         * Add a recylerview to show all exercises in the main screen
         */
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        if (mAllPrograms.length != 0) {
            findViewById(R.id.noProgramLinearLayout).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.noProgramLinearLayout).setVisibility(View.VISIBLE);
        }
        adapter = new ProgramRecylerViewAdapter(this,mAllPrograms);
        adapter.setLocalDbHelper(mLocalDBHelper);
        adapter.setMainActivity(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setAdapter(adapter);

        syncDataWithFirebase();
        removeCompletedProgramsFromDB();

        /*
         * Set your own toolbar
         */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setNavBar();

    }

    public void syncDataWithFirebase(){
        mFirebaseDBHelper.syncDataWithLocalDB(this,adapter);
    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public  boolean isAccesNetworkPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
        else{
            Toast.makeText(this, "Lütfen videoları depolamak için izin veriniz", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            //Crashlytics.getInstance().crash();
        }
    }

    public void logout_user(){
        Intent intent  = new Intent(this,LoginActivity.class);
        intent.putExtra(getResources().getString(R.string.logging_out_key),true);
        startActivity(intent);
        this.finish();
    }


    public void setNavBar(){
        SharedPreferences sharedPref = this.getSharedPreferences(getResources().getString(R.string.saved_user_file_key), Context.MODE_PRIVATE);
        String  name = sharedPref.getString(getString(R.string.saved_user_name_key), "");
        String  surname = sharedPref.getString(getString(R.string.saved_user_surname_key), "");
        String  email = sharedPref.getString(getString(R.string.saved_user_email_key), "");


        NavigationView navigationView = findViewById(R.id.nav_view);
        //View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        View headerView=navigationView.getHeaderView(0);
        TextView navbarNameInitials = headerView.findViewById(R.id.navbarNameInitials);
        TextView navbarUserName = headerView.findViewById(R.id.navbarUserNameSurname);
        TextView navbarUserMail = headerView.findViewById(R.id.navbarUserMail);

        navbarUserMail.setText(email);

        if(name!="" && surname !="")
        {
            navbarNameInitials.setText(name.substring(0,1)+surname.substring(0,1));
            navbarUserName.setText(name+" "+surname);
        }
        else if(name!="")
        {
            navbarNameInitials.setText(name.substring(0,1));
            navbarUserName.setText(name);
        }
        else if(surname!="")
        {
            navbarNameInitials.setText(surname.substring(0,1));
            navbarUserName.setText(surname);
        }
    }


    public void setLinksDownloaded(boolean isDownloaded){
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.saved_user_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.saved_user_all_videos_downloaded_key), isDownloaded);
        editor.apply();
    }

    public String getUid(){
        SharedPreferences sharedPref = this.getSharedPreferences(getResources().getString(R.string.saved_user_file_key),Context.MODE_PRIVATE);
        int uid = sharedPref.getInt(getString(R.string.saved_user_uid_key), 0);
        return String.valueOf(uid);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public String getYesterdaysDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy");
        String strDate = mdformat.format(calendar.getTime());
        return strDate;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_program) {
            // Handle the camera action
        }  else if (id == R.id.nav_exit) {
            logout_user();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK)
        {
            adapter.notifyDataSetChanged();
        }
        mUIHelper.syncMediaFiles(mLocalDBHelper,this);
    }

    // Removing from firebase will trigger to remove local db automatically
    // So just remove from firebase

    public void removeCompletedProgramsFromDB(){
        List<String> pidsToRemove = new ArrayList<String>();

        for (ProgramFirebaseDb program :
                mAllPrograms) {
            SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy");
            try {
                Date date = mdformat.parse(program.getFinishDate());
                if(System.currentTimeMillis()>date.getTime()){
                    mLocalDBHelper.deleteProgram(program.getPid());
                    ProgramFirebaseDb[] ap = mLocalDBHelper.getAllPrograms();
                    if (ap.length != 0) {
                        findViewById(R.id.noProgramLinearLayout).setVisibility(View.GONE);
                    }
                    else{
                        findViewById(R.id.noProgramLinearLayout).setVisibility(View.VISIBLE);
                    }
                    adapter.setAdapter(ap);
                    pidsToRemove.add(program.getPid());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        if(pidsToRemove.size()>0)
        {
            mFirebaseDBHelper.removeCompletedPrograms(pidsToRemove);
        }

    }

    public void showNotificationAlertFromFcm(String title,String message)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mFirebaseDBHelper.updateFcmMessageRead(notificationMessageId);
                    }
                })
                .setCancelable(false);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void updateToken(){
        FirebaseDatabase.getInstance().getReference().child("users/"+getUid()+"/token").setValue(FirebaseInstanceId.getInstance().getToken());

    }
}
