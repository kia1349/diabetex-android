package com.diabet.muhendis.diabetex.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.diabet.muhendis.diabetex.ExercisesActivity;
import com.diabet.muhendis.diabetex.MainActivity;
import com.diabet.muhendis.diabetex.R;
import com.diabet.muhendis.diabetex.helpers.LocalDBHelper;
import com.diabet.muhendis.diabetex.model.ProgramFirebaseDb;

import java.text.SimpleDateFormat;
import java.util.List;

import muhendis.diabetex.db.entity.ProgramEntity;

/**
 * Created by muhendis on 27.01.2018.
 */

public class ProgramRecylerViewAdapter extends RecyclerView.Adapter<ProgramRecylerViewAdapter.ProgramViewHolder> {

    private final String TAG = "ProgramListAdapter";
    private final String PID_KEY = "ProgramId";
    private ProgramFirebaseDb[] mDataset;
    private LocalDBHelper mLocalDbHelper;
    private MainActivity mainActivity;

    class ProgramViewHolder extends RecyclerView.ViewHolder {
        private final TextView programHeader,programStartDate,programFinishDate;
        private final Button mStartButton;
        private final ImageView mProgramDoneImage;
        private ProgramViewHolder(final View itemView) {
            super(itemView);
            programHeader = itemView.findViewById(R.id.programHeader);
            programStartDate= itemView.findViewById(R.id.programStartDate);
            programFinishDate= itemView.findViewById(R.id.programFinishDate);
            mStartButton = itemView.findViewById(R.id.startButton);
            mProgramDoneImage = itemView.findViewById(R.id.programDoneImage);

        }
    }

    private final LayoutInflater mInflater;

    public ProgramRecylerViewAdapter(Context context, ProgramFirebaseDb[] dataset) {
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    public void setAdapter(ProgramFirebaseDb[] myDataset){
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    public void setLocalDbHelper(LocalDBHelper mLocalDbHelper){
        this.mLocalDbHelper = mLocalDbHelper;
    }

    public void setMainActivity(MainActivity activity){
        mainActivity=activity;
    }

    @Override
    public ProgramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ProgramViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProgramViewHolder holder, int position) {
        if (mDataset != null) {
            Log.d("ProgramRecylerViewAd","DATA exists");


            if(mLocalDbHelper.isStatisticsProgramInsertedToday(mDataset[position].getPid()))
            {
                holder.mProgramDoneImage.setVisibility(View.VISIBLE);
            }
            else{

            }
            holder.programHeader.setText(mDataset[position].getName());

            holder.programStartDate.setText(mDataset[position].getStartDate());
            holder.programFinishDate.setText(mDataset[position].getFinishDate());
            final String pid = mDataset[position].getPid();

            holder.mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(v.getContext(), ExercisesActivity.class);
                    myIntent.putExtra(PID_KEY,pid);

                    mainActivity.startActivityForResult(myIntent,0);
                    //v.getContext().startActivity(myIntent);
                }
            });

        } else {
            Log.d("ProgramRecylerViewAd","NO DATA");

            // Covers the case of data not being ready yet.
            holder.programHeader.setText("Egzersiziniz bulunmamaktadır.");

        }
    }



    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {

        if (mDataset != null){
            //Log.d("ProgramRecylerViewAd","DATA exists");
            return mDataset.length;
        }

        else{
            //Log.d("ProgramRecylerViewAd","NO DATA");
            return 0;
        }
    }
}