package com.diabet.muhendis.diabetex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.diabet.muhendis.diabetex.DownloadImageTask;
import com.diabet.muhendis.diabetex.ExerciseDetailsActivity;
import com.diabet.muhendis.diabetex.ExercisesActivity;
import com.diabet.muhendis.diabetex.Keys;
import com.diabet.muhendis.diabetex.R;
import com.diabet.muhendis.diabetex.helpers.LocalDBHelper;
import com.diabet.muhendis.diabetex.model.ProgramExerciseFirebaseDb;
import com.diabet.muhendis.diabetex.model.ProgramFirebaseDb;

import java.io.File;

/**
 * Created by muhendis on 27.01.2018.
 */

public class ExerciseRecylerViewAdapter extends RecyclerView.Adapter<ExerciseRecylerViewAdapter.ExerciseViewHolder> {

    private final String TAG = "ProgramListAdapter";
    private ProgramExerciseFirebaseDb[] mDataset;
    private ExercisesActivity exercisesActivity;
    private LocalDBHelper mLocalDbHelper;


    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final TextView exName,exWeeklyRep,exDailyRep;
        private final ImageView exImage,exDoneTick;

        private ExerciseViewHolder(final View itemView) {
            super(itemView);
            exName = itemView.findViewById(R.id.exName);
            exDailyRep = itemView.findViewById(R.id.exDailyRep);
            exWeeklyRep = itemView.findViewById(R.id.exWeeklyRep);
            exImage = itemView.findViewById(R.id.exImage);
            exDoneTick = itemView.findViewById(R.id.exerciseTickImage);
        }
    }

    private final LayoutInflater mInflater;

    public ExerciseRecylerViewAdapter(Context context, ProgramExerciseFirebaseDb[] dataset) {
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    public void setAdapter(ProgramExerciseFirebaseDb[] myDataset){
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    public void setLocalDbHelper(LocalDBHelper mLocalDbHelper){
        this.mLocalDbHelper = mLocalDbHelper;
    }

    public void setExercisesActivity(ExercisesActivity exercisesActivity){
        this.exercisesActivity = exercisesActivity;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recylerview_exercise_item, parent, false);
        return new ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ExerciseViewHolder holder, final int position) {
        if (mDataset != null) {

            if(mLocalDbHelper.isExerciseFinishedToday(mDataset[position].getEid(),mDataset[position].getPid()))
            {
                holder.exDoneTick.setVisibility(View.VISIBLE);
            }
            else{
                holder.exDoneTick.setVisibility(View.GONE);
            }
            holder.exName.setText(mDataset[position].getName());
            holder.exDailyRep.setText(String.valueOf(mDataset[position].getDailyRep()));
            holder.exWeeklyRep.setText(String.valueOf(mDataset[position].getWeeklyRep()));
            //new DownloadImageTask(holder.exImage).execute(mDataset[position].getPhotoLink());
            String imageFilePath = Environment.getExternalStorageDirectory()
                    + File.separator + ".diabetex/.images/"+mDataset[position].getPid()+"_"+mDataset[position].getEid()+".png";
            File imageFile = new File(imageFilePath);
            if(imageFile.exists())
            {
                holder.exImage.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            }
            else
            {
                new DownloadImageTask(holder.exImage).execute(mDataset[position].getPhotoLink());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ExerciseDetailsActivity.class);
                    intent.putExtra(Keys.EX_ID,mDataset[position].getEid());
                    intent.putExtra(Keys.PID_KEY,mDataset[position].getPid());
                    //v.getContext().startActivity(intent);
                    exercisesActivity.startActivityForResult(intent,0);
                }
            });


        } else {
            // Covers the case of data not being ready yet.
        }
    }



    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mDataset != null)
            return mDataset.length;
        else return 0;
    }
}