package com.kaching123.tcr.fragment.wireless;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.Unit;

/**
 * @author Ivan v. Rikhmayer
 */
public class UnitItemAdapter extends ObjectsCursorAdapter<Unit> {

    public UnitItemAdapter(Context context) {
        super(context);
    }


    @Override
    protected View newView(int position, ViewGroup parent) {
        return UnitItemView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, final Unit item) {
        UnitItemView itemView = (UnitItemView_) convertView;
        itemView.bind(item.serialCode, String.valueOf(item.warrantyPeriod), item.status.toString());
        return convertView;
    }
}