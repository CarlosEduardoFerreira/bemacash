package com.kaching123.tcr.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by gdubina on 18/11/13.
 */
public abstract class ObjectsArrayAdapter<T> extends ArrayAdapter<T> implements IObjectsAdapter<T> {

    public ObjectsArrayAdapter(Context context) {
        super(context, 0);
    }

    public ObjectsArrayAdapter(Context context, List<T> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //long time = System.currentTimeMillis();
        if (convertView == null) {
            convertView = newView(position, parent);
        }
        View v = bindView(convertView, position, getItem(position));
        //Logger.d("Provider: getView - end: pos = %d; time = " + (System.currentTimeMillis() - time), position);
        return v;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newDropDownView(position, parent);
        }
        View v = bindView(convertView, position, getItem(position));
        return v;
    }

    protected View newDropDownView(int position, ViewGroup parent) {
        return newView(position, parent);
    }

    protected abstract View newView(int position, ViewGroup parent);

    protected abstract View bindView(View convertView, int position, T item);

    public void changeCursor(List<T> list) {
        clear();
        if (list != null) {
            addAll(list);
        }
    }
}
