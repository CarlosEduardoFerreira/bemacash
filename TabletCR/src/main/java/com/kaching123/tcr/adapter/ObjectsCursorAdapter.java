package com.kaching123.tcr.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by gdubina on 18/11/13.
 */
public abstract class ObjectsCursorAdapter<T> extends BaseAdapter implements IObjectsAdapter<T> {

    private List<T> list;
    private Context context;

    public ObjectsCursorAdapter(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public T getItem(int position) {
        assert list != null;
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(position, parent);
        }
        return bindView(convertView, position, getItem(position));
    }

    protected abstract View newView(int position, ViewGroup parent);

    protected abstract View bindView(View convertView, int position, T item);

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newDropDownView(position, parent);
        }
        return bindView(convertView, position, getItem(position));
    }

    protected View newDropDownView(int position, ViewGroup parent) {
        return newView(position, parent);
    }

    public synchronized void changeCursor(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
