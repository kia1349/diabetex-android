package com.example.muhendis.diabetex;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import android.widget.TextView;

import com.example.muhendis.diabetex.adapters.ExerciseListAdapter;
import com.example.muhendis.diabetex.adapters.ProgramListAdapter;

import java.util.List;

import muhendis.diabetex.db.entity.ExerciseEntity;
import muhendis.diabetex.db.entity.ProgramEntity;
import muhendis.diabetex.db.viewmodel.ExerciseViewModel;
import muhendis.diabetex.db.viewmodel.ProgramViewModel;

import static java.sql.Types.NULL;

public class ExercisesActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Button mSelectExerciseButton;
    private static int currentPid;
    private final String PID_KEY = "ProgramId";

    private final String TAG = "ExercisesActivity";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        //Get the pid from the intent
        Intent intent = getIntent();
        currentPid = intent.getIntExtra(PID_KEY,NULL);

        Toolbar toolbar = findViewById(R.id.exerciseToolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.exerciseTabs);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.greyDarker));
        tabLayout.setTabTextColors(getResources().getColor(R.color.greyDark),getResources().getColor(R.color.greyDarker));
        tabLayout.setSelectedTabIndicatorHeight(4);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.exerciseViewPagerContainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

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

    public static class ExerciseListFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private ExerciseViewModel mExerciseViewModel;
        private final String TAG = "ExerciseListFragment";

        public ExerciseListFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ExerciseListFragment newInstance(int sectionNumber) {
            ExerciseListFragment fragment = new ExerciseListFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.content_exercises, container, false);


            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
             /*
             * Add a recylerview to show all exercises in the main screen
             */
            RecyclerView recyclerView = rootView.findViewById(R.id.exerciseRecyclerview);

            //Register for contex menu to handle long click events in recyclerview item

            final ExerciseListAdapter adapter = new ExerciseListAdapter(getActivity());

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            // Initialize mProgramViewModel to cache the current data
            mExerciseViewModel = ViewModelProviders.of(this,new ExerciseViewModel.ExerciseViewModelFactory(getActivity().getApplication(),currentPid)).get(ExerciseViewModel.class);



            if(sectionNumber==1)
                //adapter.setExercises(mExerciseViewModel.getCompletedExercises());
                // Add an observer for the LiveData returned by getAllExercises method
                mExerciseViewModel.getUncompletedExercises().observe(this, new Observer<List<ExerciseEntity>>() {
                    @Override
                    public void onChanged(@Nullable final List<ExerciseEntity> exercises) {
                        // Update the cached copy of the words in the adapter.
                        adapter.setExercises(exercises);
                    }
                });
            else if(sectionNumber==2)
                mExerciseViewModel.getCompletedExercises().observe(this, new Observer<List<ExerciseEntity>>() {
                    @Override
                    public void onChanged(@Nullable final List<ExerciseEntity> exercises) {
                        // Update the cached copy of the words in the adapter.
                        adapter.setExercises(exercises);
                    }
                });

            return rootView;
        }


    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ExerciseListFragment (defined as a static inner class below).
            return ExerciseListFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Give page titles according to its position
            if(position==0)
                return getResources().getString(R.string.exercise_tab1_header);
            else
                return getResources().getString(R.string.exercise_tab2_header);
        }

    }



}
