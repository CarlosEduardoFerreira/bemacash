package com.kaching123.tcr.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.DragSortListView;

/**
 * Created by vkompaniets on 23.09.2016.
 */

public class CursorDragAdapter extends CursorAdapter implements DragSortListView.DragSortListener {

    public CursorDragAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    @Override
    public void drag(int i, int i1) {

    }

    @Override
    public void drop(int i, int i1) {

    }

    @Override
    public void remove(int i) {

    }
}
