package com.diabet.muhendis.diabetex;

import android.app.Application;

import com.diabet.muhendis.diabetex.jobs.AppJobCreator;
import com.diabet.muhendis.diabetex.jobs.FcmJobSync;
import com.evernote.android.job.JobManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new AppJobCreator());
        FcmJobSync.schedulePeriodicJob();
    }
}
