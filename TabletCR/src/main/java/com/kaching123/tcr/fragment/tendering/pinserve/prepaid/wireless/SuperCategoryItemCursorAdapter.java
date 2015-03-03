package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.EBean;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;

/**
 * @author Ivan v. Rikhmayer
 */
@EBean
public class SuperCategoryItemCursorAdapter extends ObjectsArrayAdapter<WirelessItem> {
    private final String SELECT_CARRIER = "Select Carrier";


    public SuperCategoryItemCursorAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.prepaid_spinner_carrier_item, parent, false);
    }

    @Override
    protected View newDropDownView(int position, ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.prepaid_spinner_dropdown_carrier_item, parent, false);
    }

    @Override
    protected View bindView(View convertView, int position, WirelessItem item) {
        TextView countryLabel = (TextView) convertView;
        if(position == 0)
            countryLabel.setText(SELECT_CARRIER);
        else
            countryLabel.setText(item.carrierName);
        return convertView;
    }
}