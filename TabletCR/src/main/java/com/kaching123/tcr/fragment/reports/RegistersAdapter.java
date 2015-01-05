package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.RegisterModel;

/**
 * Created by hamsterksu on 23.01.14.
 */
public class RegistersAdapter extends ObjectsCursorAdapter<RegisterModel> {

    public RegistersAdapter(Context context) {
        super(context);
    }

    protected View newDropDownView(int position, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
        return view;
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light, parent, false);
        return view;
    }

    @Override
    protected View bindView(View view, int position, RegisterModel item) {
        ((TextView) view).setText(item.title);
        return view;
    }

}
