package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.R;

import java.util.List;
import java.util.Map;

/**
 * Created by gdubina on 25.11.13.
 */
public class ProductFlagsAdapter extends BaseAdapter {

    private Context mContext;
    private List mData;

    public ProductFlagsAdapter(Context context, List mData) {
        super();
        this.mContext = context;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, Integer> getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.productlist_flag_view, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.countryName = (TextView) convertView.findViewById(R.id.country_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map.Entry<String, Integer> item = getItem(position);
        if (item.getValue() != null)
            holder.imageView.setImageDrawable(mContext.getResources().getDrawable(item.getValue()));
        else
            holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.operator_default_icon));
        holder.countryName.setText(item.getKey().toString());
        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView countryName;
    }
}
