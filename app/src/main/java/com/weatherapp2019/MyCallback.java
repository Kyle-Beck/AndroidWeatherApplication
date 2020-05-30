package com.weatherapp2019;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.weatherapp2019.JSONClasses.Complete;

import java.util.ArrayList;

public class MyCallback extends ItemTouchHelper.SimpleCallback {
    ArrayList<Complete> completes;
    CustomRecyclerAdapter adapter;
    MainActivity context;
    public MyCallback(MainActivity context, ArrayList<Complete> completes, CustomRecyclerAdapter adapter){
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
        this.completes = completes;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

        int position = viewHolder.getAdapterPosition();
        final Complete complete = completes.get(position);

        // Instantiate database
        final SavedCitiesHelper helper = new SavedCitiesHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        // Delete swiped city from database and the adapter's list. Then notify adapter
        db.delete("SavedCities", "CityID=" + completes.get(position).id, null);
        completes.remove(position);
        adapter.notifyItemRemoved(position);
        if(adapter.getItemCount() == 0) {
            // If noSavedCities message is empty
            if(context.findViewById(R.id.noSavedCities).toString().equals("")) {
                TextView noSavedCities = context.findViewById(R.id.noSavedCities);
                noSavedCities.setText("Add cities to your favorites by searching and clicking the 'Add To Favorites' button");
            }
        }
        db.close();

        // Smnackbar notifying delete/offering undo
        Snackbar snackbarDeleted = Snackbar.make(context.findViewById(R.id.root), R.string.snackbar_deleted_message, BaseTransientBottomBar.LENGTH_LONG);

        // Class used to undo
        class MyUndoListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                helper.insertData(complete.id);
                completes.add(complete);
                adapter.notifyDataSetChanged();
                if(!context.findViewById(R.id.noSavedCities).toString().equals("")) {
                    TextView noSavedCities = context.findViewById(R.id.noSavedCities);
                    noSavedCities.setText("");
                }
            }
        }

        // Attach undo class to snackbar. Show snackbar
        snackbarDeleted.setAction("Undo", new MyUndoListener());
        snackbarDeleted.show();
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        Toast.makeText(context, "on Move", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        ColorDrawable bgColor = new ColorDrawable(Color.RED);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        bgColor.setBounds(10000, viewHolder.itemView.getTop(),
                viewHolder.itemView.getRight() + Math.round(dX), viewHolder.itemView.getBottom());
        bgColor.draw(c);

        Drawable icon = ContextCompat.getDrawable(context, R.drawable.trash_can);
        int itemViewWidth = viewHolder.itemView.getWidth();
        int itemViewHeight = viewHolder.itemView.getHeight();

        icon.setBounds(viewHolder.itemView.getRight() - (itemViewWidth / 16) - icon.getIntrinsicWidth() * 3, viewHolder.itemView.getTop() + (itemViewHeight / 4),
                viewHolder.itemView.getRight() - (itemViewWidth / 16), viewHolder.itemView.getTop() + (itemViewHeight / 4) + icon.getIntrinsicHeight() * 3);
        icon.draw(c);

        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            icon.setVisible(false, true);
        }
    }
}
