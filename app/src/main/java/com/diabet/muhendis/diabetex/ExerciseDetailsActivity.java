package com.diabet.muhendis.diabetex;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.diabet.muhendis.diabetex.db.DiabetWatchDbHelper;
import com.diabet.muhendis.diabetex.helpers.FirebaseDBHelper;
import com.diabet.muhendis.diabetex.helpers.LocalDBHelper;
import com.diabet.muhendis.diabetex.helpers.UIHelper;
import com.diabet.muhendis.diabetex.model.ProgramExerciseFirebaseDb;
import com.diabet.muhendis.diabetex.model.StatisticsExerciseFirebaseDb;
import com.diabet.muhendis.diabetex.model.StatisticsProgramFirebaseDb;
import com.diabet.muhendis.diabetex.services.LocationService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExerciseDetailsActivity extends AppCompatActivity implements SensorEventListener{
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 2000;

    Chronometer chronometer;
    ImageView mVideoPlay;
    TextView mHeader,mInstruction,mSet,mRep,mRest,mDailyRep,mWeeklyRep;
    String eid,pid,videoLink,photoLink;
    DiabetWatchDbHelper mDbHelper;
    LocalDBHelper mLocalDbHelper;
    UIHelper mUIHelper;
    ProgramExerciseFirebaseDb mExercise;
    FirebaseDBHelper mFirebaseDbHelper;
    Button mDoneButton;
    Activity activity = this;
    SensorManager mSensorManager;
    Sensor mStepCounter;
    int stepCounter=0,walkedDistance=0;
    long startTime=0,elapsedTime=0;
    LocationManager locationManager;
    static final int OPEN_LOCATION_SETTINGS_REQUEST = 1;
    static final int OPEN_VIDEO_PLAYER_REQUEST = 0;
    static final int OPEN_LOCATION_PERMISSION_REQUEST = 2;

    List<String> walkingSpeeds;
    Location startLocation,endLocation;
    Intent startIntent;
    Boolean isStartedWalking=false;

    private final String TAG = "ExerciseDetailsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_details);


        walkingSpeeds = new ArrayList<String>();
        startTime = System.currentTimeMillis();

        mDoneButton = findViewById(R.id.exerciseDoneButton);
        mDbHelper = new DiabetWatchDbHelper(getApplicationContext());
        mLocalDbHelper = new LocalDBHelper(mDbHelper);
        mFirebaseDbHelper = new FirebaseDBHelper(getApplicationContext(),this);
        mUIHelper = new UIHelper(this);

        eid = getIntent().getStringExtra(Keys.EX_ID);
        pid = getIntent().getStringExtra(Keys.PID_KEY);

        if(mLocalDbHelper.isExerciseFinishedToday(eid,pid)){
            mDoneButton.setVisibility(View.GONE);
        }

        mExercise = mLocalDbHelper.getExercise(pid,eid);

        mHeader = findViewById(R.id.exDetailsHeader);
        mInstruction = findViewById(R.id.exDetailsExpEditText);
        mSet = findViewById(R.id.exerciseSets);
        mRep = findViewById(R.id.exerciseRep);
        mRest = findViewById(R.id.exerciseRest);
        mDailyRep = findViewById(R.id.exerciseDailyRep);
        mWeeklyRep = findViewById(R.id.exerciseWeeklyRep);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mExercise.getIsWalking() && !isStartedWalking){
                    mUIHelper.showSimpleAlertWithButton(getResources().getString(R.string.userCouldCloseScreenTitle),
                            getResources().getString(R.string.userCouldCloseScreenMessage),
                            getResources().getString(R.string.userCouldCloseScreenButtonTitle));
                    isStartedWalking=true;
                    mDoneButton.setText(getResources().getString(R.string.exerciseDetailsStopExerciseTitle));
                    mDoneButton.setBackgroundColor(getResources().getColor(R.color.redDark));
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                }
                else{
                    if(chronometer!=null)
                        chronometer.stop();
                    elapsedTime = (System.currentTimeMillis()-startTime)/1000;
                    showExerciseDone();
                }

            }
        });

        mHeader.setText(mExercise.getName());
        mInstruction.setText(mExercise.getInstruction());
        mSet.setText(String.valueOf(mExercise.getSets()));
        mRep.setText(String.valueOf(mExercise.getReps()));
        mRest.setText(String.valueOf(mExercise.getHold())+" sn");
        mDailyRep.setText(String.valueOf(mExercise.getDailyRep()));
        mWeeklyRep.setText(String.valueOf(mExercise.getWeeklyRep()));
        videoLink = mExercise.getVideoLink();
        photoLink = mExercise.getPhotoLink();


        mVideoPlay = findViewById(R.id.exDetailsVideoPlayImage);
        implementPlayImageOnClickListener();

        Toolbar toolbar = findViewById(R.id.exDetailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setDisplayOptions();


        String imageFilePath = Environment.getExternalStorageDirectory()
                + File.separator + ".diabetex/.images/"+mExercise.getPid()+"_"+mExercise.getEid()+".png";
        File imageFile = new File(imageFilePath);
        if(imageFile.exists())
        {
            ((ImageView) findViewById(R.id.exDetailsImage)).setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
        }
        else{
            new DownloadImageTask((ImageView) findViewById(R.id.exDetailsImage)).execute(photoLink);
        }

        checkAndSetupForWalkingExercise();
    }

    private void implementPlayImageOnClickListener()
    {
        mVideoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoFilePath = Environment.getExternalStorageDirectory()
                        + File.separator + ".diabetex/.videos/"+mExercise.getPid()+"_"+mExercise.getEid()+".mp4";
                Intent intent = new Intent(getApplicationContext(),ExerciseVideoActivity.class);
                intent.putExtra(Keys.VIDEO_KEY,videoLink);
                intent.putExtra(Keys.LOCAL_VIDEO_KEY,videoFilePath);
                activity.startActivityForResult(intent,OPEN_VIDEO_PLAYER_REQUEST);

            }
        });
    }

    private void checkAndSetupForWalkingExercise(){


        if(mExercise.getIsWalking()){
            setChronometer();
            mDoneButton.setText(getResources().getString(R.string.exerciseDetailsStartExerciseTitle));

            FrameLayout exerciseImages = findViewById(R.id.exDetailsImages);
            exerciseImages.setVisibility(View.GONE);

            // Use and change set and rep texts for minimum and maximum speed to inform user
            findViewById(R.id.exerciseDetailsRestLinearLayout).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.exerciseDetailsSetHeader)).setText(getResources().getString(R.string.exerciseDetailsActivityMinWalkingSpeedTitle));
            ((TextView)findViewById(R.id.exerciseDetailsRepHeader)).setText(getResources().getString(R.string.exerciseDetailsActivityMaxWalkingSpeedTitle));
            ((TextView)findViewById(R.id.exerciseSets)).setText(mExercise.getMinWalkingSpeed()+" km/sa");
            ((TextView)findViewById(R.id.exerciseRep)).setText(mExercise.getMaxWalkingSpeed()+" km/sa");

            /*
             * Register Location services
             * */

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},OPEN_LOCATION_PERMISSION_REQUEST);
            }


            isLocationEnabled();
        }
    }
    private void setChronometer(){
        chronometer = new Chronometer(this);
        chronometer.setTextSize(64);

        chronometer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(0,20,0,20);
        chronometer.setLayoutParams(params);
        LinearLayout mainLL = findViewById(R.id.exerciseDetailsMainLinearLayout);
        mainLL.addView(chronometer,1);
    }

    private void setDisplayOptions()
    {
        //Get window metrics to resize transperent image layer when actual image being downloaded
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        ImageView mTransperentImage = findViewById(R.id.exDetailsTransperentLayer);
        mTransperentImage.setMinimumHeight((int)(width/1920.0*1080));
    }

    public void showExerciseDone()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Egzersizi Tamamla");
        alertDialogBuilder.setIcon(R.drawable.ic_done_all_black_24dp);

        //final Context context = getApplicationContext();
        // set dialog message
        alertDialogBuilder
                .setMessage("Egzersizi bitirdim.")
                .setCancelable(true)
                .setPositiveButton("TAMAM",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        // Calculate the min time which should pass to finish this exercise
                        int minTimeForExercise = mExercise.getDuration()*mExercise.getReps()*mExercise.getSets();

                        // Check if spent time for this exercise is enough
                        // If not don not let user to finish exercise
                        if((int)elapsedTime<minTimeForExercise)
                        {
                            dialog.dismiss();
                            mUIHelper.showSimpleAlertWithButton(getResources().getString(R.string.spentTimeForExerciseSoLowTitle),getResources().getString(R.string.spentTimeForExerciseSoLowMessage),getResources().getString(R.string.spentTimeForExerciseSoLowBtnText));
                        }
                        else{
                            walkingSpeeds = LocationService.getInstance().walkingSpeeds;
                            walkedDistance = LocationService.getInstance().walkedDistance;

                            StatisticsExerciseFirebaseDb statisticsExerciseFirebaseDb = new StatisticsExerciseFirebaseDb(getCurrentDate(), mExercise.getEid(),mExercise.getPid(),mExercise.getUid(),(int)elapsedTime,stepCounter,walkingSpeeds,mExercise.getIsWalking(),walkedDistance);
                            mLocalDbHelper.insertStatisticsExercise(statisticsExerciseFirebaseDb,mFirebaseDbHelper);

                            if(startIntent!=null){
                                stopService(startIntent);
                            }

                            if(mLocalDbHelper.isAllExerciseFinishedForProgram(mExercise.getPid()))
                            {
                                dialog.dismiss();
                                showProgramFinished(mLocalDbHelper,mExercise.getPid());
                            }
                            else
                            {
                                setResult(Keys.FINISHED_EXERCISE_RESULT_CODE);
                                finish();
                            }
                        }


                    }
                })
                .setNegativeButton("VAZGEÇ",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        if(chronometer!=null)
                            chronometer.start();
                    }
                })
        ;

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgramFinished(LocalDBHelper mLocalDBHelper, String pid){
        final LocalDBHelper mLocalDbHelper = mLocalDBHelper;
        final String currentPid = pid;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        final View custom_dialog = inflater.inflate(R.layout.custom_dialog, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(custom_dialog)
                .setCancelable(false)
                .setTitle("Tebrikler programınızı tamamladınız.")
                .setMessage("Lütfen aşağıdaki değerleri giriniz.")
                // Add action buttons
                .setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String diabetesString = ((EditText)custom_dialog.findViewById(R.id.diabetes)).getText().toString();
                String diastoleString = ((EditText)custom_dialog.findViewById(R.id.diastole)).getText().toString();
                String systoleString = ((EditText)custom_dialog.findViewById(R.id.systole)).getText().toString();
                String pulseString = ((EditText)custom_dialog.findViewById(R.id.pulse)).getText().toString();

                if(!diabetesString.equals("") && !diastoleString.equals("") && !systoleString.equals("") && !pulseString.equals(""))
                {
                    float diabetesVal = Float.parseFloat(diabetesString);
                    float diastoleVal = Float.parseFloat(diastoleString);
                    float systoleVal = Float.parseFloat(systoleString);
                    float pulseVal = Float.parseFloat(pulseString);


                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.saved_user_file_key), Context.MODE_PRIVATE);
                    int uid = sharedPref.getInt(getString(R.string.saved_user_uid_key), 0);
                    StatisticsProgramFirebaseDb statisticsProgramFirebaseDb = new StatisticsProgramFirebaseDb(currentPid,String.valueOf(uid),getCurrentDate(),String.valueOf(diastoleVal),String.valueOf(systoleVal),String.valueOf(pulseVal),String.valueOf(diabetesVal),false);
                    mLocalDbHelper.insertStatisticsProgram(statisticsProgramFirebaseDb,mFirebaseDbHelper);
                    Toast.makeText(getApplicationContext(),"Program tamamlandı ve değerleriniz kaydedildi.",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    setResult(Activity.RESULT_OK);
                    finish();

                }
                else {
                    Toast.makeText(getApplicationContext(),"Lütfen tüm değerleri giriniz.",Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    public String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy");
        String strDate = mdformat.format(calendar.getTime());
        return strDate;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_LOCATION_SETTINGS_REQUEST) {
            checkAndSetupForWalkingExercise();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }


        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepCounter++;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static boolean isKitKatWithStepCounter(PackageManager pm) {

        // Require at least Android KitKat
        int currentApiVersion = (int) Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= 19
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);

    }

    private void isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
            alertDialog.setTitle("Konum Servislerini Etkinleştir");
            alertDialog.setMessage("Yürüme egzersizini yapabilmek için lütfen konum servislerinizi etkinleştiriniz.");
            alertDialog.setPositiveButton("Konum Servisi Etkinleştir", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent,OPEN_LOCATION_SETTINGS_REQUEST);
                    dialog.dismiss();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }
        else{
            startIntent = new Intent(this, LocationService.class);
            startIntent.putExtra(getResources().getString(R.string.minimumSpeedIntentKey),mExercise.getMinWalkingSpeed());
            startIntent.putExtra(getResources().getString(R.string.maximumSpeedIntentKey),mExercise.getMaxWalkingSpeed());

            //startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startIntent);
            }
            else{
                startService(startIntent);
            }
        }

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(startIntent!=null){
            stopService(startIntent);
        }
    }



}
