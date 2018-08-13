package com.diabet.muhendis.diabetex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.diabet.muhendis.diabetex.adapters.ExerciseRecylerViewAdapter;
import com.diabet.muhendis.diabetex.adapters.ProgramRecylerViewAdapter;
import com.diabet.muhendis.diabetex.db.DiabetWatchDbHelper;
import com.diabet.muhendis.diabetex.helpers.FirebaseDBHelper;
import com.diabet.muhendis.diabetex.helpers.LocalDBHelper;
import com.diabet.muhendis.diabetex.helpers.UIHelper;
import com.diabet.muhendis.diabetex.model.ProgramExerciseFirebaseDb;
import com.diabet.muhendis.diabetex.model.StatisticsProgramFirebaseDb;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ExercisesActivity extends AppCompatActivity {


    private String currentPid;
    private final String PID_KEY = "ProgramId";
    private DiabetWatchDbHelper mDbHelper;
    private ProgramExerciseFirebaseDb[] mAllPrograms;
    private ExerciseRecylerViewAdapter adapter;
    private LocalDBHelper mLocalDBHelper;
    private UIHelper mUIHelper;
    private FirebaseDBHelper mFirebaseDbHelper;
    private final int diabetesMin=80,diabetesMax=400,systoleMin=90,systoleMax=180,diastoleMin=50,diastoleMax=150,pulseMin=50,pulseMax=145;

    private final String TAG = "ExercisesActivity";

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        mFirebaseDbHelper = new FirebaseDBHelper(getApplicationContext(),this);
        //Get the pid from the intent
        Intent intent = getIntent();
        currentPid = intent.getStringExtra(PID_KEY);

        Toolbar toolbar = findViewById(R.id.exerciseToolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new DiabetWatchDbHelper(getApplicationContext());
        mLocalDBHelper = new LocalDBHelper(mDbHelper);
        mUIHelper = new UIHelper(this);

        setupRecylerView();

        if(mLocalDBHelper.isAllExerciseFinishedForProgram(currentPid)
                && !mLocalDBHelper.isStatisticsProgramInsertedToday(currentPid))
        {
            showProgramFinished(mLocalDBHelper,currentPid);
        }
        else if(!mLocalDBHelper.isAllExerciseFinishedForProgram(currentPid))
        {
            checkIfProgramStartedToday();
        }

        mUIHelper.syncMediaFiles(mLocalDBHelper,this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


    }

    public void setupRecylerView(){

        mAllPrograms = mLocalDBHelper.getAllExercisesForProgram(currentPid);

        /*
         * Add a recylerview to show all exercises in the main screen
         */
        RecyclerView recyclerView = findViewById(R.id.exerciseRecyclerview);
        adapter = new ExerciseRecylerViewAdapter(this,mAllPrograms);
        adapter.setExercisesActivity(this);
        adapter.setLocalDbHelper(mLocalDBHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setAdapter(adapter);
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

    public void checkIfProgramStartedToday(){
        if(!mLocalDBHelper.isProgramDataEnteredToday(currentPid))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Get the layout inflater
            LayoutInflater inflater = this.getLayoutInflater();
            final View custom_dialog = inflater.inflate(R.layout.custom_dialog, null);
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(custom_dialog)
                    .setCancelable(false)
                    .setTitle("Lütfen egzersizlere başlamadan istenilen değerleri giriniz")
                    .setNegativeButton("VAZGEÇ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
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

                        Log.d(TAG,"diabetes:"+diabetesMax+" diastole:"+diastoleMax+" systole:"+systoleMax+" pulse:"+pulseMax);
                        if(diabetesVal<diabetesMin || diabetesVal>diabetesMax ||
                                diastoleVal<diastoleMin || diastoleVal>diastoleMax ||
                                systoleVal<systoleMin || systoleVal>systoleMax ||
                                pulseVal<pulseMin || pulseVal>pulseMax)
                        {
                            Toast.makeText(getApplicationContext(),"Değerleriniz egzersiz yapmak için uygun değil. Sonra tekrar deneyiniz.",Toast.LENGTH_LONG).show();
                        }
                        else{
                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.saved_user_file_key), Context.MODE_PRIVATE);
                            int uid = sharedPref.getInt(getString(R.string.saved_user_uid_key), 0);
                            StatisticsProgramFirebaseDb statisticsProgramFirebaseDb = new StatisticsProgramFirebaseDb(currentPid,String.valueOf(uid),getCurrentDate(),String.valueOf(diastoleVal),String.valueOf(systoleVal),String.valueOf(pulseVal),String.valueOf(diabetesVal),true);
                            mLocalDBHelper.insertStatisticsProgram(statisticsProgramFirebaseDb,mFirebaseDbHelper);
                            Toast.makeText(getApplicationContext(),"Değerleriniz kaydedildi. Egzersizlerinize başlayabilirsiniz.",Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Lütfen tüm değerleri giriniz.",Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }
    }

    public String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy");
        String strDate = mdformat.format(calendar.getTime());
        return strDate;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK)
        {
            setResult(Activity.RESULT_OK);
            finish();
        }
        else if(resultCode == Keys.FINISHED_EXERCISE_RESULT_CODE)
        {
            adapter.notifyDataSetChanged();
        }
    }


}
