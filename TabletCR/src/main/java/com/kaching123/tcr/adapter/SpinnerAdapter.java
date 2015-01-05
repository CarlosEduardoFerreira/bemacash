package com.kaching123.tcr.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

import java.util.HashMap;

/**
 * Created by vkompaniets on 03.12.13.
 */
public abstract class SpinnerAdapter extends SimpleCursorAdapter {

    private HashMap<String, Integer> id2Position = new HashMap<String, Integer>();

    String[] mOriginalFrom;

    public SpinnerAdapter(Context context, int layout, String[] from, int[] to, int dropDownViewResource) {
        super(context, layout, null, from, to, 0);
        mOriginalFrom = from;
        setDropDownViewResource(dropDownViewResource);
    }

    protected abstract String getIdColumnName();

    @Override
    public void changeCursor(Cursor cursor) {
        setPositions(cursor);
        super.changeCursor(cursor);
    }

    private void setPositions(Cursor cursor) {
        if (getIdColumnName() == null){
            return;
        }
        id2Position.clear();
        if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()){
            do{
                String id = cursor.getString(cursor.getColumnIndex(getIdColumnName()));
                id2Position.put(id, cursor.getPosition());
            }while(cursor.moveToNext());
        }
    }

    public int getPosition4Id(String id){
        return id2Position.containsKey(id) ? id2Position.get(id) : 0;
    }

    public String getGuid(int pos){
        assert getIdColumnName() != null;
        Cursor c = getCursor();
        if(c == null || c.isClosed() || !c.moveToPosition(pos))
            return null;
        return c.getString(c.getColumnIndex(getIdColumnName()));
    }

    //removed dependency from _id field
    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
            mRowIDColumn = newCursor.getColumnIndex("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }

        // rescan columns in case cursor layout is different
        findColumns(mOriginalFrom);
        return oldCursor;
    }

    private void findColumns(String[] from) {
        if (mCursor != null) {
            int i;
            int count = from.length;
            if (mFrom == null || mFrom.length != count) {
                mFrom = new int[count];
            }
            for (i = 0; i < count; i++) {
                mFrom[i] = mCursor.getColumnIndexOrThrow(from[i]);
            }
        } else {
            mFrom = null;
        }
    }

    //removed dependency from _id field
    public long getItemId(int position) {
        if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mRowIDColumn != -1 ? mCursor.getLong(mRowIDColumn) : position;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
