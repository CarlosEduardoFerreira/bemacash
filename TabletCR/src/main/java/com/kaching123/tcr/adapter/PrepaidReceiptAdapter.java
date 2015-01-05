package com.kaching123.tcr.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;

/**
 * Created by teli.yin on 6/6/2014.
 */
public class PrepaidReceiptAdapter extends BaseAdapter{
    private FragmentActivity mContext;
    private String[] mNames;
    private String[] mContents;

    public PrepaidReceiptAdapter(FragmentActivity context, String[] names, String[] contents)
    {
        this.mContext = context;
        this.mNames = names;
        this.mContents = contents;
    }
    @Override
    public int getCount() {
        return mNames.length;
    }

    @Override
    public Object getItem(int i) {
        return mNames[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.prepaid_receipt_listview_item, null);
        TextView name = (TextView) view.findViewById(R.id.tv_name);
        TextView content = (TextView) view.findViewById(R.id.tv_content);
        name.setText(mNames[i]);
        content.setText(mContents[i]);
        return view;
    }
}
