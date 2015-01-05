package com.kaching123.tcr.fragment.quickservice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ItemExModel;

import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by gdubina on 25.11.13.
 */
public class QuickItemsAdapter extends ObjectsCursorAdapter<ItemExModel> {

    public QuickItemsAdapter(Context context, List<ItemExModel> objects) {
        super(context);
        changeCursor(objects);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.quickservice_item_view, parent, false);
        assert convertView != null;

        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) convertView.findViewById(R.id.title);
        holder.price = (TextView) convertView.findViewById(R.id.price);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    protected View bindView(View convertView, int position, ItemExModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        ItemExModel i = getItem(position);

        if (i == null) {
            return convertView;
        }

        convertView.getBackground().setLevel(item.btnView);

//        convertView.setBackgroundResource(R.drawable.addon_active);

        holder.name.setText(i.description);
        /*if (i.isDiscountable) {
            showPrice(holder.price, CalculationUtil.getSubTotal(BigDecimal.ONE, i.price, i.discount, i.discountType));
        } else {*/
            showPrice(holder.price, i.price);
        //}

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView price;
    }
}
