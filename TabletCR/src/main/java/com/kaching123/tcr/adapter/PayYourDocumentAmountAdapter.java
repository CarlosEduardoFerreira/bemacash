package com.kaching123.tcr.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;

import java.util.ArrayList;

/**
 * Created by teli.yin on 6/6/2014.
 */
public class PayYourDocumentAmountAdapter extends BaseAdapter {
    private FragmentActivity mContext;
    ArrayList<String> mList;

    public PayYourDocumentAmountAdapter(FragmentActivity context, ArrayList<String> list) {
        this.mContext = context;
        mList = list;

    }

    @Override
    public int getCount() {
        return mList.size() - 1;
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = LayoutInflater.from(mContext).inflate(R.layout.pay_your_document_amount_listview_item, null);
        TextView name = (TextView) view.findViewById(R.id.tv_name);
        name.setText(mList.get(i));
        if(i == 2) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.prepaid_dialog_buttons_background_color));
            name.setTextColor(mContext.getResources().getColor(R.color.prepaid_dialog_white));
        }
        return view;
    }
}
