package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.SaleOrderTipsViewModel;

/**
 * @author Ivan v. Rikhmayer
 */

public class HistoryOrderAdapter extends ObjectsCursorAdapter<SaleOrderTipsViewModel> {

private final boolean isTipsEnabled;

    public HistoryOrderAdapter(Context context, boolean isTipsEnabled) {
        super(context);
        this.isTipsEnabled = isTipsEnabled;
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return HistoryOrderView_.build(getContext(), isTipsEnabled);
    }

    @Override
    protected View bindView(View convertView, int position, SaleOrderTipsViewModel item) {
        HistoryOrderView itemView = (HistoryOrderView) convertView;
        itemView.bind(
                item.registerTitle + "-" + item.printSeqNum,
                item.createTime,
                item.tmpTotalPrice,
                item.operatorName,
                item.tenderType,
                item.type,
                item.tipsAmount,
                item.transactionsState);
        return convertView;
    }
}
