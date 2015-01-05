package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

/**
 * Created by gdubina on 25.11.13.
 */
public class ProductItemsAdapter extends ObjectsCursorAdapter<WirelessItem> {

    private Context mContext;

    public ProductItemsAdapter(Context context, List<WirelessItem> objects) {
        super(context);
        changeCursor(objects);
        this.mContext = context;
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.productlist_item_view, parent, false);
        assert convertView != null;

        ViewHolder holder = new ViewHolder();
        holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    protected View bindView(View convertView, int position, WirelessItem item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        WirelessItem i = getItem(position);

        if (i == null) {
            return convertView;
        }
        UrlImageViewHelper.setUrlDrawable(holder.imageView, i.iconUrl, R.drawable.operator_default_icon, 60000);
        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
