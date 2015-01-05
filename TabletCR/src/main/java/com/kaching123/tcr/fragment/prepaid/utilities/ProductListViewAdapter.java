package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.WirelessItemHeaderView_;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;

/**
 * Created by teli.yin on 10/31/2014.
 */

public class ProductListViewAdapter extends ObjectsCursorAdapter<WirelessItem> {


    final String SELECT_CARRIER = "Select Carrier";
    final String SELECT_Product = "Select Product";

    public ProductListViewAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return ProductItemView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, WirelessItem item) {

        ProductItemView itemView = (ProductItemView) convertView;

        itemView.bind(item.name, item.iconUrl);

        return convertView;


    }

}
