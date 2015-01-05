package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.googlecode.androidannotations.annotations.EBean;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;

/**
 * @author Ivan v. Rikhmayer
 */
@EBean
public class WirelessItemCursorAdapter extends ObjectsCursorAdapter<WirelessItem> {


    final String SELECT_CARRIER = "Select Carrier";
    final String SELECT_Product = "Select Product";

    public WirelessItemCursorAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return WirelessItemHeaderView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, WirelessItem item) {

        WirelessItemHeaderView itemView = (WirelessItemHeaderView) convertView;

        if (item.name == null) {
            itemView.bindTextOnly(SELECT_CARRIER);
        } else if (item.name.equalsIgnoreCase(SELECT_Product)) {
            itemView.bindTextOnly(SELECT_Product);
        } else {
            itemView.bind(item.name, item.iconUrl);
        }

        return convertView;


    }

}