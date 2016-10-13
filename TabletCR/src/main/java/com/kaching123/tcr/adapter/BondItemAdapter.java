package com.kaching123.tcr.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;

import java.util.List;
import java.util.Locale;

/**
 * @author Ivan v. Rikhmayer
 */
public class BondItemAdapter extends ArrayAdapter<Integer> {

    private final List<Integer> list;
    private final Context context;

    public BondItemAdapter(Context context, List<Integer> list) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Button view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = (Button) inflater.inflate(R.layout.bond_item, null);
        } else {
            view = (Button)convertView;
        }
        int value = list.get(position);
        String  name = String.format(Locale.US, "%1$s%2$d", TcrApplication.getCountryFunctionality().currencySymbolUTF() , value);
        view.setText(name);
        view.setTag(value);
        return view;
    }

    @Override
    public long getItemId(int position) {
        return list.get(position);
    }
}