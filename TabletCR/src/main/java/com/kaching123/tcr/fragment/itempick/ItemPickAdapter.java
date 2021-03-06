package com.kaching123.tcr.fragment.itempick;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.util.DrawableUtil;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.showPercent;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 11.11.13.
 */
public class ItemPickAdapter extends ObjectsCursorAdapter<ItemExModel> {

    private Drawable pencilDrawable;
    private Drawable pencilTransparent;

    public ItemPickAdapter(Context context) {
        super(context);
        pencilDrawable = context.getResources().getDrawable(R.drawable.pencil);
        DrawableUtil.boundDrawable(pencilDrawable);
        pencilTransparent = context.getResources().getDrawable(R.drawable.square_opacity);
        DrawableUtil.boundDrawable(pencilTransparent);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.itempick_item_view, parent, false);
        assert convertView != null;

        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) convertView.findViewById(R.id.item_pick_name);
        holder.ean = (TextView) convertView.findViewById(R.id.item_pick_ean);
        holder.discount = (TextView) convertView.findViewById(R.id.item_pick_discount);
        holder.price = (TextView) convertView.findViewById(R.id.item_pick_price);

        convertView.setTag(holder);
        return convertView;
    }

    private void showEanOrProductCode(ViewHolder holder, String productCode, String eanCode){
        if (TextUtils.isEmpty(eanCode)) {
            if (TextUtils.isEmpty(productCode)) {
                holder.ean.setVisibility(View.GONE);
            } else {
                holder.ean.setVisibility(View.VISIBLE);
                holder.ean.setText(productCode);
            }
        } else {
            holder.ean.setVisibility(View.VISIBLE);
            holder.ean.setText(eanCode);
        }
    }

    @Override
    protected View bindView(View convertView, int position, ItemExModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        ItemExModel i = getItem(position);


        if (i == null) {
            return convertView;
        }

        boolean isDiscountable = i.isDiscountable;
        DiscountType discountType = i.discountType;
        BigDecimal price = i.getCurrentPrice();

        holder.price.setCompoundDrawables(null, null, i.priceType == PriceType.OPEN ? pencilDrawable : pencilTransparent, null);
        holder.name.setText(i.description);
        showEanOrProductCode(holder, i.productCode, i.eanCode);

        if (isDiscountable && discountType != null) {
            BigDecimal discount = i.discount;
            switch (discountType) {
                case PERCENT:
                    showPercent(holder.discount, discount);
                    break;
                case VALUE:
                    showPrice(holder.discount, discount);
            }
        } else {
            holder.discount.setText(null);
        }

        showPrice(holder.price, price);

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView ean;
        TextView discount;
        TextView price;
    }
}
