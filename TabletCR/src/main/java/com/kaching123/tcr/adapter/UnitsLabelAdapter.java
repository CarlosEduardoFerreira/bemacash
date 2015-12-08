package com.kaching123.tcr.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.UnitLabelModel;

/**
 * Created by alboyko 08.12.2015.
 */
public class UnitsLabelAdapter extends ObjectsCursorAdapter<UnitLabelModel> {

    public UnitsLabelAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light,
                parent, false);
    }

    @Override
    protected View bindView(View convertView, int position, UnitLabelModel item) {
        ((TextView) convertView).setText(item.shortcut);
        return convertView;
    }

    @Override
    protected View newDropDownView(int position, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
        return view;
    }

    public int getPositionById(String guid) {
        if (!TextUtils.isEmpty(guid)) {
            for (int i = 0; i < getCount(); i++) {
                if (guid.equals(getItem(i).guid))
                    return i;
            }
        }
        return 0;
    }

    public int getPositionByShortcut(String shortcut) {
        if (!TextUtils.isEmpty(shortcut)) {
            for (int i = 0; i < getCount(); i++) {
                if (shortcut.equals(getItem(i).shortcut))
                    return i;
            }
        }
        return 0;
    }

}
