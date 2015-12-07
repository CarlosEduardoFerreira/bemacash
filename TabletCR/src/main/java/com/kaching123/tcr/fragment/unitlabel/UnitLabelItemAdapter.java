package com.kaching123.tcr.fragment.unitlabel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.UnitLabelModel;

/**
 * Created by alboyko 07.12.2015
 */
public class UnitLabelItemAdapter extends ObjectsCursorAdapter<UnitLabelModel> {

    public UnitLabelItemAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return UnitLabelItemView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, final UnitLabelModel item) {
        UnitLabelItemView itemView = (UnitLabelItemView_) convertView;
        itemView.bind(item.description, item.shortcut);
        return convertView;
    }


}
