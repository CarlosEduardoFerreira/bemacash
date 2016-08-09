package com.kaching123.tcr.fragment.inventory;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.util.DrawableUtil;

/**
 * Created by vkompaniets on 27.11.13.
 */
public class ItemAdapter extends ObjectsCursorAdapter<ItemExModel> {

    private Drawable pencilDrawable;
    private Drawable pencilTransparent;

    public ItemAdapter(Context context) {
        super(context);

        pencilDrawable = context.getResources().getDrawable(R.drawable.pencil);
        DrawableUtil.boundDrawable(pencilDrawable);

        pencilTransparent = context.getResources().getDrawable(R.drawable.square_opacity);
        DrawableUtil.boundDrawable(pencilTransparent);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        return ItemView_.build(getContext());
    }

    @Override
    protected View bindView(View convertView, int position, ItemExModel item) {
        ItemView view = (ItemView) convertView;
        view.bind(
                position,
                pencilDrawable,
                pencilTransparent,
                item.description,
                item.eanCode,
                item.productCode,
                item.price,
                item.priceType,
                item.availableQty,
                item.cost,
                item.isPcsUnit ? null : item.shortCut);
        return view;
    }

}
