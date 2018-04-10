package com.example.muhendis.diabetex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.muhendis.diabetex.ExerciseDetailsActivity;
import com.example.muhendis.diabetex.ExerciseVideoActivity;
import com.example.muhendis.diabetex.ExercisesActivity;
import com.example.muhendis.diabetex.Keys;
import com.example.muhendis.diabetex.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import muhendis.diabetex.db.entity.ExerciseEntity;

/**
 * Created by muhendis on 27.01.2018.
 */

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseViewHolder> {

    private final String TAG = "ExerciseListAdapter";
    private static ExerciseListAdapter mExerciseListAdapter;
    private static int mExId;

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final TextView exName,exWeeklyRep,exDailyRep,exDuration;
        private final CardView mCardView;
        private final ImageView exImage;
        private boolean multiSelect = false;
        private ArrayList<Integer> selectedItems = new ArrayList<Integer>();
        private ActionMode mActionMode;
        private ExerciseListAdapter mExerciseListAdapter;


        private ExerciseViewHolder(View itemView, ExerciseListAdapter mExerciseListAdapter) {
            super(itemView);
            this.mExerciseListAdapter = mExerciseListAdapter;
            exName = itemView.findViewById(R.id.exName);
            exDailyRep = itemView.findViewById(R.id.exDailyRep);
            exWeeklyRep = itemView.findViewById(R.id.exWeeklyRep);
            exDuration = itemView.findViewById(R.id.exDuration);
            exImage = itemView.findViewById(R.id.exImage);
            mCardView = itemView.findViewById(R.id.exerciseCard);
            implementClickListener(itemView);
        }

        private void implementClickListener(View itemView)
        {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mActionMode!=null)
                    {
                        onListItemSelect(getAdapterPosition(),v);
                        Log.d(TAG,"Clicked");
                    }
                    else
                    {
                        Intent intent = new Intent(v.getContext(), ExerciseDetailsActivity.class);
                        intent.putExtra(Keys.EX_ID,mExId);
                        v.getContext().startActivity(intent);
                        Log.d(TAG,"Clicked when action mode is null");
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onListItemSelect(getAdapterPosition(),view);
                    return true;
                }
            });
        }

        private void onListItemSelect(int position, View view)
        {
            mExerciseListAdapter.toggleSelection(position);//Toggle the selection
            boolean hasCheckedItems = mExerciseListAdapter.getSelectedCount() > 0;//Check if any items are already selected or not
            if (hasCheckedItems && mActionMode == null)
                // there are some selected items, start the actionMode
                mActionMode = ((AppCompatActivity) view.getContext()).startSupportActionMode(new ToolbarActionModeCallbacks(mExerciseListAdapter,mActionMode));
            else if (!hasCheckedItems && mActionMode != null)
                // there no selected items, finish the actionMode
                mActionMode.finish();
            if (mActionMode != null)
                //set action mode title on item selection
                mActionMode.setTitle(String.valueOf(mExerciseListAdapter
                        .getSelectedCount()) + " selected");
        }
    }

    //Create an action mode callbacks to handle toolbar

    class ToolbarActionModeCallbacks implements ActionMode.Callback{

        //private Context context;
        private ExerciseListAdapter recyclerView_adapter;
        private ActionMode mActionMode;

        public ToolbarActionModeCallbacks(ExerciseListAdapter recyclerView_adapter, ActionMode actionMode) {
            //this.context = context;
            this.recyclerView_adapter = recyclerView_adapter;
            this.mActionMode = actionMode;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.recycleritem_select_menu, menu);//Inflate the menu over action mode
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
            //So here show action menu according to SDK Levels

            if (Build.VERSION.SDK_INT < 11) {
                MenuItemCompat.setShowAsAction(menu.findItem(R.id.context_menu_ok_icon), MenuItemCompat.SHOW_AS_ACTION_NEVER);
                //MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_copy), MenuItemCompat.SHOW_AS_ACTION_NEVER);
                //MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_forward), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            } else {
                menu.findItem(R.id.context_menu_ok_icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                //menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                //menu.findItem(R.id.action_forward).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }


            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            mode.finish();
            /*switch (item.getItemId()) {
                case R.id.action_delete:

                    //Check if current action mode is from ListView Fragment or RecyclerView Fragment
                    if (isListViewFragment) {
                        Fragment listFragment = new MainActivity().getFragment(0);//Get list view Fragment
                        if (listFragment != null)
                            //If list fragment is not null
                            ((ListView_Fragment) listFragment).deleteRows();//delete selected rows
                    } else {
                        //If current fragment is recycler view fragment
                        Fragment recyclerFragment = new MainActivity().getFragment(1);//Get recycler view fragment
                        if (recyclerFragment != null)
                            //If recycler fragment not null
                            ((RecyclerView_Fragment) recyclerFragment).deleteRows();//delete selected rows
                    }
                    break;
                case R.id.action_copy:

                    //Get selected ids on basis of current fragment action mode
                    SparseBooleanArray selected;
                    if (isListViewFragment)
                        selected = listView_adapter
                                .getSelectedIds();
                    else
                        selected = recyclerView_adapter
                                .getSelectedIds();

                    int selectedMessageSize = selected.size();

                    //Loop to all selected items
                    for (int i = (selectedMessageSize - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            //get selected data in Model
                            Item_Model model = message_models.get(selected.keyAt(i));
                            String title = model.getTitle();
                            String subTitle = model.getSubTitle();
                            //Print the data to show if its working properly or not
                            Log.e("Selected Items", "Title - " + title + "n" + "Sub Title - " + subTitle);

                        }
                    }
                    Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show();//Show toast
                    mode.finish();//Finish action mode
                    break;
                case R.id.action_forward:
                    Toast.makeText(context, "You selected Forward menu.", Toast.LENGTH_SHORT).show();//Show toast
                    mode.finish();//Finish action mode
                    break;


            }*/
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {

            //When action mode destroyed remove selected selections and set action mode to null
            //First check current fragment action mode
            /*if (isListViewFragment) {
                listView_adapter.removeSelection();  // remove selection
                Fragment listFragment = new MainActivity().getFragment(0);//Get list fragment
                if (listFragment != null)
                    ((ListView_Fragment) listFragment).setNullToActionMode();//Set action mode null
            } else {
                recyclerView_adapter.removeSelection();  // remove selection
                Fragment recyclerFragment = new MainActivity().getFragment(1);//Get recycler fragment
                if (recyclerFragment != null)
                    ((RecyclerView_Fragment) recyclerFragment).setNullToActionMode();//Set action mode null
            }*/

            //recyclerView_adapter.removeSelection();  // remove selection
            //mActionMode=null;
        }
    }

    private final LayoutInflater mInflater;
    private List<ExerciseEntity> mExercises; // Cached copy of exercises
    private boolean buttonClicked= false;
    private SparseBooleanArray mSelectedItemsIds;

    public ExerciseListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mSelectedItemsIds = new SparseBooleanArray();
        mExerciseListAdapter = this;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recylerview_exercise_item, parent, false);
        return new ExerciseViewHolder(itemView,mExerciseListAdapter);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        if (mExercises != null) {
            ExerciseEntity current = mExercises.get(position);
            mExId = current.getId();
            holder.exName.setText(current.getExName());
            holder.exDailyRep.setText(String.valueOf(current.getDailyRep()));
            holder.exWeeklyRep.setText(String.valueOf(current.getWeeklyRep()));
            holder.exDuration.setText(String.valueOf(current.getDuration())+" dk");
            /*holder.mCardView.setBackgroundColor(mSelectedItemsIds.get(position) ? Color.BLUE
                            : Color.RED);*/
            Log.d(TAG,"ON BIND VIEW HOLDER ICERSINDE Position:"+position);


        } else {
            // Covers the case of data not being ready yet.
            //holder.programHeader.setText("Egzersiziniz bulunmamaktadır.");
        }
    }

    public void setExercises(List<ExerciseEntity> exercises){
        mExercises = exercises;
        notifyDataSetChanged();
    }

    //Toggle selection methods
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
        Log.d(TAG,mSelectedItemsIds.toString());
    }


    //Remove selected selections
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }


    //Put or delete selected position into SparseBooleanArray
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }


    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mExercises != null)
            return mExercises.size();
        else return 0;
    }
}