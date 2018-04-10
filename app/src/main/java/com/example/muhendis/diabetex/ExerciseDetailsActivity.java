package com.example.muhendis.diabetex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class ExerciseDetailsActivity extends AppCompatActivity {
    ImageView mVideoPlay;
    private final String TAG = "ExerciseDetailsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_details);
        mVideoPlay = findViewById(R.id.exDetailsVideoPlayImage);
        implementPlayImageOnClickListener();

        Toolbar toolbar = findViewById(R.id.exDetailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setDisplayOptions();

        new DownloadImageTask((ImageView) findViewById(R.id.exDetailsImage))
                .execute("https://drive.google.com/uc?export=download&id=1CLmo1td29K47fqnca2j0JbvFk3NO_pbU");



    }

    private void implementPlayImageOnClickListener()
    {
        mVideoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ExerciseVideoActivity.class);
                startActivity(intent);
            }
        });
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
        Log.d(TAG,"Minimum height: "+((int)(width/1920.0*1080)));
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


}
