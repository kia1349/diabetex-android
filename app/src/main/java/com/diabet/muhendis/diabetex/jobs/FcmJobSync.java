package com.diabet.muhendis.diabetex.jobs;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.diabet.muhendis.diabetex.services.MyFirebaseMessagingService;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class FcmJobSync extends Job {
    public static final String TAG = "fcm_job_tag";
    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent i = new Intent(getContext(), MyFirebaseMessagingService.class);
        getContext().startService(i);
        return Result.SUCCESS;
    }
    public static void schedulePeriodicJob() {
        int jobId = new JobRequest.Builder(FcmJobSync.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }
}
