package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;
import java.util.Map;

/**
 * Created by gdubina on 25.11.13.
 */
public class ProductCarriersAdapter extends BaseAdapter {

    private Context mContext;
    private List mData;

    public ProductCarriersAdapter(Context context, List mData) {
        super();
        this.mContext = context;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, WirelessItem> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.productlist_carrier_view, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map.Entry<String, WirelessItem> item = getItem(position);
        WirelessItem wirelessItem = item.getValue();
        UrlImageViewHelper.setUrlDrawable(holder.imageView, wirelessItem.iconUrl, R.drawable.operator_default_icon, 60000);
        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
