package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.EmployeeForReportsModel;

/**
 * Created by hamsterksu on 23.01.14.
 */
public class EmployeesAdapter extends ObjectsCursorAdapter<EmployeeForReportsModel> {

    public EmployeesAdapter(Context context) {
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
    protected View bindView(View view, int position, EmployeeForReportsModel item) {
        ((TextView) view).setText(item.login);
        return view;
    }

}
