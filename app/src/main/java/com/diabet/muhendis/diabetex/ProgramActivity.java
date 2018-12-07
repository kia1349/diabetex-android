package com.diabet.muhendis.diabetex;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diabet.muhendis.diabetex.adapters.ProgramRecylerViewAdapter;
import com.diabet.muhendis.diabetex.db.DiabetWatchDbHelper;
import com.diabet.muhendis.diabetex.helpers.FirebaseDBHelper;
import com.diabet.muhendis.diabetex.helpers.LocalDBHelper;
import com.diabet.muhendis.diabetex.helpers.UIHelper;
import com.diabet.muhendis.diabetex.model.ProgramFirebaseDb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain
 * to handle interaction events.
 * Use the {@link ProgramActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgramActivity extends Fragment {
    private final String TAG = "ProgramActivity";
    private LocalDBHelper mLocalDBHelper;
    private DiabetWatchDbHelper mDbHelper;
    private ProgramFirebaseDb[] mAllPrograms;
    private FirebaseDBHelper mFirebaseDBHelper;
    private ProgramRecylerViewAdapter adapter;
    private UIHelper mUIHelper;
    private Activity activity;


    public ProgramActivity() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment.

     * @return A new instance of fragment ProgramActivity.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgramActivity newInstance() {
        ProgramActivity fragment = new ProgramActivity();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_program, container, false);

        mFirebaseDBHelper = new FirebaseDBHelper(getActivity().getApplicationContext(),getActivity());
        mDbHelper = new DiabetWatchDbHelper(getActivity().getApplicationContext());
        mLocalDBHelper = new LocalDBHelper(mDbHelper);
        mAllPrograms = mLocalDBHelper.getAllPrograms();
        mFirebaseDBHelper.syncLocalStatisticsWithFirebase(getUid());

        /*
         * Add a recylerview to show all exercises in the main screen
         */
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerview);
        if (mAllPrograms.length != 0) {
            rootView.findViewById(R.id.noProgramLinearLayout).setVisibility(View.GONE);
        }
        else{
            rootView.findViewById(R.id.noProgramLinearLayout).setVisibility(View.VISIBLE);
        }
        adapter = new ProgramRecylerViewAdapter(getActivity(),mAllPrograms);
        adapter.setLocalDbHelper(mLocalDBHelper);
        adapter.setMainActivity((MainActivity)getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setAdapter(adapter);

        syncDataWithFirebase();
        removeCompletedProgramsFromDB();
        // Inflate the layout for this fragment
        return rootView;
    }

    public void syncDataWithFirebase(){
        mFirebaseDBHelper.syncDataWithLocalDB(getActivity(),adapter);
    }

    public String getUid(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getResources().getString(R.string.saved_user_file_key),Context.MODE_PRIVATE);
        int uid = sharedPref.getInt(getString(R.string.saved_user_uid_key), 0);
        return String.valueOf(uid);
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
                        getActivity().findViewById(R.id.noProgramLinearLayout).setVisibility(View.GONE);
                    }
                    else{
                        getActivity().findViewById(R.id.noProgramLinearLayout).setVisibility(View.VISIBLE);
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

}
