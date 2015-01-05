package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.googlecode.androidannotations.annotations.EBean;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.PaymentTransactionModel;

/**
 * @author Ivan v. Rikhmayer
 */

@EBean
public class TransactionHistoryMiniAdapter extends ObjectsCursorAdapter<PaymentTransactionModel> {

    public TransactionHistoryMiniAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return TransactionHistoryMiniItemView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, PaymentTransactionModel item) {
        TransactionHistoryMiniItemView itemView = (TransactionHistoryMiniItemView) convertView;

//        if (itemView.ready()) return convertView;

        itemView.bind(position, item);
        return convertView;
    }
}