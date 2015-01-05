package com.kaching123.tcr.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.websvc.api.prepaid.VectorDocument;

/**
 * Created by teli.yin on 6/6/2014.
 */
public class PrepaidSunpassPayYourDocumentOrderViewAdapter extends BaseAdapter {
    private FragmentActivity mContext;
    private VectorDocument mDocuments;
    private final String DOLLAR_AMPERSAND = "$";

    public PrepaidSunpassPayYourDocumentOrderViewAdapter(FragmentActivity context, VectorDocument documents) {
        this.mContext = context;
        this.mDocuments = documents;
    }

    @Override
    public int getCount() {
        return mDocuments.size();
    }

    @Override
    public Object getItem(int i) {
        return mDocuments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.prepaid_pyd_order_view_listview_item, null);
        TextView name = (TextView) view.findViewById(R.id.tv_name);
        TextView content = (TextView) view.findViewById(R.id.tv_content);
        name.setText(mDocuments.get(i).documentId);
        content.setText(DOLLAR_AMPERSAND + String.valueOf(mDocuments.get(i).documentPaymentAmount));
        return view;
    }
}
