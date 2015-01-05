package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.googlecode.androidannotations.annotations.EBean;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemView.IQtyListener;
import com.kaching123.tcr.model.payment.HistoryDetailedOrderItemModel;

import java.util.Date;

/**
 * @author Ivan v. Rikhmayer
 */
@EBean
public class HistoryDetailedOrderItemAdapter extends ObjectsCursorAdapter<HistoryDetailedOrderItemModel> {

    private HistoryDetailedOrderItemListFragment watcher;
    private IQtyListener itemListener;

    public HistoryDetailedOrderItemAdapter(Context context) {
        super(context);
    }

    public void setWatcher(HistoryDetailedOrderItemListFragment watcher) {
        this.watcher = watcher;
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return HistoryDetailedOrderItemView_.build(getContext()).setListener(itemListener).setCallback(watcher);//.setHost(this.host);

    }

    @Override
    protected View bindView(View convertView, int position, HistoryDetailedOrderItemModel item) {
        ((HistoryDetailedOrderItemView)convertView).bind(item);
        return convertView;
    }

    public void setItemListener(IQtyListener itemListener) {
        this.itemListener = itemListener;
    }

    public void setAllChecked(boolean isChecked) {
        for (int i = 0; i < getCount(); i++) {
            HistoryDetailedOrderItemModel item = getItem(i);
            if (!item.saleItemModel.isSerializable)
                item.wanted = isChecked;
        }
        notifyDataSetChanged();
    }

}