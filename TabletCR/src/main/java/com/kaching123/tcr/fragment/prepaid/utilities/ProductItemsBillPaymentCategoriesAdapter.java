package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;

import java.util.List;

/**
 * Created by gdubina on 25.11.13.
 */
public class ProductItemsBillPaymentCategoriesAdapter extends BaseAdapter {

    private Context mContext;
    private List<BillPaymentItem> mData;

    public ProductItemsBillPaymentCategoriesAdapter(Context context, List<BillPaymentItem> list) {
        this.mContext = context;
        this.mData = list;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public BillPaymentItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.productlist_item_bill_payment_view, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BillPaymentItem item = getItem(position);
        holder.text.setText(item.categoryDescription);
        return convertView;
    }

    private static class ViewHolder {
        TextView text;
    }

}
